package com.dekidea.tuneurl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.dekidea.tuneurl.db.entity.AudioItem;
import com.dekidea.tuneurl.repository.Repository;
import com.dekidea.tuneurl.service.APIService;
import com.dekidea.tuneurl.util.Constants;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ConnectivityChangeReceiver  extends BroadcastReceiver implements Constants {

    @Inject
    public Repository repo;

	@Override
	public void onReceive(Context context, Intent intent) {

        AndroidInjection.inject(this, context);
		
		ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo info = cm.getActiveNetworkInfo();
	      
	    if(info != null && info.isConnected()) {

            List<AudioItem> items = repo.getAudioItems();

            if(items != null && !items.isEmpty()) {

                for (int i = 0; i < items.size(); i++) {

                    AudioItem item = items.get(i);

                    String audio_file_path = item.getAudio_file_path();
                    String user_response = item.getUser_response();

                    callAPIService(context, audio_file_path, user_response);
                }
            }
	    }
	}

	
	private void callAPIService(Context context, String audio_file_path, String poll_response){
		
		Intent i = new Intent(context, APIService.class);

		i.putExtra(ACTION, ACTION_GET_DATA_SECOND_RUN);
		i.putExtra(AUDIO_FILE_PATH, audio_file_path);
		i.putExtra(USER_RESPONSE, poll_response);

        if (Build.VERSION.SDK_INT > 25) {

            context.startForegroundService(i);
        }
        else{

            context.startService(i);
        }
	}
}
