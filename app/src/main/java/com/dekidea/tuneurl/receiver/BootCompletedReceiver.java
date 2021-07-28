package com.dekidea.tuneurl.receiver;

import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver implements Constants {
	
	@Override
	public void onReceive(Context context, Intent intent) {		

		int autorun_setting = Settings.fetchIntSetting(context, SETTING_AUTORUN_MODE, SETTING_AUTORUN_MODE_DISABLED);
		
		if(autorun_setting == SETTING_AUTORUN_MODE_ENABLED){
			
			TimeUtils.scheduleAutomaticStart(context);
		}		
	}
}
