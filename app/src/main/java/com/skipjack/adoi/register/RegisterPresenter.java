package com.skipjack.adoi.register;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.skipjack.adoi.database.AppSharedPreference;
import mx.skipjack.service.MatrixService;
import mx.skipjack.service.MatrixCallback;
import mx.skipjack.service.MxCredentialManager;

import org.matrix.androidsdk.rest.model.login.Credentials;

public class RegisterPresenter extends MatrixCallback<Credentials> {

    private RegisterView view;
    private MxCredentialManager credentialManager;
    public RegisterPresenter(@NonNull RegisterView view) {
        this.view = view;
        credentialManager = new MxCredentialManager(this);
    }

    public void onRegister(String username,String email, String password){
        view.onStartProgress();
        credentialManager.register(username,email,password);
    }

    @Override
    public void onAPISuccess(Credentials data) {
        AppSharedPreference.get().saveLoginCredential(data);

        view.onStopProgress();
        view.onRegisterSuccess();
    }

    @Override
    public void onAPIFailure(String errorMessage) {
        view.onStopProgress();
        view.onRegisterError(errorMessage);
    }

    @Override
    public void onAPIMxError(int status, String errorcode, String errorBody) {
        view.onStopProgress();
        view.onRegisterError(errorBody);
    }
}
