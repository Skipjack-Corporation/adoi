package com.skipjack.adoi.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    public static final int REQUESTCODE_ASK_PERMISSION = 568;
    public static boolean checkPermission(Activity activity, Permission... permissions){
        for (Permission permission: permissions){
            if (ContextCompat.checkSelfPermission(activity, permission.getPermission())
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                return false;
            }
        }

        return true;
    }

    public static void askPermissions(Activity activity, Permission... permissions){
        String[] permissionArr = new String[permissions.length];
        int i = 0;
        for (Permission permission: permissions){
            permissionArr[i] = permission.getPermission();
            i++;
        }
        ActivityCompat.requestPermissions(activity, permissionArr,
                REQUESTCODE_ASK_PERMISSION);
    }


}
