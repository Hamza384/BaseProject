package com.example.charginganimation.hello.baseproject.myproject.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.charginganimation.hello.baseproject.myproject.databinding.ActivityLanguageBinding
import com.example.charginganimation.hello.baseproject.myproject.ui.adapter.LanguageAdapter
import com.example.charginganimation.hello.baseproject.myproject.util.data.DataItems.languageArray
import com.example.charginganimation.hello.baseproject.myproject.util.listener.OnLanguageSelected


class LanguageActivity : AppCompatActivity(), OnLanguageSelected {

    private val binding by lazy {
        ActivityLanguageBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setView()
    }

    private fun setView() {
        initAd()
        initAdapter()

    }

    private fun initAd() {
        //diComponent.adUtils.initNativeAd(this@LanguageActivity, R.layout.unified_native_ad, "large")
    }

    private fun initAdapter() {
        binding.rvLanguage.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = LanguageAdapter(
                this@LanguageActivity,
                languageArray,
                this@LanguageActivity
            )
        }

        binding.btnContinue.setOnClickListener {

        }


    }

    override fun onLanguageSelected(pos: Int) {
        val intent = Intent(this@LanguageActivity, MainActivity::class.java)
        intent.putExtra("ln_code", languageArray[pos].languageCode)
        startActivity(intent)
    }


}