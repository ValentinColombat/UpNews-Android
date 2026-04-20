package com.valentincolombat.upnews.data.billing

import android.app.Activity
import android.app.Application
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import androidx.core.app.NotificationManagerCompat
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.data.repository.UserRepository
import com.valentincolombat.upnews.service.NotificationManager
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Équivalent Android de StoreKitManager.shared (iOS).
 *
 * IDs Play Store à configurer dans Google Play Console :
 *   - PRODUCT_MONTHLY : abonnement mensuel
 *   - PRODUCT_YEARLY  : abonnement annuel
 */
class BillingManager private constructor(private val application: Application) {

    companion object {
        // TODO: Remplacer par les vrais IDs Play Console
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_YEARLY  = "premium_yearly"

        @Volatile private var instance: BillingManager? = null

        fun init(application: Application) {
            instance ?: synchronized(this) {
                instance ?: BillingManager(application).also { instance = it }
            }
        }

        val shared: BillingManager get() = instance
            ?: error("BillingManager not initialized — call BillingManager.init() in Application.onCreate()")
    }

    // MARK: - State

    private val _isLoading          = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _monthlyProduct     = MutableStateFlow<ProductDetails?>(null)
    val monthlyProduct: StateFlow<ProductDetails?> = _monthlyProduct.asStateFlow()

    private val _yearlyProduct      = MutableStateFlow<ProductDetails?>(null)
    val yearlyProduct: StateFlow<ProductDetails?> = _yearlyProduct.asStateFlow()

    private val _errorMessage       = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _purchaseSuccess    = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    /** true = Play Store a débité l'utilisateur mais l'activation serveur a échoué.
     *  L'UI doit proposer "Restaurer mes achats" plutôt que de laisser l'utilisateur
     *  croire que son paiement n'a pas abouti. */
    private val _activationFailed   = MutableStateFlow(false)
    val activationFailed: StateFlow<Boolean> = _activationFailed.asStateFlow()

    // MARK: - Private

    private val scope   = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val client  = SupabaseClient.client
    private val userRepo = UserRepository.shared

    private val billingClient = BillingClient.newBuilder(application)
        .setListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) scope.launch { handlePurchases(purchases) }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> { /* ignoré volontairement */ }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    scope.launch { restorePurchases() }
                }
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                    _errorMessage.value = "Play Store indisponible. Vérifie ta connexion et réessaie."
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    _errorMessage.value = "La facturation n'est pas disponible sur cet appareil."
                }
                else -> {
                    _errorMessage.value = "L'achat a échoué. Réessaie dans quelques instants."
                }
            }
        }
        .enablePendingPurchases(
            com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    // MARK: - Init

    init { connect() }

    // MARK: - Connexion & chargement produits

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { queryProducts() }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Reconnexion automatique gérée par BillingClient
            }
        })
    }

    private suspend fun queryProducts() {
        _isLoading.value = true
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY).map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.forEach { pd ->
                when (pd.productId) {
                    PRODUCT_MONTHLY -> _monthlyProduct.value = pd
                    PRODUCT_YEARLY  -> _yearlyProduct.value  = pd
                }
            }
        }
        _isLoading.value = false
    }

    // MARK: - Achat

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken ?: run {
                _errorMessage.value = "Offre indisponible. Réessaie dans quelques instants."
                return
            }

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }

    // MARK: - Restauration

    suspend fun restorePurchases() {
        if (!billingClient.isReady) {
            _errorMessage.value = "Play Store indisponible. Vérifie ta connexion et réessaie."
            return
        }
        _isLoading.value = true
        try {
            val result = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchases = result.purchasesList.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (activePurchases.isNotEmpty()) {
                    handlePurchases(activePurchases)
                } else {
                    _errorMessage.value = "Aucun abonnement actif trouvé. Si tu penses que c'est une erreur, contacte le support."
                }
            } else {
                _errorMessage.value = "Play Store indisponible. Vérifie ta connexion et réessaie."
            }
        } catch (e: Exception) {
            Log.e("Billing", "restorePurchases erreur: ${e.message}", e)
            _errorMessage.value = "La restauration a échoué. Réessaie dans quelques instants."
        } finally {
            _isLoading.value = false
        }
    }

    // MARK: - Validation achats

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) continue

            if (!purchase.isAcknowledged) {
                val ackResult = billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                )
                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w("Billing", "Acknowledgment échoué (code ${ackResult.responseCode}) — Play Store retentera automatiquement")
                }
            }

            val productId = purchase.products.firstOrNull() ?: continue
            upgradeUserToPremium(purchase.purchaseToken, productId)
            break
        }
    }

    private suspend fun upgradeUserToPremium(purchaseToken: String, productId: String) {
        try {
            client.functions.invoke(
                function = "verify-android-purchase",
                body = buildJsonObject {
                    put("purchase_token", purchaseToken)
                    put("product_id", productId)
                }
            )
            Log.d("Billing", "Edge Function verify-android-purchase OK")
        } catch (e: Exception) {
            Log.e("Billing", "Edge Function failed: ${e.message}", e)
            // Le paiement Play Store a réussi — c'est uniquement l'activation serveur qui a échoué.
            // checkSubscriptionValidity() récupèrera l'état au prochain démarrage / retour en foreground.
            _activationFailed.value = true
            _errorMessage.value = "Ton paiement a bien été traité par Google Play, mais l'activation de ton abonnement a échoué. Appuie sur \"Restaurer\" pour réessayer."
            return
        }

        _activationFailed.value = false
        userRepo.setSubscriptionTier(SubscriptionTier.PREMIUM)
        NotificationManagerCompat.from(application)
            .cancel(NotificationManager.NOTIFICATION_ID_AUDIO_LIMIT)
        _purchaseSuccess.value = true
    }

    // MARK: - Vérification expiry abonnement

    /** Vérifie si l'abonnement est toujours actif côté Play Store.
     *  Si l'utilisateur est premium local mais n'a plus d'abonnement actif → downgrade. */
    suspend fun checkSubscriptionValidity() {
        if (!billingClient.isReady) return
        try {
            val result = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return

            val activePurchase = result.purchasesList.firstOrNull {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }

            if (activePurchase == null && userRepo.isPremium && !userRepo.isOGMember.value) {
                Log.d("Billing", "Aucun abonnement actif — downgrade vers free")
                downgradeToFree()
            } else if (activePurchase != null && !userRepo.isPremium) {
                Log.d("Billing", "Abonnement actif détecté mais user en free — re-upgrade")
                val productId = activePurchase.products.firstOrNull() ?: return
                upgradeUserToPremium(activePurchase.purchaseToken, productId)
            }
        } catch (e: Exception) {
            Log.e("Billing", "checkSubscriptionValidity erreur: ${e.message}")
        }
    }

    private suspend fun downgradeToFree() {
        try {
            client.functions.invoke("downgrade-android-purchase")
            Log.d("Billing", "Edge Function downgrade-android-purchase OK")
        } catch (e: Exception) {
            Log.e("Billing", "Edge Function downgrade failed: ${e.message}", e)
        }
        userRepo.setSubscriptionTier(SubscriptionTier.FREE)
        runCatching { userRepo.loadUserProfile() }
    }

    fun clearError()           { _errorMessage.value   = null  ; _activationFailed.value = false }
    fun resetPurchaseSuccess() { _purchaseSuccess.value = false }

}
