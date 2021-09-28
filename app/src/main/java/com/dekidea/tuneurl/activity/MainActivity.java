package com.dekidea.tuneurl.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.fragment.AboutFragment;
import com.dekidea.tuneurl.fragment.MainFragment;
import com.dekidea.tuneurl.fragment.SavedInfoFragment;
import com.dekidea.tuneurl.fragment.SettingsFragment;
import com.dekidea.tuneurl.service.MediaProjectionServer;
import com.dekidea.tuneurl.service.SoundListenerService;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.FileUtils;
import com.dekidea.tuneurl.util.Permissions;
import com.dekidea.tuneurl.util.Settings;

import java.util.ArrayList;

import dagger.android.AndroidInjection;

public class MainActivity extends FragmentActivity implements Constants {

    private static final String ROOT_URI = "content://com.android.externalstorage.documents/tree/primary%3A";
    private static final String APP_DIRECTORY = "TuneURL";

    private static final int PERMISSIONS_REQUEST = 1337;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 1338;
    private static final int IGNORE_BATTERY_OPTIMIZATIONS_REQUEST = 1339;
    private static final int SCREEN_CAPTURE_REQUEST = 1340;

    private static final int NUM_PAGES = 4;
    private static final int MAIN_PAGE_INDEX = 0;
    private static final int SAVED_INFO_PAGE_INDEX = 1;
    private static final int SETTINGS_PAGE_INDEX = 2;
    private static final int ABOUT_PAGE_INDEX = 3;

    private Context mContext;

    private int mRunningState = SETTING_RUNNING_STATE_STOPPED;

    private MainActivity.AppStatusReceiver mAppStatusReceiver;
    private IntentFilter mIntentFilter;

    private ArrayList<Fragment> mFragments;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private boolean mAutorunStarted;
    
    private boolean mPermissionsChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        mPermissionsChecked = false;

        configureDagger();

        FileUtils.installResources(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        Intent intent = getIntent();

        if(intent != null){

            Bundle bundle = intent.getExtras();

            if(bundle != null){

                mAutorunStarted = bundle.getBoolean(AUTORUN_STARTED);

                System.out.println("mAutorunStarted: " + mAutorunStarted);

                if(mAutorunStarted){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

                        setShowWhenLocked(true);
                        setTurnScreenOn(true);
                        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
                        keyguardManager.requestDismissKeyguard(this, null);
                    }

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                }
            }
        }


        mAppStatusReceiver = new MainActivity.AppStatusReceiver();
        mIntentFilter = new IntentFilter(MESSAGE_APP_STATUS_CHANGED);

        mFragments = initializeFragments();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }


    private void configureDagger() {

        AndroidInjection.inject(this);
    }


    @Override
    public void onResume() {

        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(!mPermissionsChecked){
                
                checkPermissions();
            }
            else{

                resume();
            }
        }
        else {

            resume();
        }

        registerReceiver(mAppStatusReceiver, mIntentFilter);
    }


    @Override
    public void onWindowFocusChanged (boolean hasFocus){

        super.onWindowFocusChanged(hasFocus);

        if(hasFocus){

            if(mAutorunStarted && mRunningState == SETTING_RUNNING_STATE_STOPPED){

                start();
            }
            else{

                displayStatus(mRunningState);
            }
        }
    }


    private void resume() {

        System.out.println("MainActivity.resume()");

        if (Permissions.needsIgnoreBatteryOptimizations(mContext)) {

            Permissions.requestIgnoreBatteryOptimizations(mContext);
        }
        else if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!android.provider.Settings.canDrawOverlays(mContext)) {

                System.out.println("MainActivity.resume(): alertNeedsOverlayPermission()");

                alertNeedsOverlayPermission();
            }
        }

        mRunningState = Settings.fetchIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

        FileUtils.initializeReferenceBenchmarkWaves(mContext);
    }


    @Override
    public void onPause() {

        super.onPause();

        unregisterReceiver(mAppStatusReceiver);
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    public void start() {

        System.out.println("MainActivity.start()");

        mAutorunStarted = false;

        Settings.startListeningService(mContext);

        mRunningState = SETTING_RUNNING_STATE_STARTED;

        displayStatus(mRunningState);
    }


    public void stop() {

        mRunningState = SETTING_RUNNING_STATE_STOPPED;

        Settings.updateIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

        Settings.stopListeningService(mContext);

        FileUtils.deleteCacheSoundFiles(mContext);

        mAutorunStarted = false;

        displayStatus(mRunningState);
    }


    private void displayStatus(int running_state) {

        ((MainFragment) mFragments.get(MAIN_PAGE_INDEX)).displayStatus(running_state);
    }


    private AlertDialog mNoNetworkConnectionAlert;

    public void showNoNetworkConnectionAlert() {

        System.out.println("showNoNetworkConnectionAlert()");

        if(mNoNetworkConnectionAlert == null) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialog);

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.alert_no_connection, null);

            TextView text_ok = (TextView) mainLayout.getChildAt(4);
            text_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mNoNetworkConnectionAlert.dismiss();

                    mNoNetworkConnectionAlert = null;

                    displayStatus(mRunningState);
                }
            });

            mNoNetworkConnectionAlert = alertBuilder.create();

            mNoNetworkConnectionAlert.setView(mainLayout);
            //mPermissionsDeniedAlert.setCancelable(false);
            mNoNetworkConnectionAlert.show();
        }
    }


    @Override
    public void onBackPressed() {

        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            if (mRunningState == SETTING_RUNNING_STATE_STARTED) {

                moveTaskToBack(true);
            }
            else {

                finish();
            }
        }
        else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    class AppStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {

                int message = intent.getIntExtra(MESSAGE, -1);

                if (message == MESSAGE_APP_STATUS_STARTED) {

                    mRunningState = SETTING_RUNNING_STATE_STARTED;
                } else if (message == MESSAGE_APP_STATUS_STOPPED) {

                    mRunningState = SETTING_RUNNING_STATE_STOPPED;
                }

                displayStatus(mRunningState);
            }
        }
    }


    @SuppressLint("NewApi")
    private void checkPermissions() {

        mPermissionsChecked = true;

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (Permissions.hasAllPermissions(mContext)) {

                resume();
            }
            else {

                alertNeedsPermissions();
            }
        }
    }


    @SuppressLint("NewApi")
    private void requestPermissions() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            String[] needed_permissions = Permissions.getNeededPermissions(mContext);

            if (needed_permissions != null) {

                requestPermissions(needed_permissions, PERMISSIONS_REQUEST);
            }
        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case PERMISSIONS_REQUEST:

                if (Permissions.hasAllPermissions(mContext)) {

                    FileUtils.installResources(this);

                    resume();
                }
                else {

                    alertPermissionsDenied();
                }

                break;
        }
    }


    public void alertNeedsPermissions() {

        /*

        String[] permissions = Permissions.getNeededPermissionsLabels(mContext);

        String message = "";

        for (int i = 0; i < permissions.length; i++) {

            if (permissions[i] != null) {

                message = message + permissions[i] + "\n";
            }
        }

        message = message + getString(R.string.alert_permissions_message);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);

        alertBuilder.setTitle(getString(R.string.alert_permissions_title));

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.alert_simple_message_view, null);

        TextView text = (TextView) mainLayout.getChildAt(0);
        text.setText(message);

        alertBuilder.setNegativeButton(mContext.getString(R.string.alert_permissions_deny), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                alertPermissionsDenied();
            }
        });

        alertBuilder.setPositiveButton(mContext.getString(R.string.alert_permissions_allow), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                requestPermissions();
            }
        });

        AlertDialog alert = alertBuilder.create();

        alert.setView(mainLayout);
        alert.setCancelable(false);
        alert.show();

         */

        requestPermissions();
    }


    private AlertDialog mOverlayPermissionAlert;

    private void alertNeedsOverlayPermission() {

        System.out.println("alertNeedsOverlayPermission()");

        if(mOverlayPermissionAlert == null) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialog);

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.alert_overlay_permission_view, null);

            String title = getString(R.string.alert_overlay_permission_title_1) +
                    " <b>" + getString(R.string.app_name) + "</b> " +
                    getString(R.string.alert_overlay_permission_title_2);

            TextView text_title = (TextView) mainLayout.getChildAt(0);
            text_title.setText(Html.fromHtml(title));

            TextView button_allow = (TextView) mainLayout.getChildAt(2);
            button_allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mOverlayPermissionAlert.dismiss();

                    mOverlayPermissionAlert = null;

                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));

                    startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
                }
            });

            TextView button_deny = (TextView) mainLayout.getChildAt(4);
            button_deny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mOverlayPermissionAlert.dismiss();

                    mOverlayPermissionAlert = null;

                    alertPermissionsDenied();
                }
            });

            mOverlayPermissionAlert = alertBuilder.create();

            mOverlayPermissionAlert.setView(mainLayout);
            //mOverlayPermissionAlert.setCancelable(false);
            mOverlayPermissionAlert.show();
        }
    }


    private AlertDialog mPermissionsDeniedAlert;

    private void alertPermissionsDenied() {

        if(mPermissionsDeniedAlert == null) {

            String[] permissions = Permissions.getNeededPermissionsLabels(mContext);

            String message = "";

            for (int i = 0; i < permissions.length; i++) {

                if (permissions[i] != null) {

                    message = message + permissions[i] + "<br>";
                }
            }

            message = "<b>" + message + "</b><br>" + getString(R.string.alert_permissions_denied_message);

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialog);

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.alert_permissions_denied, null);

            TextView text_title = (TextView) mainLayout.getChildAt(0);
            text_title.setText(getString(R.string.alert_permissions_denied_title));

            TextView text_message = (TextView) mainLayout.getChildAt(2);
            text_message.setText(Html.fromHtml(message));

            TextView text_ok = (TextView) mainLayout.getChildAt(4);
            text_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mPermissionsDeniedAlert.dismiss();

                    mPermissionsDeniedAlert = null;
                }
            });

            mPermissionsDeniedAlert = alertBuilder.create();

            mPermissionsDeniedAlert.setView(mainLayout);
            //mPermissionsDeniedAlert.setCancelable(false);
            mPermissionsDeniedAlert.show();
        }
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return mFragments.get(position);
        }

        @Override
        public int getCount() {

            return mFragments.size();
        }
    }


    private ArrayList<Fragment> initializeFragments() {

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        for (int i = 0; i < NUM_PAGES; i++) {

            if (i == MAIN_PAGE_INDEX) {

                MainFragment mainFragment = new MainFragment();
                mainFragment.setMainActivity(this);
                fragments.add(mainFragment);
            }
            else if (i == SAVED_INFO_PAGE_INDEX) {

                SavedInfoFragment savedInfoFragment = new SavedInfoFragment();
                fragments.add(savedInfoFragment);
            }
            else if (i == SETTINGS_PAGE_INDEX) {

                SettingsFragment settingsFragment = new SettingsFragment();
                settingsFragment.setMainActivity(this);
                fragments.add(settingsFragment);
            }
            else if (i == ABOUT_PAGE_INDEX) {

                AboutFragment aboutFragment = new AboutFragment();
                fragments.add(aboutFragment);
            }
        }

        return fragments;
    }


    public int getRunningState(){

        return  mRunningState;
    }


    public void startIgnoreBatteryOptimizationsActivity(){

        try {

            Intent i = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));

            startActivityForResult(i, IGNORE_BATTERY_OPTIMIZATIONS_REQUEST);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);

        try {

            if (requestCode == IGNORE_BATTERY_OPTIMIZATIONS_REQUEST) {

                if (resultCode == Activity.RESULT_OK) {

                    ((SettingsFragment) mFragments.get(SETTINGS_PAGE_INDEX)).enableIgnoreBatteryOptimizationsSetting();
                } else {

                    ((SettingsFragment) mFragments.get(SETTINGS_PAGE_INDEX)).disableIgnoreBatteryOptimizationsSetting();
                }
            }
            else if (requestCode == SCREEN_CAPTURE_REQUEST) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    if (resultCode == Activity.RESULT_OK) {

                        Settings.startScreenCaptureService(mContext, resultCode, resultData);
                    }
                    else{

                        stopScreenCaptureService();

                        Toast.makeText(mContext, R.string.cannot_capture_internal_audio, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public void stopScreenCaptureService(){

        Settings.stopScreenCaptureService(mContext);
    }


    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        System.out.println("MainActivity.onNewIntent()");

        if(mRunningState == SETTING_RUNNING_STATE_STARTED){

            String action = intent.getStringExtra(ACTION);

            if(HEADSET_EVENT.equals(action)){

                stop();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

                    setShowWhenLocked(true);
                    setTurnScreenOn(true);
                    KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
                    keyguardManager.requestDismissKeyguard(this, null);
                }

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                int headset_event = intent.getIntExtra(HEADSET_EVENT, HEADSET_PLUGGED);

                showHeadsetConnectionAlert(headset_event);
            }
        }
    }


    public void showPlaybackCaptureAlert() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            Intent i = mediaProjectionManager.createScreenCaptureIntent();

            startActivityForResult(i, SCREEN_CAPTURE_REQUEST);
        }
    }


    private void showHeadsetConnectionAlert(int headset_event) {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, R.style.CustomAlertDialog);

        String title = getString(R.string.headset_disconnected);

        if(headset_event == HEADSET_PLUGGED){

            title = getString(R.string.headset_connected);
        }

        alertBuilder.setTitle(title);

        alertBuilder.setMessage(R.string.headset_dialog_message);

        alertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = alertBuilder.create();

        dialog.show();
    }
}
