package com.dekidea.tuneurl.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.dekidea.tuneurl.R;

import static android.content.Context.POWER_SERVICE;

public class Permissions implements Constants {

    public static boolean hasAllPermissions(Context context) {

        return (hasPermission(context, Manifest.permission.RECORD_AUDIO) &&
                hasPermission(context, Manifest.permission.WAKE_LOCK) &&
                hasPermission(context, Manifest.permission.INTERNET) &&
                hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) &&
                hasPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED) &&
                hasPermission(context, Manifest.permission.READ_PHONE_STATE) &&
                hasPermission(context, Manifest.permission.CALL_PHONE) &&
                hasPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS));
    }


    @SuppressLint("NewApi")
    public static boolean hasPermission(Context context, String perm) {

        return(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, perm));
    }


    @SuppressLint("NewApi")
    public static String[] getNeededPermissions(Context context) {

        String[] needed_permissions = null;

        if(android.os.Build.VERSION.SDK_INT >= 23){

            String[] all_permissions = new String[8];

            int count = all_permissions.length;

            if(!Permissions.hasPermission(context, Manifest.permission.RECORD_AUDIO)){

                all_permissions[0] = Manifest.permission.RECORD_AUDIO;
            }
            else {

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.WAKE_LOCK)){

                all_permissions[1] = Manifest.permission.WAKE_LOCK;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.INTERNET)){

                all_permissions[2] = Manifest.permission.INTERNET;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)){

                all_permissions[3] = Manifest.permission.ACCESS_NETWORK_STATE;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED)){

                all_permissions[4] = Manifest.permission.RECEIVE_BOOT_COMPLETED;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.READ_PHONE_STATE)){

                all_permissions[5] = Manifest.permission.READ_PHONE_STATE;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.CALL_PHONE)){

                all_permissions[6] = Manifest.permission.CALL_PHONE;
            }
            else{

                count = count - 1;
            }


            if(!Permissions.hasPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)){

                all_permissions[7] = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
            }
            else{

                count = count - 1;
            }

            if(count > 0){

                needed_permissions = new String[count];
                int index = 0;

                for(int i=0; i<all_permissions.length; i++){

                    if(all_permissions[i] != null){

                        needed_permissions[index] = all_permissions[i];

                        index = index + 1;
                    }
                }
            }
        }

        return needed_permissions;
    }


    @SuppressLint("NewApi")
    public static String[] getNeededPermissionsLabels(Context context) {

        String[] permissions = new String[8];

        if(android.os.Build.VERSION.SDK_INT >= 23){

            if(!Permissions.hasPermission(context, Manifest.permission.RECORD_AUDIO)){

                permissions[0] = context.getString(R.string.permissions_record_audio);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.WAKE_LOCK))){

                permissions[1] = context.getString(R.string.permissions_wake_lock);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.INTERNET))){

                permissions[2] = context.getString(R.string.permissions_internet);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE))){

                permissions[3] = context.getString(R.string.permissions_access_network_state);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED))){

                permissions[4] = context.getString(R.string.permissions_boot_completed);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.READ_PHONE_STATE)&&
                    Permissions.hasPermission(context, Manifest.permission.CALL_PHONE))){

                permissions[5] = context.getString(R.string.permissions_call_phone);
            }

            if(!(Permissions.hasPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))){

                permissions[6] = context.getString(R.string.permissions_ignore_battery_optimizations);
            }

            if(!android.provider.Settings.canDrawOverlays(context)){

                permissions[7] = context.getString(R.string.permissions_system_alert_window);
            }
        }

        return permissions;
    }


    @SuppressLint("NewApi")
    public static boolean needsIgnoreBatteryOptimizations(Context context){

        boolean needs = false;

        try {

            if (Build.VERSION.SDK_INT >= 23) {

                PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(POWER_SERVICE);
                String packageName = context.getApplicationContext().getPackageName();
                needs = pm.isIgnoringBatteryOptimizations(packageName);
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }

        return needs;
    }


    @SuppressLint("NewApi")
    public static void requestIgnoreBatteryOptimizations(Context context){

        try {

            if(Build.VERSION.SDK_INT >= 23) {

                String packageName = context.getApplicationContext().getPackageName();
                PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {

                    Intent intent = new Intent();
                    intent.setAction("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
                    intent.setData(Uri.parse("package:" + packageName));
                    context.getApplicationContext().startActivity(intent);
                }
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
}
