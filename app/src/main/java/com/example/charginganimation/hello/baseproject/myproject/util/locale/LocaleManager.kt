package com.example.charginganimation.hello.baseproject.myproject.util.locale

import android.content.Context
import android.content.res.Configuration
import com.example.charginganimation.hello.baseproject.myproject.di.ComponentClass
import java.util.Locale

object LocaleManager {
    private var currentLocale: Locale = Locale.getDefault()

    private val diComponent by lazy {
        ComponentClass()
    }


    fun setLocale(context: Context, languageCode: String) {
        val newLocale = Locale(languageCode)
        diComponent.sharePrefUtil.saveLocale = languageCode
        Locale.setDefault(newLocale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(newLocale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        currentLocale = newLocale
    }

    fun getCurrentLocale(): String {
        return diComponent.sharePrefUtil.saveLocale
        //return currentLocale
    }
}