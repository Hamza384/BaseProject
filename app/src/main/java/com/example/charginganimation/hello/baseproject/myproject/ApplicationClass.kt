package com.example.charginganimation.hello.baseproject.myproject

import android.app.Application
import com.example.charginganimation.hello.baseproject.myproject.di.modulesList
import com.example.charginganimation.hello.baseproject.myproject.util.ad.AppOpenManager
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ApplicationClass : Application() {

    private lateinit var appOpenManager: AppOpenManager

    override fun onCreate() {
        super.onCreate()
        //init Ad
        initAd()
        //init Koin
        initKoin()


    }

    private fun initKoin() {
        startKoin {
            androidContext(this@ApplicationClass)
            modules(modulesList)
        }
    }

    private fun initAd() {
        appOpenManager = AppOpenManager(this, this)
        MobileAds.initialize(this)
    }

}