package com.example.charginganimation.hello.baseproject.myproject.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.charginganimation.hello.baseproject.myproject.BuildConfig
import com.example.charginganimation.hello.baseproject.myproject.databinding.ActivityMainBinding
import com.example.charginganimation.hello.baseproject.myproject.util.billing.BillingManager
import com.example.charginganimation.hello.baseproject.myproject.util.billing.constants.SubscriptionProductIds
import com.example.charginganimation.hello.baseproject.myproject.util.billing.dataClasses.ProductDetail
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnConnectionListener
import com.example.charginganimation.hello.baseproject.myproject.util.billing.interfaces.OnPurchaseListener
import com.example.charginganimation.hello.baseproject.myproject.util.billing.status.State
import com.example.charginganimation.hello.baseproject.myproject.util.permission.PermissionManager


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val permissionManager = PermissionManager
    private val billingManager by lazy { BillingManager(this) }
    private val productId: String = "Paste your original Product ID"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }







    //to request storage related permission

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionManager.requestPermissions(
            this@MainActivity,
            permission,
            object : PermissionManager.PermissionCallback {
                override fun onPermissionGranted() {
                    // Permission granted, handle the logic
                    // e.g., Load player and audio files
                    //loadVideoDirectories()
                }

                override fun onPermissionDenied() {
                    // Permission denied, handle the logic
                    // e.g., Show an error message or disable functionality
                }
            })
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    private fun initObserver() {
        State.billingState.observe(this) {
            Log.d("BillingManager", "initObserver: $it")
            //tvTitle.text = it.toString()
        }

        //Observe Your Products
        billingManager.productDetailsLiveData.observe(this) { list ->
            var month = 0L
            var year = 0L
            list.forEach { productDetail: ProductDetail ->
                Log.d("TAG", "initObservers: $productDetail")
                when (productDetail.productId) {
                    SubscriptionProductIds.basicProductMonthly -> {
                        //binding.mtvOfferPrice1.text = productDetail.price
                        month = productDetail.priceAmountMicros / 1000000
                    }

                    SubscriptionProductIds.basicProductYearly -> {
                        //binding.mtvOfferPrice2.text = productDetail.price
                        year = productDetail.priceAmountMicros / 1000000
                    }

                    SubscriptionProductIds.basicProductSemiYearly -> {
                        //binding.mtvOfferPrice3Premium.text = productDetail.price
                    }

                    productId -> {
                        //binding.mtvOfferPrice3Premium.text = productDetail.price
                    }
                }
            }
            // Best Offer
            if (month == 0L || year == 0L) return@observe
            val result = 100 - (year * 100 / (12 * month))
            val text = "Save $result%"
            //binding.mtvBestOffer.text = text

            val perMonth = (year / 12L).toString()
            //binding.mtvOffer.text = perMonth
        }
    }

    private fun initBilling() {
        billingManager.setCheckForSubscription(true)
        if (BuildConfig.DEBUG) {
            billingManager.startConnection(
                billingManager.getDebugProductIDList(),
                object : OnConnectionListener {
                    override fun onConnectionResult(isSuccess: Boolean, message: String) {
                        showMessage(message)
                        Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                    }

                    override fun onOldPurchaseResult(isPurchased: Boolean) {
                        // Update your shared-preferences here!
                        Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                    }
                })
        } else {
            billingManager.startConnection(listOf(productId), object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        }
    }


    private fun onPurchaseClick() {
        // In-App
        billingManager.makeInAppPurchase(this, object : OnPurchaseListener {
            override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                showMessage(message)
            }
        })

        // Subscription
        /*billingManager.makeSubPurchase(
            this,
            SubscriptionPlans.basicPlanMonthly,
            object : OnPurchaseListener {
                override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                    showMessage(message)
                }
            })*/
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }





}