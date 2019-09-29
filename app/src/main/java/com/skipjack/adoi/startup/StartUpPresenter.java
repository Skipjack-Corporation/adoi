package com.skipjack.adoi.startup;

import androidx.annotation.NonNull;

import com.skipjack.adoi.database.AppSharedPreference;

import mx.skipjack.service.MxSessionManager;

public class StartUpPresenter implements MxSessionManager.Callback {
    private StartUpView view;
    private MxSessionManager sessionManager;
    public StartUpPresenter(@NonNull StartUpView view) {
        this.view = view;
        sessionManager = new MxSessionManager(this);
    }

    public void startSession(){
        view.onStartProgress();
        sessionManager.startSession();
    }


    @Override
    public void onStartingNow() {
        view.onStopProgress();
        view.onStartMainScreen();
    }
}
