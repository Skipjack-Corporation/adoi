package com.skipjack.adoi.login;


import androidx.annotation.NonNull;

import support.skipjack.adoi.matrix.MatrixCallback;
import com.skipjack.adoi._repository.CredentialRepository;

import support.skipjack.adoi.local_storage.AppSharedPreference;

import org.matrix.androidsdk.rest.model.login.Credentials;

public class LoginPresenter extends MatrixCallback<Credentials> {
    private LoginView view;
    private CredentialRepository credentialRepository;

    public LoginPresenter(@NonNull LoginView view) {
        this.view = view;
        credentialRepository = new CredentialRepository(this);

    }

    public void onLogin(boolean isEmail, boolean isPhone, String username, String password){
        view.onStartProgress();
        if (isEmail)
            credentialRepository.loginUsingEmail(username,password);
        else if (isPhone)
            credentialRepository.loginUsingPhone(username,password);
        else
            credentialRepository.loginUsingUsername(username,password);
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

}
