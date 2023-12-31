package com.example.charginganimation.hello.baseproject.myproject.util.billing.dataProvider

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.charginganimation.hello.baseproject.myproject.util.billing.constants.SubscriptionProductIds

internal class DataProviderSub {

    private var productDetailsList: List<ProductDetails> = listOf()

    /* ------------------------------------------------------ Product ID ------------------------------------------------------ */

    /**
     * @field productIdsList:   List of product Id's providing by the developer to check/retrieve-details if these products are existing in Google Play Console
     */

    val productIdsList = listOf(
        SubscriptionProductIds.basicProductWeekly,
        SubscriptionProductIds.basicProductFourWeeks,
        SubscriptionProductIds.basicProductMonthly,
        SubscriptionProductIds.basicProductQuarterly,
        SubscriptionProductIds.basicProductSemiYearly,
        SubscriptionProductIds.basicProductYearly,
        SubscriptionProductIds.basicProductLifeTime,
    )

    fun getProductList(): List<QueryProductDetailsParams.Product> {
        val arrayList = ArrayList<QueryProductDetailsParams.Product>()
        productIdsList.forEach {
            arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build())
        }
        return arrayList.toList()
    }

    /* ---------------------------------------------------- Product Details ---------------------------------------------------- */

    fun setProductDetailsList(productDetailsList: List<ProductDetails>) {
        this.productDetailsList = productDetailsList
    }

    fun getProductDetailsList(): List<ProductDetails> {
        return productDetailsList
    }
}