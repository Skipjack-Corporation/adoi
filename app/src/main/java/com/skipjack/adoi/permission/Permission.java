package com.skipjack.adoi.permission;

import android.Manifest;

public enum Permission {
    PERMISSION_CAMERA(Manifest.permission.CAMERA),
    PERMISSION_RECORD_AUDIO(Manifest.permission.RECORD_AUDIO),
    PERMISSION_READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    PERMISSION_WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private String permission;
    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
