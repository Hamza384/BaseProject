package com.example.charginganimation.hello.baseproject.myproject.util.preference

import android.content.SharedPreferences


private const val IS_FIRST_TIME = "IS_FIRST_TIME"
private const val SHOW_BATTERY = "SHOW_BATTERY"


class SharedPref(private val sharedPreferences: SharedPreferences) {


    var isFirstTimeInstall: Boolean
        get() = sharedPreferences.getBoolean(IS_FIRST_TIME, false)
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(IS_FIRST_TIME, value)
                apply()
            }
        }


    var showBatteryPercentage: Boolean
        get() = sharedPreferences.getBoolean(SHOW_BATTERY, false)
        set(value) {
            sharedPreferences.edit().apply {
                putBoolean(SHOW_BATTERY, value)
                apply()
            }
        }


}