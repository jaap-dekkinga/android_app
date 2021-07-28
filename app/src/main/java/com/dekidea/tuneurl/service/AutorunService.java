package com.dekidea.tuneurl.service;

import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;
import com.dekidea.tuneurl.util.WakeLocker;

import android.app.IntentService;
import android.content.Intent;


public class AutorunService extends IntentService implements Constants {
	
	public AutorunService() {
		
		super("AutorunService");		
	}

	
	public AutorunService(String name) {
		
		super(name);
	}	
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		WakeLocker.acquirePartialWakeLock(this.getApplicationContext());
		
		if(intent != null){
			
			int action = intent.getIntExtra(ACTION, -1);
			
			if(action == ACTION_AUTORUN_START){

                Settings.updateIntSetting(this, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STARTED);

                Settings.startListeningService(this);
				
				Intent broadcast = new Intent(MESSAGE_APP_STATUS_CHANGED);
				broadcast.putExtra(MESSAGE, MESSAGE_APP_STATUS_STARTED);
				this.sendBroadcast(broadcast);
			}
			else if(action == ACTION_AUTORUN_STOP){

                Settings.updateIntSetting(this, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);
				
				Settings.stopListeningService(this);
				
				Intent broadcast = new Intent(MESSAGE_APP_STATUS_CHANGED);
				broadcast.putExtra(MESSAGE, MESSAGE_APP_STATUS_STOPPED);
				this.sendBroadcast(broadcast);
				
				TimeUtils.scheduleAutomaticStart(this);
			}
		}
		
		WakeLocker.release();
	}
	
	
	@Override
	public void onDestroy(){
		
		super.onDestroy();
		
		
	}
}