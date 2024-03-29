package com.skipjack.adoi.login;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;
import com.skipjack.adoi.base.BaseApplication;
import com.skipjack.adoi.register.RegisterActivity;
import com.skipjack.adoi.startup.StartUpActivity;
import com.skipjack.adoi.utility.AppUtility;

import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.rest.model.login.Credentials;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseAppCompatActivity implements LoginView {

    @BindView(R.id.editUsername)
    EditText editUsername;
    @BindView(R.id.editPassword)
    EditText editPassword;

    LoginPresenter loginPresenter;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    public void onCreate() {
        loginPresenter = new LoginPresenter(this);
    }

    @OnClick(R.id.btnLogin)
    public void onLoginClick() {
        if (AppUtility.checkIfEmpty(editUsername, editPassword)) {
            AppUtility.toast(this, getString(R.string.empty_field_message));
        } else if (!AppUtility.isNetworkConnected(this)) {
            AppUtility.toast(this, getString(R.string.network_fail_message));
        } else {
            loginPresenter.onLogin(AppUtility.isEmailValid(editUsername.getText().toString()),
                    false, editUsername.getText().toString(), editPassword.getText().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btnMakeAccount)
    public void onMakeAccountClick() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.textForgotPassword)
    public void onForgotPasswordClick() {
    }

    @Override
    public void onStartProgress() {
        showProgressDialog();
    }

    @Override
    public void onStopProgress() {
        hideProgressDialog();
    }

    @Override
    public void onLoginSuccess(Credentials credentials) {
        BaseApplication.startStreamService();
        finishAffinity();
        startActivity(new Intent(this, StartUpActivity.class));
    }

    @Override
    public void onLoginError(String message) {
        AppUtility.toast(this, message);
    }
}
