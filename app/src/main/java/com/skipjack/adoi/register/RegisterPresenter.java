package com.skipjack.adoi.register;


import androidx.annotation.NonNull;

import support.skipjack.adoi.local_storage.AppSharedPreference;

import support.skipjack.adoi.matrix.MatrixCallback;
import com.skipjack.adoi._repository.CredentialRepository;

import org.matrix.androidsdk.rest.model.login.Credentials;

public class RegisterPresenter extends MatrixCallback<Credentials> {

    private RegisterView view;
    private CredentialRepository credentialRepository;
    public RegisterPresenter(@NonNull RegisterView view) {
        this.view = view;
        credentialRepository = new CredentialRepository(this);
    }

    public void onRegister(String username,String email, String password){
        view.onStartProgress();
        credentialRepository.register(username,email,password);
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
}
