package com.skipjack.adoi.base;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skipjack.adoi.R;

import butterknife.ButterKnife;
import com.skipjack.adoi._repository.CallRespository;

public abstract class BaseAppCompatActivity extends AppCompatActivity {

    public abstract int getLayoutResource();
    public abstract void onCreate();

    public Bundle saveInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.saveInstance = savedInstanceState;
        setContentView(getLayoutResource());
        ButterKnife.bind(this);

        BaseApplication.setCurrentActivity(this);
        onCreate();
        CallRespository.get().isAppAlive = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallRespository.get().isAppAlive = false;
    }

    /**
     * BroadcastReceiver
     * */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    /**
     * Initialize and show progressdialog
     * */
    private Dialog progressdialog;

    public void showProgressDialog() {
        if (progressdialog == null) {
            progressdialog = new Dialog(this);
            progressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressdialog.setContentView(R.layout.layout_progressdialog);
            progressdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        progressdialog.setCancelable(false);
        progressdialog.show();
    }
    /**
     * hide progressdialog
     * */
    public void hideProgressDialog() {
        try {
            if (progressdialog.isShowing()) {
                progressdialog.dismiss();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
