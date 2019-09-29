package com.skipjack.adoi.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;


import androidx.core.content.ContextCompat;

import com.skipjack.adoi.database.AppSharedPreference;

import mx.skipjack.service.EventStreamService;
import mx.skipjack.service.MatrixService;


public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();

        MatrixService.get().init(getApplicationContext());
        AppSharedPreference.get().init(this);

        registerActivityLifecycleCallbacks(this);

        if (AppSharedPreference.get().hasLoginCredentials()){
            ContextCompat.startForegroundService(this,
                    new Intent(this, EventStreamService.class));

        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
