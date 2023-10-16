package com.example.charginganimation.hello.baseproject.myproject.util.ad;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;


import com.example.charginganimation.hello.baseproject.myproject.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;


public class AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static final String LOG_TAG = "AppOpenManager";
    private static boolean isShowingAd = false;
    private final Application myApplication;
    private final Context context;
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private Activity currentActivity;


    public AppOpenManager(Application myApplication, Context context) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.context = context;

    }


    public void fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(AppOpenAd ad) {
                AppOpenManager.this.appOpenAd = ad;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
            }

        };
        AdRequest request = getAdRequest();
        AppOpenAd.load(myApplication, context.getResources().getString(R.string.open_id), request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }


    public void showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    AppOpenManager.this.appOpenAd = null;
                    isShowingAd = false;
                    fetchAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            };

            appOpenAd.show(currentActivity);

        } else {
            fetchAd();
        }
    }


    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }


    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;

    }


    @OnLifecycleEvent(ON_START)
    public void onStart() {
        showAdIfAvailable();
        Log.d(LOG_TAG, "onStart");
    }
}