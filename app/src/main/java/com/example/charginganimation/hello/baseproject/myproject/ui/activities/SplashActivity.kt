package com.example.charginganimation.hello.baseproject.myproject.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.example.charginganimation.hello.baseproject.myproject.databinding.ActivitySplashBinding
import com.example.charginganimation.hello.baseproject.myproject.di.ComponentClass


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private val diComponent by lazy {
        ComponentClass()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {

        Handler(Looper.getMainLooper()).postDelayed(4000) {
            binding.apply {
                btnContinue.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }


        binding.btnContinue.setOnClickListener {
            if (diComponent.sharePrefUtil.isFirstTimeInstall) {
                //startActivity(Intent(this@SplashActivity, WelcomeFragment::class.java))
            } else {
                //startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
            }
            finish()
        }


    }
}