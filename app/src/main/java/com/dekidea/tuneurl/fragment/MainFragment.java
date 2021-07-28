package com.dekidea.tuneurl.fragment;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;

public class MainFragment extends Fragment implements Constants {

    private MainActivity mMainActivity;
    private Switch mOnOffSwitch;
    private TextView mOnOffLabel;

    private Switch mInternalAudioSwitch;
    private TextView mInternalAudioLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_page, container, false);

        mOnOffSwitch = (Switch)rootView.findViewById(R.id.on_off_switch);
        mOnOffLabel = (TextView)rootView.findViewById(R.id.on_off_label);

        mInternalAudioSwitch = (Switch)rootView.findViewById(R.id.internal_audio_switch);
        mInternalAudioLabel = (TextView)rootView.findViewById(R.id.internal_audio_label);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            mInternalAudioLabel.setVisibility(View.GONE);
            mInternalAudioSwitch.setVisibility(View.GONE);
        }

        mOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub

                if(mMainActivity != null && isChecked){

                    if(Settings.isOnline(mMainActivity.getApplicationContext())){

                        mMainActivity.start();
                    }
                    else{

                        mMainActivity.showNoNetworkConnectionAlert();
                    }
                }
                else{

                    if(mMainActivity != null){

                        mMainActivity.stop();
                    }
                }
            }});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            mInternalAudioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    // TODO Auto-generated method stub

                    if(mMainActivity != null){

                        if(isChecked){

                            mMainActivity.showPlaybackCaptureAlert();
                        }
                        else{

                            mMainActivity.stopScreenCaptureService();
                        }
                    }
                }
            });
        }


        if(mMainActivity != null){

            displayStatus(mMainActivity.getRunningState());
        }

        return rootView;
    }

    public void setMainActivity(MainActivity activity){

        mMainActivity = activity;
    }

    public void displayInternalAudioStatus(boolean active){

        mInternalAudioSwitch.setChecked(active);
    }

    public void displayStatus(int running_state){

        if(running_state == SETTING_RUNNING_STATE_STOPPED){

            mOnOffSwitch.setChecked(false);

            mOnOffLabel.setText(R.string.off_label);
            mOnOffLabel.setTextColor(Color.LTGRAY);
        }
        else{

            mOnOffSwitch.setChecked(true);

            mOnOffLabel.setText(R.string.on_label);
            mOnOffLabel.setTextColor(Color.WHITE);
        }
    }
}