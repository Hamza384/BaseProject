package com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces



interface OnConnectionListener {

    fun onConnectionResult(isSuccess: Boolean, message: String) {}
    fun onOldPurchaseResult(isPurchased: Boolean)

}