package com.skipjack.adoi.startup;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.database.AppSharedPreference;
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
        startActivity(new Intent(this, MainActivity.class));
        }else{
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }


    }
}
