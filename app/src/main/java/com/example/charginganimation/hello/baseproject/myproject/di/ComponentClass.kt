package com.example.charginganimation.hello.baseproject.myproject.di

import com.example.charginganimation.hello.baseproject.myproject.util.preference.SharedPref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ComponentClass : KoinComponent {

    val sharePrefUtil by inject<SharedPref>()

}