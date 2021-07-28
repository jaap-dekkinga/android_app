package com.dekidea.tuneurl.service;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;

@SuppressLint("NewApi")
public class AutorunJobService extends JobService implements Constants {

    private static final String TAG = "AutorunJobService";

    @Override
    public boolean onStartJob(JobParameters params) {

        try{

            int action = params.getExtras().getInt(ACTION, -1);
            int running_state = Settings.fetchIntSetting(this, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

            if(action == ACTION_AUTORUN_START){

                if(running_state == SETTING_RUNNING_STATE_STOPPED) {

                    if (android.os.Build.VERSION.SDK_INT >= 30) {

                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra(AUTORUN_STARTED, true);
                        startActivity(i);
                    }
                    else {

                        Settings.startListeningService(this);
                    }

                    Intent broadcast = new Intent(MESSAGE_APP_STATUS_CHANGED);
                    broadcast.putExtra(MESSAGE, MESSAGE_APP_STATUS_STARTED);
                    this.sendBroadcast(broadcast);
                }
            }
            else if(action == ACTION_AUTORUN_STOP){

                if(running_state == SETTING_RUNNING_STATE_STARTED) {

                    Settings.stopListeningService(this);

                    Intent broadcast = new Intent(MESSAGE_APP_STATUS_CHANGED);
                    broadcast.putExtra(MESSAGE, MESSAGE_APP_STATUS_STOPPED);
                    this.sendBroadcast(broadcast);
                }

                TimeUtils.scheduleAutomaticStart(this);
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return true;
    }
}