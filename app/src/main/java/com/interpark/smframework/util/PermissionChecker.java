package com.interpark.smframework.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionChecker {
    public static final int PERMISSION_ALL=0;

    public static boolean checkPermission(Context context, String[] permissions){
        for(String permission:permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static void seekPermission(Activity activity, String[] permissions, int PermissionCode){
        ActivityCompat.requestPermissions(activity, permissions, PermissionCode);
    }
}
