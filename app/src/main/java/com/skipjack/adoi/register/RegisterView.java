package com.skipjack.adoi.register;

public interface RegisterView {
    void onStartProgress();
    void onStopProgress();


    void onRegisterSuccess();
    void onRegisterError(String message);
}
