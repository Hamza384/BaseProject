package com.example.charginganimation.hello.baseproject.myproject.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.charginganimation.hello.baseproject.myproject.R
import com.example.charginganimation.hello.baseproject.myproject.util.ad.AdUtil
import com.google.android.gms.ads.interstitial.InterstitialAd

class PrivacyActivity : AppCompatActivity() {


    private lateinit var checkBoxAgree: CheckBox
    private lateinit var btnAgree: Button
    private lateinit var ivBack: ImageView
    private lateinit var tvPrivacy: TextView
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var nativeUtils: AdUtil
    private var frameLayout: FrameLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        checkBoxAgree = findViewById(R.id.agreedChkbox)
        tvPrivacy = findViewById(R.id.privacyTv)
        btnAgree = findViewById(R.id.agreeBtn)
        initAd()
        checkBoxAgree.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (checkBoxAgree.isChecked) {
                btnAgree.visibility = View.VISIBLE
            } else {
                btnAgree.visibility = View.GONE
            }
        }
        btnAgree.setOnClickListener { openFirstActivity() }



        tvPrivacy.setOnClickListener {
            try {

                //Ad Privacy Link Here

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("")
                    )
                )
            } catch (e: Exception) {
                e.message
            }
        }

    }

    private fun initAd() {
        frameLayout = findViewById(R.id.container)
        nativeUtils = AdUtil()

    }

    private fun openFirstActivity() {
        //showInterstitial()
    }


    private fun newActivity() {
        /*startActivity(Intent(this@PrivacyActivity, SettingActivity::class.java))
        finish()*/
    }
}