package com.example.charginganimation.hello.baseproject.myproject.util.billing.dataClasses

import com.example.charginganimation.hello.baseproject.myproject.util.billing.enums.ProductType

/**
 * @property productId: Unique ID (Console's ID
 * @property price: e.g. Rs 750.00
 * @property currencyCode: e.g. USD, PKR, etc
 * @property freeTrialPeriod: e.g. 3, 5, 7
 * @property priceAmountMicros: e.g. 750000000
 * @property freeTrial: isAvailable or not
 */

data class ProductDetail(
    var productId: String,
    var price: String,
    var currencyCode: String,
    var freeTrialPeriod: Int,
    var priceAmountMicros: Long = 0,
    var freeTrial: Boolean = false,
    var productType: ProductType = ProductType.SUBS
) {
    constructor() : this(productId = "", price = "", currencyCode = "", 0, 0, false)
}