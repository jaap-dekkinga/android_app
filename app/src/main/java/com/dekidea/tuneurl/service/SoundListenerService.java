package com.dekidea.tuneurl.service;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.receiver.HeadsetReceiver;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.SoundMeter;
import com.dekidea.tuneurl.util.SoundMeterExternal;
import com.dekidea.tuneurl.util.SoundMeterInternal;
import com.dekidea.tuneurl.util.TimeUtils;
import com.dekidea.tuneurl.util.WakeLocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;


public class SoundListenerService extends Service implements Constants {

	private Context mContext;

	private SoundMeter mSoundMeter;

	private Handler mHandler;
	private Runnable mPollTask;

	private int mRunningState;
	private int mListeningState;

	private double mSoundThreshold;

	private HeadsetReceiver mHeadSetReceiver;
	private IntentFilter mIntentFilter;

	@Override
	public void onCreate() {

		super.onCreate();

		mContext = this;

		mRunningState = SETTING_RUNNING_STATE_STOPPED;
		mListeningState = SETTING_LISTENING_STATE_STOPPED;

		mHeadSetReceiver = new HeadsetReceiver();
		mIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){

		onStart(intent, startId);

		return Service.START_STICKY;
	}


	@Override
	public void onDestroy(){

		super.onDestroy();
	}


	@Override
	public void onStart(Intent intent, int startId){

		WakeLocker.acquirePartialWakeLock(this.getApplicationContext());

		int action = ACTION_STOP_SERVICE;

		if(intent != null){

			action = intent.getIntExtra(ACTION, ACTION_STOP_SERVICE);
		}

		if(action == ACTION_START_SERVICE){

			startService();
		}
		else if(action == ACTION_STOP_SERVICE){

			stopService();
		}
		else if(action == ACTION_START_LISTENING){

			startListening();
		}
		else if(action == ACTION_STOP_LISTENING){

			//stopListening();
			pauseListening();
		}
	}


	private void startService() {

		runAsForeground();

		Settings.initializeCurrentHeadsetState(mContext);

		mRunningState = SETTING_RUNNING_STATE_STARTED;
		Settings.updateIntSetting(mContext, SETTING_RUNNING_STATE, mRunningState);

		mSoundThreshold = Settings.fetchIntSetting(this, SETTING_SOUND_THRESHOLD, DEFAULT_SOUND_THRESHOLD);

		startListening();

		registerReceiver(mHeadSetReceiver, mIntentFilter);
	}


	private void startListening(){

		if(mListeningState == SETTING_LISTENING_STATE_STOPPED) {

			mListeningState = SETTING_LISTENING_STATE_STARTED;

			Settings.updateIntSetting(this, SETTING_LISTENING_STATE, mListeningState);

			if (mSoundMeter == null) {

				AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

				if(audioManager.isWiredHeadsetOn() || Settings.useInternalAudio()){

					mSoundMeter = new SoundMeterInternal(this);
				}
				else{

					mSoundMeter = new SoundMeterExternal(this);
				}
			}

			mSoundMeter.start();

			mHandler = new Handler();

			mPollTask = new Runnable() {

				public void run() {

					try {

						if (mListeningState == SETTING_LISTENING_STATE_STARTED) {

							long time = TimeUtils.getCurrentTimeInMillis();

							double amp = mSoundMeter.getAmplitude();

							if ((amp > mSoundThreshold)) {

								startAnalyzingFingerprint(time);
							}
							else {

								if (mListeningState == SETTING_LISTENING_STATE_STARTED && mHandler != null && mPollTask != null) {

									mHandler.postDelayed(mPollTask, MIC_POLL_INTERVAL);
								}
							}
						}
					}
					catch (Exception e) {

						e.printStackTrace();
					}
				}
			};

			mPollTask.run();
		}
		else{

			//System.out.println("SoundListenerService.startListening(): ALREADY STARTED");
		}
	}


	private void stopService() {

		try {

			if (mHeadSetReceiver != null) {

				unregisterReceiver(mHeadSetReceiver);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}

		stopListening();

		mRunningState = SETTING_RUNNING_STATE_STOPPED;
		Settings.updateIntSetting(mContext, SETTING_RUNNING_STATE, mRunningState);

		WakeLocker.release();

		stopSelf();
	}


	private void stopListening() {

		mListeningState = SETTING_LISTENING_STATE_STOPPED;
		Settings.updateIntSetting(this, SETTING_LISTENING_STATE, mListeningState);

		if(mHandler != null){

			try{

				mHandler.removeCallbacks(mPollTask);
			}
			catch(Exception e){

				e.printStackTrace();
			}

			mHandler = null;
		}

		mPollTask = null;

		if(mSoundMeter != null){

			try{

				mSoundMeter.stop();
			}
			catch(Exception e){

				e.printStackTrace();
			}
		}

		mSoundMeter = null;
	}


	private void pauseListening() {

		mListeningState = SETTING_LISTENING_STATE_STOPPED;
		Settings.updateIntSetting(this, SETTING_LISTENING_STATE, mListeningState);

		if(mHandler != null){

			try{

				mHandler.removeCallbacks(mPollTask);
			}
			catch(Exception e){

				e.printStackTrace();
			}

			mHandler = null;
		}

		mPollTask = null;

		System.out.println("Settings.useInternalAudio() = " + (Settings.useInternalAudio()));
		System.out.println("Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q = " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q));

		if(Settings.useInternalAudio() && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {

			if (mSoundMeter != null) {

				try {

					mSoundMeter.pause();
				}
				catch (Exception e) {

					e.printStackTrace();
				}
			}
		}
		else{

			if (mSoundMeter != null) {

				try {

					mSoundMeter.stop();
				}
				catch (Exception e) {

					e.printStackTrace();
				}
			}

			mSoundMeter = null;
		}
	}


	private void startAnalyzingFingerprint(long time) {

		//stopListening();
		pauseListening();

		Intent i = new Intent(this, SoundMatchingService.class);
		i.putExtra(TIME, time);
		startService(i);
	}


	private void runAsForeground(){

		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		// Create the Foreground Service
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
		Notification notification = notificationBuilder
				.setContentIntent(pendingIntent)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_launcher_small)
				.setContentText(getString(R.string.listening_service_label))
				.setPriority(PRIORITY_HIGH)
				.setCategory(NotificationCompat.CATEGORY_SERVICE)
				.build();

		startForeground(NOTIFICATION_ID, notification);
	}


	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(NotificationManager notificationManager){
		String channelId = "tune_url_sound_listener_service";
		String channelName = "TuneURL Service";
		NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

		channel.setImportance(NotificationManager.IMPORTANCE_NONE);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
		return channelId;
	}
}