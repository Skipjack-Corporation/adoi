package com.skipjack.adoi.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

public class AppUtility {

    /**
     * Checks if any field is empty
     * */
    public static boolean checkIfEmpty(EditText... editTexts){
        for (EditText editText: editTexts){
            if (editText.getText().toString().replace(" ","").isEmpty()){
                return true;
            }
        }
        return false;
    }
    /**
     * Toast implementation
     * @param message - the message
     * */
    public static void toast(Context context,String message){
        Toast toast  = Toast.makeText(context,message,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    /**
     * Checks network connection
     * */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if email is valid
     * */
    public static boolean isEmailValid(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public static boolean isEmailValid(EditText... editTexts) {
        for (EditText editText: editTexts){
            if (!isEmailValid(editText.getText().toString())){
                return false;
            }
        }
        return true;
    }
}
