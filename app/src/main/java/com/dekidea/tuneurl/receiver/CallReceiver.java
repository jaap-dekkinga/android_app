package com.dekidea.tuneurl.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;

import java.util.Timer;
import java.util.TimerTask;

public class CallReceiver extends BroadcastReceiver implements Constants {

    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private MyCallListener mMyCallListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        mMyCallListener = new MyCallListener();

        mTelephonyManager.listen(mMyCallListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    class MyCallListener extends PhoneStateListener implements Constants{

        @SuppressLint("NewApi")
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {

                case TelephonyManager.CALL_STATE_OFFHOOK:

                    System.out.println("CALL_STATE_OFFHOOK");

                    Settings.stopListening(mContext);

                    break;

                case TelephonyManager.CALL_STATE_RINGING:

                    System.out.println("CALL_STATE_RINGING");

                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    System.out.println("CALL_STATE_IDLE");

                    int running_state = Settings.fetchIntSetting(mContext, Constants.SETTING_RUNNING_STATE, Constants.SETTING_RUNNING_STATE_STOPPED);

                    if(running_state == SETTING_RUNNING_STATE_STARTED){

                        TimerTask timerTask = new TimerTask() {

                            public void run() {

                                Settings.startListening(mContext);
                            }
                        };

                        Timer timer = new Timer();
                        timer.schedule(timerTask, 2000);
                    }

                    break;

                default:

                    System.out.println("CALL_STATE_OTHER");

                    break;
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    }
}