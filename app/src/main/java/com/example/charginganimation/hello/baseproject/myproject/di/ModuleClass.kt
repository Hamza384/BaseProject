package com.example.charginganimation.hello.baseproject.myproject.di

import android.app.Application
import com.example.charginganimation.hello.baseproject.myproject.util.preference.SharedPref
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val sharedPrefModule = module {

    single {
        SharedPref(androidContext().getSharedPreferences("SharedPref", Application.MODE_PRIVATE))
    }

}

val modulesList = listOf(sharedPrefModule)