package com.dekidea.tuneurl.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class SettingsFragment extends Fragment implements Constants {

    private Context mContext;

    private MainActivity mMainActivity;

    private LinearLayout mStoreStoriesLayout;
    private TextView mStoreStoriesView;

    private LinearLayout mTimezoneLayout;
    private TextView mTimezoneView;

    private Switch mAutorunSettingSwitch;
    private LinearLayout mAutorunIntervalLayout;
    private TextView mAutorunStartView;
    private TextView mAutorunEndView;

    private LinearLayout mAPIURLLayout;
    private TextView mAPIURLView;
    private EditText mAPIURLEditText;

    private RelativeLayout mIgnoreBatteryOptimizationsSettingLayout;
    private Switch mIgnoreBatteryOptimizationsSettingSwitch;

    private int mStoreStoriesSetting;
    private String[] mStoreStoriesValues;

    private int mTimezoneSetting;
    private String[] mTimezoneValues;

    private int mAutorunSetting;
    private int mAutorunStartHour;
    private int mAutorunStartMinute;
    private int mAutorunEndHour;
    private int mAutorunEndMinute;

    private String mAPIURL;

    private int mIgnoreBatteryOptimizationsSetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_settings_page, container, false);

        mContext = this.getContext();

        mStoreStoriesLayout = (LinearLayout) rootView.findViewById(R.id.settings_store_interval_layout);
        mStoreStoriesLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                showStoreOptions();
            }});
        mStoreStoriesView = (TextView) rootView.findViewById(R.id.settings_store_interval_value);

        mTimezoneLayout = (LinearLayout) rootView.findViewById(R.id.setting_timezone_layout);
        mTimezoneLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showTimezoneOptions();
            }});
        mTimezoneView = (TextView) rootView.findViewById(R.id.setting_timezone_value);

        mAutorunSettingSwitch = (Switch) rootView.findViewById(R.id.setting_autorun_mode);
        mAutorunSettingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub

                if(isChecked){

                    mAutorunSetting = SETTING_AUTORUN_MODE_ENABLED;
                }
                else{

                    mAutorunSetting = SETTING_AUTORUN_MODE_DISABLED;
                }

                Settings.updateIntSetting(mContext, SETTING_AUTORUN_MODE, mAutorunSetting);

                if(mAutorunSetting == SETTING_AUTORUN_MODE_ENABLED){

                    TimeUtils.scheduleAutomaticStart(mContext);
                }
                else{

                    TimeUtils.cancelAutomaticStart(mContext);
                }
            }});

        mAutorunIntervalLayout = (LinearLayout) rootView.findViewById(R.id.setting_autorun_interval_layout);
        mAutorunIntervalLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showAutorunIntervalOptions();
            }});
        mAutorunStartView = (TextView) rootView.findViewById(R.id.setting_autorun_start_value);
        mAutorunEndView = (TextView) rootView.findViewById(R.id.setting_autorun_end_value);


        mAPIURLLayout = (LinearLayout) rootView.findViewById(R.id.setting_api_url_layout);
        mAPIURLLayout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showAPIURLDialog();
            }});

        mAPIURLView = (TextView) rootView.findViewById(R.id.setting_api_url_value);

        mIgnoreBatteryOptimizationsSettingLayout = (RelativeLayout) rootView.findViewById(R.id.setting_ingnore_battery_optimizations_layout);
        if (android.os.Build.VERSION.SDK_INT < 23) {

            mIgnoreBatteryOptimizationsSettingLayout.setVisibility(View.GONE);
        }
        else {

            mIgnoreBatteryOptimizationsSetting = Settings.fetchIntSetting(mContext, SETTING_IGNORE_BATTERY_OPTIMIZATIONS, SETTING_IGNORE_BATTERY_OPTIMIZATIONS_DISABLED);

            mIgnoreBatteryOptimizationsSettingSwitch = (Switch) rootView.findViewById(R.id.setting_ingnore_battery_optimizations);

            if (mIgnoreBatteryOptimizationsSetting == SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED) {

                mIgnoreBatteryOptimizationsSettingSwitch.setChecked(true);
                mIgnoreBatteryOptimizationsSettingSwitch.setEnabled(false);
            }
            else {

                mIgnoreBatteryOptimizationsSettingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub

                        if (mMainActivity != null && isChecked) {

                            mMainActivity.startIgnoreBatteryOptimizationsActivity();
                        }
                    }
                });
            }
        }

        initData();

        return rootView;
    }

    private void initData(){

        mStoreStoriesSetting = Settings.fetchIntSetting(mContext, SETTING_STORE_NEWS_ITEMS, SETTING_STORE_NEWS_ITEMS_1DAY);
        mStoreStoriesValues = getResources().getStringArray(R.array.setting_store_interval_values);

        mTimezoneSetting = Settings.fetchIntSetting(mContext, SETTING_TIMEZONE, DEFAULT_TIMEZONE);
        mTimezoneValues = getResources().getStringArray(R.array.setting_timezone_values);

        mAutorunSetting = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_MODE, SETTING_AUTORUN_MODE_DISABLED);
        mAutorunStartHour = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_START_HOUR, DEFAULT_AUTORUN_START_HOUR);
        mAutorunStartMinute = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_START_MINUTE, DEFAULT_AUTORUN_START_MINUTE);
        mAutorunEndHour = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_END_HOUR, DEFAULT_AUTORUN_END_HOUR);
        mAutorunEndMinute = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_END_MINUTE, DEFAULT_AUTORUN_END_MINUTE);

        mAPIURL =  Settings.fetchStringSetting(mContext, SETTING_API_URL, TUNEURL_API_BASE_URL);

        if(mAPIURL == null){

            mAPIURL = TUNEURL_API_BASE_URL;
        }

        displayData();
    }


    private void displayData(){

        mStoreStoriesView.setText(mStoreStoriesValues[mStoreStoriesSetting]);
        mTimezoneView.setText(mTimezoneValues[mTimezoneSetting]);

        if(mAutorunSetting == SETTING_AUTORUN_MODE_ENABLED){

            mAutorunSettingSwitch.setChecked(true);
        }
        else{

            mAutorunSettingSwitch.setChecked(false);
        }

        mAutorunStartView.setText(mAutorunStartHour + ":" + TimeUtils.pad(mAutorunStartMinute));
        mAutorunEndView.setText(mAutorunEndHour + ":" + TimeUtils.pad(mAutorunEndMinute));

        mAPIURLView.setText(mAPIURL);

        if (mIgnoreBatteryOptimizationsSettingSwitch != null) {

            mIgnoreBatteryOptimizationsSettingSwitch.setChecked(mIgnoreBatteryOptimizationsSetting == SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED);

            if (mIgnoreBatteryOptimizationsSetting == SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED) {

                mIgnoreBatteryOptimizationsSettingSwitch.setEnabled(false);
            }
        }
    }


    private void showStoreOptions(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(R.string.setting_store_interval_title);

        builder.setSingleChoiceItems(R.array.setting_store_interval_values, mStoreStoriesSetting, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                mStoreStoriesSetting	= which;
            }
        });


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                Settings.updateIntSetting(mContext, SETTING_STORE_NEWS_ITEMS, mStoreStoriesSetting);

                displayData();
            }
        });


        AlertDialog alert = builder.create();

        alert.show();
    }


    private void showTimezoneOptions(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(R.string.setting_timezone_title);

        builder.setSingleChoiceItems(R.array.setting_timezone_values, mTimezoneSetting, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                mTimezoneSetting	= which;
            }
        });


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                Settings.updateIntSetting(mContext, SETTING_TIMEZONE, mTimezoneSetting);

                displayData();
            }
        });


        AlertDialog alert = builder.create();

        alert.show();
    }


    private Button mStartIntervalButton;
    private Button mEndIntervalButton;

    private void showAutorunIntervalOptions(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);

        alertBuilder.setTitle(getString(R.string.set_autorun_interval_title));

        alertBuilder.setPositiveButton(android.R.string.ok, new  DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Settings.updateIntSetting(mContext, SETTING_AUTORUN_START_HOUR, mAutorunStartHour);
                Settings.updateIntSetting(mContext, SETTING_AUTORUN_START_MINUTE, mAutorunStartMinute);
                Settings.updateIntSetting(mContext, SETTING_AUTORUN_END_HOUR, mAutorunEndHour);
                Settings.updateIntSetting(mContext, SETTING_AUTORUN_END_MINUTE, mAutorunEndMinute);

                if(mAutorunSetting == SETTING_AUTORUN_MODE_ENABLED){

                    TimeUtils.scheduleAutomaticStart(mContext);
                }

                displayData();
            }});

        alertBuilder.setNegativeButton(android.R.string.cancel, new  DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                mAutorunStartHour = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_START_HOUR, DEFAULT_AUTORUN_START_HOUR);
                mAutorunStartMinute = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_START_MINUTE, DEFAULT_AUTORUN_START_MINUTE);
                mAutorunEndHour = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_END_HOUR, DEFAULT_AUTORUN_END_HOUR);
                mAutorunEndMinute = Settings.fetchIntSetting(mContext, SETTING_AUTORUN_END_MINUTE, DEFAULT_AUTORUN_END_MINUTE);
            }});


        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mainLayout = (LinearLayout)inflater.inflate(R.layout.alert_set_autorun_interval, null);

        mStartIntervalButton = (Button)((LinearLayout)mainLayout.getChildAt(0)).getChildAt(1);
        mStartIntervalButton.setText(mAutorunStartHour + ":" + TimeUtils.pad(mAutorunStartMinute));
        mStartIntervalButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                TimePickerDialog time_picker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {

                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mAutorunStartHour = hourOfDay;
                        mAutorunStartMinute = minute;

                        mStartIntervalButton.setText(mAutorunStartHour + ":" + TimeUtils.pad(mAutorunStartMinute));
                    }
                }, mAutorunStartHour, mAutorunStartMinute, true);

                time_picker.setTitle(getString(R.string.set_autorun_start));
                time_picker.show();
            }});


        mEndIntervalButton = (Button)((LinearLayout)mainLayout.getChildAt(1)).getChildAt(1);
        mEndIntervalButton.setText(mAutorunEndHour + ":" + TimeUtils.pad(mAutorunEndMinute));
        mEndIntervalButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                TimePickerDialog time_picker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {

                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mAutorunEndHour = hourOfDay;
                        mAutorunEndMinute = minute;

                        mEndIntervalButton.setText(mAutorunEndHour + ":" + TimeUtils.pad(mAutorunEndMinute));
                    }
                }, mAutorunEndHour, mAutorunEndMinute, true);

                time_picker.setTitle(getString(R.string.set_autorun_end));
                time_picker.show();
            }});


        AlertDialog alert = alertBuilder.create();

        alert.setView(mainLayout);

        alert.show();
    }


    private void showAPIURLDialog(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);

        alertBuilder.setTitle(getString(R.string.setting_api_url_title));

        alertBuilder.setPositiveButton(android.R.string.ok, new  DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    String api_url = mAPIURLEditText.getText().toString();

                    if(api_url != null && !api_url.isEmpty()){

                        try {

                            URL url = new URL(api_url);

                            if (!api_url.equals(mAPIURL)) {

                                mAPIURL = api_url;

                                Settings.updateStringSetting(mContext, SETTING_API_URL, mAPIURL);
                            }
                        }
                        catch(MalformedURLException e){

                            e.printStackTrace();

                            Toast.makeText(mContext, R.string.malformed_url_message, Toast.LENGTH_LONG).show();
                        }
                    }

                    displayData();
                }
                catch (Exception e){

                    e.printStackTrace();
                }
            }});

        alertBuilder.setNegativeButton(android.R.string.cancel, new  DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }});


        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mainLayout = (LinearLayout)inflater.inflate(R.layout.alert_set_api_url, null);

        mAPIURLEditText = (EditText)((LinearLayout)mainLayout).getChildAt(0);
        mAPIURLEditText.setText(mAPIURL);

        AlertDialog alert = alertBuilder.create();

        alert.setView(mainLayout);

        alert.show();
    }


    public void setMainActivity(MainActivity activity){

        mMainActivity = activity;
    }


    public void enableIgnoreBatteryOptimizationsSetting(){

        mIgnoreBatteryOptimizationsSetting = SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED;

        Settings.updateIntSetting(mContext, SETTING_IGNORE_BATTERY_OPTIMIZATIONS, SETTING_IGNORE_BATTERY_OPTIMIZATIONS_ENABLED);

        displayData();
    }


    public void disableIgnoreBatteryOptimizationsSetting(){

        mIgnoreBatteryOptimizationsSetting = SETTING_IGNORE_BATTERY_OPTIMIZATIONS_DISABLED;

        Settings.updateIntSetting(mContext, SETTING_IGNORE_BATTERY_OPTIMIZATIONS, SETTING_IGNORE_BATTERY_OPTIMIZATIONS_DISABLED);

        if (mIgnoreBatteryOptimizationsSettingSwitch != null) {

            mIgnoreBatteryOptimizationsSettingSwitch.setChecked(false);
        }

        displayData();
    }
}