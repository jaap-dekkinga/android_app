package com.dekidea.tuneurl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;

public class HeadsetReceiver extends BroadcastReceiver implements Constants {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {

            int state = intent.getIntExtra("state", -1);

            if(state != Settings.getCurrentHeadsetState()) {

                Settings.setCurrentHeadsetState(state);

                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(ACTION, HEADSET_EVENT);
                i.putExtra(HEADSET_EVENT, state);

                context.startActivity(i);
            }
        }
    }
}