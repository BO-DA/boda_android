package com.example.boda.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static boolean haveAllPermission(Context c , String[] s){
        boolean ispms = true;
        for (String pms : s ){
            if (ContextCompat.checkSelfPermission(c, pms) != PackageManager.PERMISSION_GRANTED  ) {
                ispms = false;
                break;
            }
        }
        return ispms;
    }

    public static boolean recheckPermission (Activity activity , String[] permissions){
        boolean isrpms = false;
        for (String s : permissions){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, s)){
                isrpms = true;
                break;
            }
        }
        return isrpms;
    }
}
