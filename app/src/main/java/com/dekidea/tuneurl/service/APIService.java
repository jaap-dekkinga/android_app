package com.dekidea.tuneurl.service;

import android.app.IntentService;
import android.content.Intent;

import com.dekidea.tuneurl.repository.Repository;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.WakeLocker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.inject.Inject;
import dagger.android.AndroidInjection;


public class APIService extends IntentService implements Constants {

    @Inject
    public Repository repo;
	
	static {
		
		System.loadLibrary("mp3lame");
	}
	
	public APIService() {
		
		super("APIService");		
	}

	
	public APIService(String name) {
		
		super(name);
	}


    @Override
    public void onCreate() {

        configureDagger();

        super.onCreate();
    }


    private void configureDagger(){

        AndroidInjection.inject(this);
    }


    @Override
	protected void onHandleIntent(Intent intent) {
		
		WakeLocker.acquirePartialWakeLock(this.getApplicationContext());
		
		if(intent != null){
			
			//int display_width = TimeUtils.fetchIntSetting(this.getApplicationContext(), SETTING_DISPLAY_WIDTH, DEFAULT_DISPLAY_WIDTH);
			
			int action = intent.getIntExtra(ACTION, -1)	;
            String user_response = intent.getStringExtra(USER_RESPONSE);

			if(action == ACTION_SEARCH_FINGERPRINT){

				String fingerprint_string = intent.getStringExtra(FINGERPRINT);

				if(fingerprint_string != null){

					JsonParser jsonParser = new JsonParser();
					JsonArray data = (JsonArray) jsonParser.parse("[" + fingerprint_string + "]");

					JsonObject buffer = new JsonObject();

					buffer.addProperty("type", "Buffer");
					buffer.add("data", data);

					JsonObject fingerprint = new JsonObject();

					fingerprint.add("fingerprint", buffer);

					repo.searchFingerprint(this.getApplicationContext(), fingerprint);
				}
			}
			else if(action == ACTION_DO_ACTION){

				repo.doAction(this.getApplicationContext(), user_response);
			}
			else if(action == ACTION_ADD_RECORD_OF_INTEREST){

				long TuneURL_ID = intent.getLongExtra(TUNEURL_ID, -1);
				String Interest_action = intent.getStringExtra(INTEREST_ACTION);
				String date = intent.getStringExtra(DATE);

				repo.addRecordOfInterest(this.getApplicationContext(), String.valueOf(TuneURL_ID), Interest_action, date);
			}
		}
		
		WakeLocker.release();
	}


    private native void initEncoder(int numChannels, int sampleRate, int bitRate, int mode, int quality);

    private native void destroyEncoder();

    private native int encodeFile(String sourcePath, String targetPath);


    private int encodeAudioFile(String raw_file_path, String encoded_file_path){

        initEncoder(AUDIO_FILE_NUM_CHANNELS, AUDIO_FILE_MP3_SAMPLE_RATE, AUDIO_FILE_BITRATE, AUDIO_FILE_MODE, AUDIO_FILE_QUALITY);

        int result = encodeFile(raw_file_path, encoded_file_path);

        destroyEncoder();

        return result;
    }
}