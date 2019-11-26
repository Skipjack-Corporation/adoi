package com.skipjack.adoi.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;


import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import support.skipjack.adoi.matrix.ssl_errorhandler.EventEmitter;
import support.skipjack.adoi.matrix.MatrixService;
import support.skipjack.adoi.local_storage.AppDatabase;
import support.skipjack.adoi.local_storage.AppSharedPreference;

import support.skipjack.adoi.matrix.service.MatrixEventService;


public class BaseApplication extends Application {

    private static Context context;
    private static Activity mCurrentActivity = null;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        BaseApplication.context = context;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public static void setCurrentActivity(Activity mCurrentActivity) {
        BaseApplication.mCurrentActivity = mCurrentActivity;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        context = this;

        MatrixService.get().init(getApplicationContext());

        AppDatabase.GET = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Adoi2-Database").build();

        AppSharedPreference.get().init(this);

        startStreamService();

    }
    //==============================================================================================================
    // cert management : store the active activities.
    //==============================================================================================================

    private static final EventEmitter<Activity> mOnActivityDestroyedListener = new EventEmitter<>();

    /**
     * @return the EventEmitter list.
     */
    public static EventEmitter<Activity> getOnActivityDestroyedListener() {
        return mOnActivityDestroyedListener;
    }

    public static void startStreamService(){
        if (AppSharedPreference.get().hasLoginCredentials()){
            Intent serviceIntent =  new Intent(context, MatrixEventService.class);
//            context.startService(serviceIntent);
            ContextCompat.startForegroundService(context, serviceIntent);

        }
    }
}
