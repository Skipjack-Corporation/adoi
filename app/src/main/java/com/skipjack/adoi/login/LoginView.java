package com.skipjack.adoi.login;

import org.matrix.androidsdk.rest.model.login.Credentials;

public interface LoginView {
    void onStartProgress();
    void onStopProgress();

    void onLoginSuccess(Credentials credentials);
    void onLoginError(String message);
}
