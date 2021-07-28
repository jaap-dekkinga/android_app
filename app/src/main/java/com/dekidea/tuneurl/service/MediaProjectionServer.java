package com.dekidea.tuneurl.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

public class MediaProjectionServer extends Service implements Constants {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        runAsForeground();

        int resultCode = intent.getIntExtra("resultCode", 0);
        Intent resultData = intent.getParcelableExtra(Intent.EXTRA_INTENT);

        setAudioPlaybackCaptureConfiguration(resultCode, resultData);

        return Service.START_STICKY;
    }


    private void setAudioPlaybackCaptureConfiguration(int resultCode, Intent resultData){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);

            Settings.setAudioPlaybackCaptureConfiguration(mediaProjection);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void runAsForeground(){

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the Foreground Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = createNotificationChannel(notificationManager);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_small)
                .setContentText(getString(R.string.screen_capturing_service_label))
                .setPriority(PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(CAPTURING_NOTIFICATION_ID, notification);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "tune_url_media_capture_service";
        String channelName = "TuneURL Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}
