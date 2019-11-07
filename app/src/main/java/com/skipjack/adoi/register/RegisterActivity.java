package com.skipjack.adoi.register;

import android.content.Intent;
import android.widget.EditText;

import com.skipjack.adoi.R;
import com.skipjack.adoi.base.BaseAppCompatActivity;

import com.skipjack.adoi.utility.AppUtility;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterActivity extends BaseAppCompatActivity implements RegisterView {

    @BindView(R.id.editUsername)EditText editUsername;
    @BindView(R.id.editEmail)EditText editEmail;
    @BindView(R.id.editConfirmPassword)EditText editConfirmPassword;
    @BindView(R.id.editPassword)EditText editPassword;

    RegisterPresenter registerPresenter;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_register;
    }

    @Override
    public void onCreate() {
        registerPresenter = new RegisterPresenter(this);
    }

    @OnClick(R.id.btnRegister)
    public void onRegisterClick(){
        startActivity(new Intent(this, RegisterProfileActivity.class));
//        if (AppUtility.checkIfEmpty(editEmail,editUsername,editConfirmPassword,editPassword)){
//            AppUtility.toast(this,getString(R.string.empty_field_message));
//        }else if (!(editPassword.getText().toString().equals(editConfirmPassword.getText().toString()))){
//            AppUtility.toast(this,getString(R.string.password_mismatch));
//        }else if (!AppUtility.isNetworkConnected(this)){
//            AppUtility.toast(this,getString(R.string.network_fail_message));
//        }else {
//            registerPresenter.onRegister(editUsername.getText().toString(),
//                    editEmail.getText().toString(),
//                    editPassword.getText().toString());
//        }
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
    public void onRegisterSuccess() {
//        AppUtility.toast(this,getString(R.string.Success));
        startActivity(new Intent(this, RegisterProfileActivity.class));
    }

    @Override
    public void onRegisterError(String message) {
        AppUtility.toast(this,message);
    }
}
