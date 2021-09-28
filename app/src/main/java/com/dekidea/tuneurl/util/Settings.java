package com.dekidea.tuneurl.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.dekidea.tuneurl.api.APIData;
import com.dekidea.tuneurl.service.MediaProjectionServer;
import com.dekidea.tuneurl.service.SoundListenerService;

import static android.content.Context.MODE_PRIVATE;

import java.util.Timer;
import java.util.TimerTask;

public class Settings implements Constants{

    private static APIData currentAPIData;
    private static int currentHeadsetState = -1;
    private static AudioPlaybackCaptureConfiguration audioPlaybackCaptureConfiguration = null;
    private static boolean useInternalAudio;


    public static void setUseInternalAudio(boolean use){

        useInternalAudio = use;
    }


    public static boolean useInternalAudio(){

        return useInternalAudio;
    }


    public static void setAudioPlaybackCaptureConfiguration(MediaProjection projection){

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                audioPlaybackCaptureConfiguration = new AudioPlaybackCaptureConfiguration.Builder(projection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .build();
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }

    public static AudioPlaybackCaptureConfiguration getAudioPlaybackCaptureConfiguration(){

        return audioPlaybackCaptureConfiguration;
    }

    public static void setCurrentHeadsetState(int state){

        currentHeadsetState = state;
    }


    public static boolean isWiredHeadsetOn(Context context){

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        return  audioManager.isWiredHeadsetOn();
    }

    public static int getCurrentHeadsetState(){

        return currentHeadsetState;
    }


    public static void setCurrentAPIData(APIData apiData){

        currentAPIData = apiData;
    }

    public static APIData getCurrentAPIData(){

        return currentAPIData;
    }


    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null) {

            if (netInfo.isConnected()) {

                return true;
            }
        }

        return false;
    }


    public static void startListeningService(Context context) {

        System.out.println("Settings.startListeningService()");

        Settings.updateIntSetting(context, SETTING_LISTENING_STATE, SETTING_LISTENING_STATE_STARTED);

        try {

            Intent i = new Intent(context, SoundListenerService.class);

            if (Build.VERSION.SDK_INT > 25) {

                context.startForegroundService(i);
            }
            else{

                context.startService(i);
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public static void stopListeningService(Context context) {

        System.out.println("Settings.stopListeningService()");

        Settings.updateIntSetting(context, SETTING_LISTENING_STATE, SETTING_LISTENING_STATE_STOPPED);

        try {

            Intent i = new Intent(context, SoundListenerService.class);

            context.stopService(i);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public static void startListening(Context context) {

        System.out.println("Settings.startListening()");

        try {

            Intent i = new Intent();
            i.setAction(LISTENING_ACTION);
            i.putExtra(ACTION, ACTION_START_LISTENING);

            context.sendBroadcast(i);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public static void stopListening(Context context) {

        System.out.println("Settings.stopListening()");

        try {

            Intent i = new Intent();
            i.setAction(LISTENING_ACTION);
            i.putExtra(ACTION, ACTION_STOP_LISTENING);

            context.sendBroadcast(i);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public static void stopRecorder(Context context) {

        System.out.println("Settings.stopRecorder()");

        try {

            Intent i = new Intent();
            i.setAction(LISTENING_ACTION);
            i.putExtra(ACTION, ACTION_STOP_RECORDER);

            context.sendBroadcast(i);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public static int fetchIntSetting(Context context, String setting, int default_value){

        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return (sp.getInt(setting, default_value));
    }


    public static void updateIntSetting(Context context, String setting, int value){

        try {

            SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putInt(setting, value);

            editor.commit();
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }

    public static String fetchStringSetting(Context context, String setting, String default_value){

        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return (sp.getString(setting, default_value));
    }

    public static void updateStringSetting(Context context, String setting, String value){

        try {

            SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString(setting, value);

            editor.commit();
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    public static void initializeCurrentHeadsetState(Context context){

        if(isHeadsetConnected(context)){

            setCurrentHeadsetState(HEADSET_PLUGGED);
        }
        else{

            setCurrentHeadsetState(HEADSET_UNPLUGGED);
        }
    }


    private static boolean isHeadsetConnected(Context context){

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager.isWiredHeadsetOn()){

            return true;
        }

        /*
        else {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(bluetoothAdapter != null) {

                return (BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET));
            }
        }
        */

        return false;
    }


    public static void startScreenCaptureService(Context context, int resultCode, Intent resultData){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            try {

                setUseInternalAudio(true);

                Intent i = new Intent(context, MediaProjectionServer.class);

                i.putExtra("resultCode", resultCode);
                i.putExtra(Intent.EXTRA_INTENT, resultData);

                context.startForegroundService(i);
            }
            catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    public static void stopScreenCaptureService(Context context){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            try {

                Intent i = new Intent(context, MediaProjectionServer.class);

                context.stopService(i);

                setUseInternalAudio(false);
            }
            catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}
