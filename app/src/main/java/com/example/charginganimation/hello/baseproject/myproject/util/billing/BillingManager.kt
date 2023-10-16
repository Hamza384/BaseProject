package com.example.charginganimation.hello.baseproject.myproject.util.billing

import android.app.Activity
import android.content.Context
import com.example.charginganimation.hello.baseproject.myproject.util.billing.helper.BillingHelper
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnPurchaseListener
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnConnectionListener



class BillingManager(context: Context) : BillingHelper(context) {

    override fun setCheckForSubscription(isCheckRequired: Boolean) {
        checkForSubscription = isCheckRequired
    }

    override fun startConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener) = startBillingConnection(productIdsList, onConnectionListener)

    fun makeInAppPurchase(activity: Activity?, onPurchaseListener: OnPurchaseListener) = purchaseInApp(activity, onPurchaseListener)

    fun makeSubPurchase(activity: Activity?, subscriptionPlans: String, onPurchaseListener: OnPurchaseListener) = purchaseSub(activity, subscriptionPlans, onPurchaseListener)

}