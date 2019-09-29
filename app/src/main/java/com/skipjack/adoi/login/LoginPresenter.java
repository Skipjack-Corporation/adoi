package com.skipjack.adoi.login;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import mx.skipjack.service.MatrixCallback;
import mx.skipjack.service.MxCredentialManager;

import com.skipjack.adoi.database.AppSharedPreference;

import org.matrix.androidsdk.rest.model.login.Credentials;

public class LoginPresenter extends MatrixCallback<Credentials> {
    private LoginView view;
    private MxCredentialManager credentialManager;

    public LoginPresenter(@NonNull LoginView view) {
        this.view = view;
        credentialManager = new MxCredentialManager(this);

    }

    public void onLogin(boolean isEmail, boolean isPhone, String username, String password){
        view.onStartProgress();
        if (isEmail)
            credentialManager.loginUsingEmail(username,password);
        else if (isPhone)
            credentialManager.loginUsingPhone(username,password);
        else
            credentialManager.loginUsingUsername(username,password);
    }

    @Override
    public void onAPISuccess(Credentials data) {
        AppSharedPreference.get().saveLoginCredential(data);

        view.onStopProgress();
        view.onLoginSuccess(data);
    }

    @Override
    public void onAPIFailure(String errorMessage) {
        view.onStopProgress();
        view.onLoginError(errorMessage);
    }

    @Override
    public void onAPIMxError(int status, String errorcode, String errorBody) {
        view.onStopProgress();
        view.onLoginError(errorBody);
    }
}
