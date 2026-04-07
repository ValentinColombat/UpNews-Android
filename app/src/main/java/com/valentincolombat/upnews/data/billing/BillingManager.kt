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
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.data.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Équivalent Android de StoreKitManager.shared (iOS).
 *
 * IDs Play Store à configurer dans Google Play Console :
 *   - PRODUCT_MONTHLY : abonnement mensuel
 *   - PRODUCT_YEARLY  : abonnement annuel
 */
class BillingManager private constructor(application: Application) {

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
        _isLoading.value = true
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
        }
        _isLoading.value = false
    }

    // MARK: - Validation achats

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        var shouldUpgrade = false
        for (purchase in purchases) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) continue

            // Acknowledger si nécessaire
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams)
            }

            shouldUpgrade = true
        }
        if (shouldUpgrade) upgradeUserToPremium()
    }

    private suspend fun upgradeUserToPremium() {
        // 1. Sync Supabase EN PREMIER (avant de fermer l'écran)
        try {
            val session = client.auth.currentSessionOrNull()
            val userId  = session?.user?.id?.toString()
            if (userId != null) {
                @Serializable data class TierUpdate(val subscription_tier: String)
                client.from("users")
                    .update(TierUpdate("premium")) {
                        filter { eq("id", userId) }
                    }
                Log.d("Billing", "Supabase subscription_tier=premium OK (userId=$userId)")
            } else {
                Log.w("Billing", "upgradeUserToPremium: session ou userId null, Supabase non mis à jour")
            }
        } catch (e: Exception) {
            Log.e("Billing", "Supabase premium sync failed: ${e.message}")
        }

        // 2. Mise à jour locale — suffit à réactiver toutes les features premium immédiatement
        userRepo.setSubscriptionTier(SubscriptionTier.PREMIUM)

        // 3. Signal succès — l'écran SubscriptionScreen se ferme seulement ici
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

            val hasActiveSub = result.purchasesList.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            if (!hasActiveSub && userRepo.isPremium && !userRepo.isOGMember.value) {
                Log.d("Billing", "Aucun abonnement actif — downgrade vers free")
                downgradeToFree()
            }
        } catch (e: Exception) {
            Log.e("Billing", "checkSubscriptionValidity erreur: ${e.message}")
        }
    }

    private suspend fun downgradeToFree() {
        try {
            val session = client.auth.currentSessionOrNull()
            val userId  = session?.user?.id?.toString()
            if (userId != null) {
                @Serializable data class TierUpdate(val subscription_tier: String)
                client.from("users")
                    .update(TierUpdate("free")) {
                        filter { eq("id", userId) }
                    }
                Log.d("Billing", "Supabase subscription_tier=free OK")
            }
        } catch (e: Exception) {
            Log.e("Billing", "Supabase downgrade sync failed: ${e.message}")
        }
        userRepo.setSubscriptionTier(SubscriptionTier.FREE)
        runCatching { userRepo.loadUserProfile() }
    }

    fun clearError()          { _errorMessage.value   = null  }
    fun resetPurchaseSuccess() { _purchaseSuccess.value = false }
}
