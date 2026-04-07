package com.valentincolombat.upnews.ui.freemium

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.valentincolombat.upnews.data.billing.BillingManager
import kotlinx.coroutines.flow.StateFlow

class SubscriptionViewModel : ViewModel() {

    private val billing = BillingManager.shared

    val isLoading:      StateFlow<Boolean>         = billing.isLoading
    val monthlyProduct: StateFlow<ProductDetails?> = billing.monthlyProduct
    val yearlyProduct:  StateFlow<ProductDetails?> = billing.yearlyProduct
    val errorMessage:   StateFlow<String?>         = billing.errorMessage
    val purchaseSuccess: StateFlow<Boolean>        = billing.purchaseSuccess

    fun purchase(activity: Activity, product: ProductDetails) {
        billing.launchPurchaseFlow(activity, product)
    }

    suspend fun restorePurchases() = billing.restorePurchases()

    fun clearError()           = billing.clearError()
    fun resetPurchaseSuccess() = billing.resetPurchaseSuccess()
}
