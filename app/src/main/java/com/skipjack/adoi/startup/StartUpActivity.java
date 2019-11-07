package com.skipjack.adoi.startup;


import android.content.Intent;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;

import support.skipjack.adoi.local_storage.AppSharedPreference;

import com.skipjack.adoi.login.LoginActivity;
import com.skipjack.adoi.main.MainActivity;

public class StartUpActivity extends BaseAppCompatActivity {


    @Override
    public int getLayoutResource() {
        return R.layout.activity_start_up;
    }

    @Override
    public void onCreate() {

        if (AppSharedPreference.get().hasLoginCredentials()){
            finishAffinity();
            startActivity(new Intent(StartUpActivity.this, MainActivity.class));
        }else{
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }



    }

    @Override
    protected void onDestroy() {

//        if (AppSharedPreference.get().hasLoginCredentials()){
//            unregisterReceiver(broadcastReceiver);
//        }
        super.onDestroy();
    }

//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            finishAffinity();
//            startActivity(new Intent(StartUpActivity.this, MainActivity.class));
//        }
//    };

}
