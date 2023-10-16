plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.charginganimation.hello.baseproject.myproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.charginganimation.hello.baseproject.myproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "inter_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "native_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "open_id", "ca-app-pub-3940256099942544/3419835294")
        }


        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "inter_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "native_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "open_id", "ca-app-pub-3940256099942544/3419835294")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.9.0")
    kapt("com.github.bumptech.glide:compiler:4.9.0")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    //ads
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-process:2.3.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.3.1")
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    //Koin
    implementation("io.insert-koin:koin-android:3.4.0")
    //ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:6.0.1")



}