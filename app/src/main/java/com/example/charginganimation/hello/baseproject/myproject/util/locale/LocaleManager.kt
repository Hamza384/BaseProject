package com.example.charginganimation.hello.baseproject.myproject.util.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    private var currentLocale: Locale = Locale.getDefault()

    fun setLocale(context: Context, languageCode: String) {
        val newLocale = Locale(languageCode)
        Locale.setDefault(newLocale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(newLocale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        currentLocale = newLocale
    }

    fun getCurrentLocale(): Locale {
        return currentLocale
    }
}