package com.example.charginganimation.hello.baseproject.myproject.util.billing.helper

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.example.charginganimation.hello.baseproject.myproject.util.billing.constants.SubscriptionPlans
import com.example.charginganimation.hello.baseproject.myproject.util.billing.constants.SubscriptionProductIds
import com.example.charginganimation.hello.baseproject.myproject.util.billing.dataClasses.ProductDetail
import com.example.charginganimation.hello.baseproject.myproject.util.billing.dataProvider.DataProviderInApp
import com.example.charginganimation.hello.baseproject.myproject.util.billing.dataProvider.DataProviderSub
import com.example.charginganimation.hello.baseproject.myproject.util.billing.enums.BillingState
import com.example.charginganimation.hello.baseproject.myproject.util.billing.enums.ProductType
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnPurchaseListener
import com.example.charginganimation.hello.baseproject.myproject.util.billing.status.State.getBillingState
import com.example.charginganimation.hello.baseproject.myproject.util.billing.status.State.setBillingState
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnConnectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("unused")
abstract class BillingHelper(private val context: Context) {

    private val dataProviderInApp by lazy { DataProviderInApp() }
    private val dataProviderSub by lazy { DataProviderSub() }

    private var onConnectionListener: OnConnectionListener? = null
    private var onPurchaseListener: OnPurchaseListener? = null

    private var isPurchasedFound = false

    @JvmField
    protected var checkForSubscription = false

    private val _productDetailList = ArrayList<ProductDetail>()
    private val productDetailList: List<ProductDetail> get() = _productDetailList.toList()

    private val _productDetailsLiveData = MutableLiveData<List<ProductDetail>>()
    val productDetailsLiveData: LiveData<List<ProductDetail>> = _productDetailsLiveData

    /* ------------------------------------------------ Initializations ------------------------------------------------ */

    private val billingClient by lazy {
        BillingClient.newBuilder(context).setListener(purchasesUpdatedListener).enablePendingPurchases().build()
    }

    /* ------------------------------------------------ Establish Connection ------------------------------------------------ */
    abstract fun setCheckForSubscription(isCheckRequired: Boolean)

    abstract fun startConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener)

    /**
     *  Get a single testing product_id ("android.test.purchased")
     */
    fun getDebugProductIDList() = dataProviderInApp.getDebugProductIDList()

    /**
     *  Get multiple testing product_ids
     */
    fun getDebugProductIDsList() = dataProviderInApp.getDebugProductIDsList()

    protected fun startBillingConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener) {
        this.onConnectionListener = onConnectionListener
        if (productIdsList.isEmpty()) {
            setBillingState(BillingState.EMPTY_PRODUCT_ID_LIST)
            onConnectionListener.onConnectionResult(false, BillingState.EMPTY_PRODUCT_ID_LIST.message)
            return
        }
        dataProviderInApp.setProductIdsList(productIdsList)
        setBillingState(BillingState.CONNECTION_ESTABLISHING)

        if (billingClient.isReady) {
            setBillingState(BillingState.CONNECTION_ALREADY_ESTABLISHING)
            proceedBilling()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                setBillingState(BillingState.CONNECTION_DISCONNECTED)
                Handler(Looper.getMainLooper()).post {
                    onConnectionListener.onConnectionResult(false, BillingState.CONNECTION_DISCONNECTED.message)
                }
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val isBillingReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                if (isBillingReady) {
                    proceedBilling()
                } else {
                    setBillingState(BillingState.CONNECTION_FAILED)
                    onConnectionListener.onConnectionResult(false, billingResult.debugMessage)
                }
            }
        })
    }

    private fun proceedBilling() {
        setBillingState(BillingState.CONNECTION_ESTABLISHED)
        getInAppOldPurchases()
        Handler(Looper.getMainLooper()).post {
            onConnectionListener?.onConnectionResult(true, BillingState.CONNECTION_ESTABLISHED.message)
        }
    }

    private fun getInAppOldPurchases() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_FETCHING)
        val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { _, purchases ->
            CoroutineScope(Dispatchers.Main).launch {
                onConnectionListener?.onOldPurchaseResult(false)
            }

            Log.d(TAG, " --------------------------- old purchase (In-App)  --------------------------- ")
            Log.d(TAG, "getInAppOldPurchases: Object: $purchases")
            purchases.forEach { purchase ->
                Log.d(TAG, "getInAppOldPurchases: Object: $purchase")
                Log.d(TAG, "getInAppOldPurchases: Products: ${purchase.products}")
                Log.d(TAG, "getInAppOldPurchases: Original JSON: ${purchase.originalJson}")
                Log.d(TAG, "getInAppOldPurchases: Developer Payload: ${purchase.developerPayload}")

                if (purchase.products.isEmpty()) {
                    setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_FOUND)
                    return@forEach
                }

                // getting the  single  product-id of every purchase in the list = sku
                val compareSKU = purchase.products[0]

                if (purchase.isAcknowledged) {
                    dataProviderInApp.getProductIdsList().forEach {
                        if (it.contains(compareSKU, true)) {
                            isPurchasedFound = true
                            setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED)
                            Handler(Looper.getMainLooper()).post {
                                CoroutineScope(Dispatchers.Main).launch {
                                    onConnectionListener?.onOldPurchaseResult(true)
                                }
                            }
                        }
                    }
                    checkForSubscriptionIfAvailable()
                } else {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        for (i in 0 until dataProviderInApp.getProductIdsList().size) {
                            setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_BUT_NOT_ACKNOWLEDGE)

                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()

                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK || purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_ACKNOWLEDGE)
                                    isPurchasedFound = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        onConnectionListener?.onOldPurchaseResult(true)
                                    }
                                } else {
                                    setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_FAILED_TO_ACKNOWLEDGE)
                                }
                                checkForSubscriptionIfAvailable()
                            }
                        }
                    } else {
                        checkForSubscriptionIfAvailable()
                    }
                }
            }
            if (purchases.isEmpty()) {
                setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_FOUND)
                checkForSubscriptionIfAvailable()
            }
            queryForAvailableInAppProducts()
            queryForAvailableSubProducts()
        }
    }

    private fun checkForSubscriptionIfAvailable() {
        if (isPurchasedFound || !checkForSubscription) {
            return
        }
        getSubscriptionOldPurchases()
    }

    private fun getSubscriptionOldPurchases() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_FETCHING)
        val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()

        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                CoroutineScope(Dispatchers.Main).launch {
                    onConnectionListener?.onOldPurchaseResult(false)
                }

                Log.d(TAG, " --------------------------- old purchase (Sub)   --------------------------- ")
                Log.d(TAG, "getSubscriptionOldPurchases: List: $purchases")
                purchases.forEach { purchase ->
                    Log.d(TAG, "getSubscriptionOldPurchases: Object: $purchase")
                    Log.d(TAG, "getSubscriptionOldPurchases: Products: ${purchase.products}")
                    Log.d(TAG, "getSubscriptionOldPurchases: Original JSON: ${purchase.originalJson}")
                    Log.d(TAG, "getSubscriptionOldPurchases: Developer Payload: ${purchase.developerPayload}")


                    if (purchase.products.isEmpty()) {
                        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_NOT_FOUND)
                        return@forEach
                    }

                    // getting the  single  product-id of every purchase in the list = sku
                    val compareSKU = purchase.products[0]

                    if (purchase.isAcknowledged) {
                        for (i in 0 until dataProviderSub.productIdsList.size) {
                            if (dataProviderSub.productIdsList[i].contains(compareSKU)) {
                                setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED)
                                CoroutineScope(Dispatchers.Main).launch {
                                    onConnectionListener?.onOldPurchaseResult(true)
                                }
                                return@forEach
                            }
                        }
                    } else {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_BUT_NOT_ACKNOWLEDGE)
                            for (i in 0 until dataProviderSub.productIdsList.size) {
                                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK || purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_ACKNOWLEDGE)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            onConnectionListener?.onOldPurchaseResult(true)
                                        }
                                    } else {
                                        setBillingState(BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_FAILED_TO_ACKNOWLEDGE)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* -------------------------------------------- Query available console products  -------------------------------------------- */

    private fun queryForAvailableInAppProducts() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING)
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(dataProviderInApp.getProductList()).build())
        }
        // Process the result.
        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHED_SUCCESSFULLY)
            if (productDetailsResult.productDetailsList.isNullOrEmpty()) {
                setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST)
            } else {
                productDetailsResult.productDetailsList?.forEach {
                    val item = ProductDetail()
                    item.productId = it.productId
                    item.price = it.oneTimePurchaseOfferDetails?.formattedPrice.toString().removeSuffix(".00")
                    item.currencyCode = it.oneTimePurchaseOfferDetails?.priceCurrencyCode.toString()
                    item.productType = ProductType.INAPP
                    item.priceAmountMicros = it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L

                    _productDetailList.add(item)
                    _productDetailsLiveData.postValue(productDetailList)
                }
                dataProviderInApp.setProductDetailsList(productDetailsResult.productDetailsList!!)
                setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_AVAILABLE)
            }
        } else {
            setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING_FAILED)
        }
    }

    private fun queryForAvailableSubProducts() = CoroutineScope(Dispatchers.Main).launch {
        setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_FETCHING)
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(dataProviderSub.getProductList()).build())
        }
        // Process the result.
        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_FETCHED_SUCCESSFULLY)
            if (productDetailsResult.productDetailsList.isNullOrEmpty()) {
                setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST)
            } else {
                productDetailsResult.productDetailsList?.forEach { productDetails ->
                    if (!productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
                        val purchaseList = productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList

                        val item = ProductDetail()
                        item.productId = productDetails.productId

                        purchaseList.forEach { temp ->
                            if (temp.priceAmountMicros == 0L) {
                                item.freeTrial = true
                                item.freeTrialPeriod = when (temp.billingPeriod) {
                                    "P3D" -> 3
                                    "P5D" -> 5
                                    "P7D" -> 7
                                    "P1M" -> 30
                                    else -> 1
                                }
                            } else {
                                item.currencyCode = temp.priceCurrencyCode
                                item.price = temp.formattedPrice.removeSuffix(".00")
                                item.priceAmountMicros = temp.priceAmountMicros
                            }
                        }
                        _productDetailList.add(item)
                    }
                }
                _productDetailsLiveData.postValue(productDetailList)
                dataProviderSub.setProductDetailsList(productDetailsResult.productDetailsList!!)
                setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_AVAILABLE)
            }
        } else {
            setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_FETCHING_FAILED)
        }
    }


    /* --------------------------------------------------- Make Purchase  --------------------------------------------------- */

    protected fun purchaseInApp(activity: Activity?, onPurchaseListener: OnPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener
        if (checkValidationsInApp(activity)) return

        dataProviderInApp.getProductDetail()?.let { productDetail ->
            val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetail).build())
            val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

            // Launch the billing flow
            val billingResult = billingClient.launchBillingFlow(activity!!, billingFlowParams)

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY)
                BillingClient.BillingResponseCode.USER_CANCELED -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED)
                else -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            }
        } ?: run {
            setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST)
        }
    }

    private fun checkValidationsInApp(activity: Activity?): Boolean {
        if (activity == null) {
            setBillingState(BillingState.ACTIVITY_REFERENCE_NOT_FOUND)
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.EMPTY_PRODUCT_ID_LIST) {
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.NO_INTERNET_CONNECTION) {
            if (isInternetConnected && onConnectionListener != null) {
                startBillingConnection(productIdsList = dataProviderInApp.getProductIdsList(), onConnectionListener!!)
                return true
            }
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }


        if (getBillingState() == BillingState.CONNECTION_FAILED || getBillingState() == BillingState.CONNECTION_DISCONNECTED || getBillingState() == BillingState.CONNECTION_ESTABLISHING) {
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING || getBillingState() == BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING_FAILED) {
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (dataProviderInApp.getProductDetail() == null) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST)
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST) {
            onPurchaseListener?.onPurchaseResult(false, BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST.message)
            return true
        }

        dataProviderInApp.getProductIdsList().forEach { id ->
            dataProviderInApp.getProductDetailsList().forEach { productDetails ->
                if (id != productDetails.productId) {
                    setBillingState(BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_FOUND)
                    onPurchaseListener?.onPurchaseResult(false, BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_FOUND.message)
                    return true
                }
            }
        }

        if (billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).responseCode != BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.FEATURE_NOT_SUPPORTED)
            return true
        }
        return false
    }

    protected fun purchaseSub(activity: Activity?, subscriptionPlans: String, onPurchaseListener: OnPurchaseListener) {
        Log.d(TAG, "purchaseSub: in")
        if (checkValidationsSub(activity)) return

        this.onPurchaseListener = onPurchaseListener

        Log.d(TAG, "purchaseSub: Starting: ${dataProviderSub.getProductDetailsList()}")

        var prodDetails: ProductDetails? = null

        dataProviderSub.getProductDetailsList().forEach { productDetails ->
            if (productDetails.productId == SubscriptionProductIds.basicProductWeekly && subscriptionPlans == SubscriptionPlans.basicPlanWeekly) {
                prodDetails = productDetails
                return@forEach
            } else if (productDetails.productId == SubscriptionProductIds.basicProductFourWeeks && subscriptionPlans == SubscriptionPlans.basicPlanFourWeeks) {
                prodDetails = productDetails
                return@forEach
            } else if (productDetails.productId == SubscriptionProductIds.basicProductMonthly && subscriptionPlans == SubscriptionPlans.basicPlanMonthly) {
                prodDetails = productDetails
                return@forEach
            } else if (productDetails.productId == SubscriptionProductIds.basicProductQuarterly && subscriptionPlans == SubscriptionPlans.basicPlanQuarterly) {
                prodDetails = productDetails
                return@forEach
            } else if (productDetails.productId == SubscriptionProductIds.basicProductSemiYearly && subscriptionPlans == SubscriptionPlans.basicPlanSemiYearly) {
                prodDetails = productDetails
                return@forEach
            } else if (productDetails.productId == SubscriptionProductIds.basicProductYearly && subscriptionPlans == SubscriptionPlans.basicPlanYearly) {
                prodDetails = productDetails
                return@forEach
            }
        }

        Log.d(TAG, "purchaseSub: prodDetails : $prodDetails")

        if (prodDetails == null) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_NOT_FOUND)
            return
        }

        // Retrieve all offers the user is eligible for.
        val offers = prodDetails!!.subscriptionOfferDetails?.let {
            retrieveEligibleOffers(offerDetails = it, tag = subscriptionPlans)
        }

        //  Get the offer id token of the lowest priced offer.
        val offerToken = offers?.let { leastPricedOfferToken(it) }

        offerToken?.let { token ->
            val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(prodDetails!!).setOfferToken(token).build())
            val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

            // Launch the billing flow
            val billingResult = billingClient.launchBillingFlow(activity!!, billingFlowParams)

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY)
                BillingClient.BillingResponseCode.USER_CANCELED -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED)
                else -> setBillingState(BillingState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            }
        }
    }

    private fun checkValidationsSub(activity: Activity?): Boolean {
        if (activity == null) {
            setBillingState(BillingState.ACTIVITY_REFERENCE_NOT_FOUND)
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.NO_INTERNET_CONNECTION) {
            if (isInternetConnected && onConnectionListener != null) {
                startBillingConnection(productIdsList = dataProviderInApp.getProductIdsList(), onConnectionListener!!)
                return true
            }
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONNECTION_FAILED || getBillingState() == BillingState.CONNECTION_DISCONNECTED || getBillingState() == BillingState.CONNECTION_ESTABLISHING) {
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_SUB_FETCHING || getBillingState() == BillingState.CONSOLE_PRODUCTS_SUB_FETCHING_FAILED) {
            onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
            return true
        }

        if (getBillingState() == BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST) {
            onPurchaseListener?.onPurchaseResult(false, BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST.message)
            return true
        }

        if (dataProviderSub.getProductDetailsList().isEmpty()) {
            setBillingState(BillingState.CONSOLE_PRODUCTS_SUB_NOT_FOUND)
            onPurchaseListener?.onPurchaseResult(false, BillingState.CONSOLE_PRODUCTS_SUB_NOT_FOUND.message)
            return true
        }

        if (billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).responseCode != BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.FEATURE_NOT_SUPPORTED)
            return true
        }
        return false
    }

    /**
     * Retrieves all eligible base plans and offers using tags from ProductDetails.
     *
     * @param offerDetails offerDetails from a ProductDetails returned by the library.
     * @param tag string representing tags associated with offers and base plans.
     *
     * @return the eligible offers and base plans in a list.
     *
     */
    private fun retrieveEligibleOffers(offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>, tag: String): List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }
        return eligibleOffers
    }

    /**
     * Calculates the lowest priced offer amongst all eligible offers.
     * In this implementation the lowest price of all offers' pricing phases is returned.
     * It's possible the logic can be implemented differently.
     * For example, the lowest average price in terms of month could be returned instead.
     *
     * @param offerDetails List of of eligible offers and base plans.
     *
     * @return the offer id token of the lowest priced offer.
     *
     */
    private fun leastPricedOfferToken(offerDetails: List<ProductDetails.SubscriptionOfferDetails>): String {
        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        if (offerDetails.isNotEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    if (price.priceAmountMicros < lowestPrice) {
                        lowestPrice = price.priceAmountMicros.toInt()
                        leastPricedOffer = offer
                        offerToken = leastPricedOffer.offerToken
                    }
                }
            }
        }
        return offerToken
    }

    /* --------------------------------------------------- Purchase Response  --------------------------------------------------- */

    private val purchasesUpdatedListener: PurchasesUpdatedListener = PurchasesUpdatedListener { billingResult: BillingResult, purchaseMutableList: MutableList<Purchase>? ->
        Log.d(TAG, "purchasesUpdatedListener: $purchaseMutableList")

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                setBillingState(BillingState.PURCHASED_SUCCESSFULLY)
                handlePurchase(purchaseMutableList)
                return@PurchasesUpdatedListener
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {}
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {}
            BillingClient.BillingResponseCode.ERROR -> setBillingState(BillingState.PURCHASING_ERROR)
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {}
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                setBillingState(BillingState.PURCHASING_ALREADY_OWNED)
                onPurchaseListener?.onPurchaseResult(true, BillingState.PURCHASING_ALREADY_OWNED.message)
                return@PurchasesUpdatedListener
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {}
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {}
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {}
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {}
            BillingClient.BillingResponseCode.USER_CANCELED -> setBillingState(BillingState.PURCHASING_USER_CANCELLED)
        }
        onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
    }

    private fun handlePurchase(purchases: MutableList<Purchase>?) = CoroutineScope(Dispatchers.Main).launch {
        purchases?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (purchase.isAcknowledged) {
                    setBillingState(BillingState.PURCHASED_SUCCESSFULLY)
                    onPurchaseListener?.onPurchaseResult(true, BillingState.PURCHASED_SUCCESSFULLY.message)
                } else {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener)
                    }
                }
                return@launch
            } else {
                setBillingState(BillingState.PURCHASING_FAILURE)
            }
        } ?: kotlin.run {
            setBillingState(BillingState.PURCHASING_USER_CANCELLED)
        }
        onPurchaseListener?.onPurchaseResult(false, getBillingState().message)
    }

    private val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
            setBillingState(BillingState.PURCHASED_SUCCESSFULLY)
            CoroutineScope(Dispatchers.Main).launch {
                onPurchaseListener?.onPurchaseResult(true, BillingState.PURCHASED_SUCCESSFULLY.message)
            }
            Log.d(TAG, "acknowledgePurchaseResponseListener: Acknowledged successfully")
        } else {
            Log.d(TAG, "acknowledgePurchaseResponseListener: Acknowledgment failure")
        }
    }

    /* ------------------------------------- Internet Connection ------------------------------------- */

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val isInternetConnected: Boolean
        get() {
            try {
                val network = connectivityManager.activeNetwork ?: return false
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } catch (ex: Exception) {
                return false
            }
        }

    companion object {
        const val TAG = "BillingManager"
    }
}