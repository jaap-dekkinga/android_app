package com.dekidea.tuneurl.service;

import com.asha.libresample2.Resample;
import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.activity.MainActivity;
import com.dekidea.tuneurl.receiver.HeadsetReceiver;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.FileUtils;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.WakeLocker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;


import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SoundListenerService extends Service implements Constants {

	private static final int DEFAULT_SOUND_THRESHOLD = 90;
	private static final float SIMILARITY_THRESHOLD = 0.5f;

	private static final int RECORDER_BPP = 16;
	private static final int RECORDING_SAMPLE_RATE = 44100;
	private static final int FINGERPRINT_SAMPLE_RATE = 10240;

	private static final int TRIGGER_SIZE_MILLIS = 1500;
	private static final int TUNE_URL_SIZE_MILLIS = 5000;

	private static final int FINGERPRINT_TRIGGER_BUFFER_SIZE = (int) ((double) FINGERPRINT_SAMPLE_RATE * ((double) RECORDER_BPP / 8d) * ((double) TRIGGER_SIZE_MILLIS / 1000d));
	private static final int RECORDED_TRIGGER_BUFFER_SIZE = (int) ((double) RECORDING_SAMPLE_RATE * ((double) RECORDER_BPP / 8d) * ((double) TRIGGER_SIZE_MILLIS / 1000d));

	private static final int FINGERPRINT_TUNE_URL_BUFFER_SIZE = (int) ((double) FINGERPRINT_SAMPLE_RATE * ((double) RECORDER_BPP / 8d) * ((double) TUNE_URL_SIZE_MILLIS / 1000d));
	private static final int RECORDED_TUNE_URL_BUFFER_SIZE = (int) ((double) RECORDING_SAMPLE_RATE * ((double) RECORDER_BPP / 8d) * ((double) TUNE_URL_SIZE_MILLIS / 1000d));

	private static final int TUNE_URL_WAVE_LENGHT = (int) ((double) FINGERPRINT_TUNE_URL_BUFFER_SIZE / 2d);

	private static final int STOPPED = 0;
	private static final int LISTENING = 1;
	private static final int RECORDING_TRIGGER = 2;
	private static final int RECORDING_TUNEURL = 3;
	private static final int IDLE = 4;


	private Context mContext;

	private AudioRecord mRecorder;

	private int mRecorderState;

	private double mSoundThreshold;

	private float mSimilarity;

	private HeadsetReceiver mHeadSetReceiver;
	private IntentFilter mIntentFilter;

	private ListenerActionReceiver mListenerActionReceiver;
	private IntentFilter mListenerActionFilter;

	private ByteBuffer referenceTriggerByteBuffer = null;

	private ByteBuffer triggerByteBuffer = null;
	private ByteBuffer tuneUrlByteBuffer = null;

	private ByteBuffer resampledTriggerByteBuffer = null;
	private ByteBuffer resampledTuneUrlByteBuffer = null;

	ExecutorService mExecutorService;


	static {

		System.loadLibrary("native-lib");
	}


	@Override
	public void onCreate() {

		super.onCreate();

		mContext = this;

		initializeResources();
	}


	private void initializeResources() {

		try {

			mExecutorService = Executors.newFixedThreadPool(1);

			String reference_trigger_file_path = FileUtils.getReferenceFilePaths(this)[0];

			InputStream inputStream = new FileInputStream(reference_trigger_file_path);

			referenceTriggerByteBuffer = ByteBuffer.allocateDirect(FINGERPRINT_TRIGGER_BUFFER_SIZE);
			referenceTriggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			Channels.newChannel(inputStream).read(referenceTriggerByteBuffer);

			resampledTriggerByteBuffer = ByteBuffer.allocateDirect(FINGERPRINT_TRIGGER_BUFFER_SIZE);
			resampledTriggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			triggerByteBuffer = ByteBuffer.allocateDirect(RECORDED_TRIGGER_BUFFER_SIZE);
			triggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			tuneUrlByteBuffer = ByteBuffer.allocateDirect(RECORDED_TUNE_URL_BUFFER_SIZE);
			tuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			resampledTuneUrlByteBuffer = ByteBuffer.allocateDirect(FINGERPRINT_TUNE_URL_BUFFER_SIZE);
			resampledTuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			mHeadSetReceiver = new HeadsetReceiver();
			mIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

			mListenerActionReceiver = new ListenerActionReceiver();
			mListenerActionFilter = new IntentFilter(LISTENING_ACTION);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

				registerReceiver(mHeadSetReceiver, mIntentFilter, Context.RECEIVER_EXPORTED);
				registerReceiver(mListenerActionReceiver, mListenerActionFilter, Context.RECEIVER_EXPORTED);
			}
			else{

				registerReceiver(mHeadSetReceiver, mIntentFilter);
				registerReceiver(mListenerActionReceiver, mListenerActionFilter);
			}

			mSimilarity = 0;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		super.onStartCommand(intent, flags, startId);

		WakeLocker.acquirePartialWakeLock(this.getApplicationContext());

		startService();

		return Service.START_STICKY;
	}


	@Override
	public void onDestroy() {

		super.onDestroy();

		stopService();
	}


	private void startService() {

		runAsForeground();

		Settings.initializeCurrentHeadsetState(mContext);

		Settings.updateIntSetting(mContext, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STARTED);

		mSoundThreshold = Settings.fetchIntSetting(this, SETTING_SOUND_THRESHOLD, DEFAULT_SOUND_THRESHOLD);

		startListening();
	}


	private void runAsForeground() {

		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
	private String createNotificationChannel(NotificationManager notificationManager) {
		String channelId = "tune_url_sound_listener_service";
		String channelName = "TuneURL Service";
		NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

		channel.setImportance(NotificationManager.IMPORTANCE_NONE);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
		return channelId;
	}


	private void stopService() {

		releaseResources();

		Settings.updateIntSetting(mContext, SETTING_LISTENING_STATE, Constants.SETTING_LISTENING_STATE_STOPPED);

		WakeLocker.release();
	}


	class ListenerActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {

				int action = intent.getIntExtra(ACTION, -1);

				if (action == ACTION_START_LISTENING) {

					startListening();
				} else if (action == ACTION_STOP_LISTENING) {

					stopListening();
				}
				else if (action == ACTION_STOP_RECORDER) {

					mRecorderState = STOPPED;
				}
			}
		}
	}


	private void startListening() {

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

			if (mRecorder == null) {

				try {

					AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

					int audio_source = MediaRecorder.AudioSource.MIC;

					if (audioManager.isWiredHeadsetOn()) {

						audio_source = MediaRecorder.AudioSource.DEFAULT;
					}

					int bufferSize = AudioRecord.getMinBufferSize(RECORDING_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4;

					mRecorder = new AudioRecord(audio_source,
							RECORDING_SAMPLE_RATE,
							AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT, bufferSize);

					startListeningForTrigger();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			else {

				mRecorderState = LISTENING;
			}
		}
	}


	private void startListeningForTrigger(){

		try {

			if (mRecorder != null) {

				int state = mRecorder.getState();

				if(state == AudioRecord.STATE_INITIALIZED) {

					mRecorder.startRecording();

					mExecutorService.execute(new Runnable() {
						@Override
						public void run() {

							listenForTrigger();
						}
					});
				}
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	private void listenForTrigger(){

		System.out.println("SoundListenerService.listenForTrigger(): START");

		mRecorderState = LISTENING;

		mSimilarity = 0;

		byte data[] = new byte[512];

		int read = 0;

		while (mRecorderState != STOPPED && mRecorder != null) {

			try {

				if(mRecorderState == LISTENING){

					read = mRecorder.read(data, 0, data.length);

					double amplitude = getAmplitude(data, read);

					if(amplitude > mSoundThreshold){

						mRecorderState = RECORDING_TRIGGER;
					}
				}
				else if(mRecorderState == IDLE){

					read = mRecorder.read(data, 0, data.length);
				}
				else {

					triggerByteBuffer.clear();
					triggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

					tuneUrlByteBuffer.clear();
					tuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

					while((mRecorderState == RECORDING_TRIGGER || mRecorderState == RECORDING_TUNEURL) &&
							mRecorder != null) {

						read = mRecorder.read(data, 0, data.length);

						if(read > 0) {

							if (mRecorderState == RECORDING_TRIGGER) {

								int trigger_remaining = triggerByteBuffer.remaining();

								if (read < trigger_remaining) {

									triggerByteBuffer.put(data, 0, read);
								}
								else {

									triggerByteBuffer.put(data, 0, trigger_remaining);

									mRecorderState = RECORDING_TUNEURL;

									checkTriggerFingerprint();
								}
							}
							else {

								int tuneurl_remaining = tuneUrlByteBuffer.remaining();

								if (read < tuneurl_remaining) {

									tuneUrlByteBuffer.put(data, 0, read);
								}
								else {

									tuneUrlByteBuffer.put(data, 0, tuneurl_remaining);

									if (mSimilarity >= SIMILARITY_THRESHOLD) {

										stopListening();

										searchTuneUrlFingerprint();
									}
									else {

										mRecorderState = LISTENING;
									}
								}
							}
						}
					}
				}
			}
			catch (Exception e){

				e.printStackTrace();
			}
		}

		System.out.println("SoundListenerService.listenForTrigger(): END");

		stopRecorder();
	}


	private double getAmplitude(byte data[], int read){

		short[] short_data = new short[data.length / 2];

		ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(short_data);

		double amplitude = 0;

		if (read > 0) {

			double sum = 0;

			for (int i = 0; i < (int)(read/2); i++) {

				sum += short_data[i] * short_data[i];
			}

			double raw_amplitude = sum / read;

			if (raw_amplitude > 0) {

				amplitude = (20d * Math.log10(raw_amplitude * 10));
			}
		}

		//System.out.println("amplitude = " + (int)amplitude);

		return amplitude;
	}


	private void stopListening(){

		System.out.println("SoundListenerService.stopListening()");

		mRecorderState = IDLE;
	}


	private void stopRecorder(){

		System.out.println("SoundListenerService.stopRecorder()");

		if(mRecorder != null){

			try{

				int state = mRecorder.getState();

				if(state == AudioRecord.STATE_INITIALIZED) {

					mRecorder.stop();
				}
			}
			catch (Exception e){

				e.printStackTrace();
			}

			try{

				mRecorder.release();
			}
			catch (Exception e){

				e.printStackTrace();
			}

			mRecorder = null;
		}
	}


	private void releaseResources(){

		System.out.println("SoundListenerService.releaseResources()");

		mRecorderState = STOPPED;

		try {

			if (mHeadSetReceiver != null) {

				unregisterReceiver(mHeadSetReceiver);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}

		try {

			if (mListenerActionReceiver != null) {

				unregisterReceiver(mListenerActionReceiver);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}

		if(mRecorder != null){

			try{

				int state = mRecorder.getState();

				if(state == AudioRecord.STATE_INITIALIZED) {

					mRecorder.stop();
				}
			}
			catch (Exception e){

				e.printStackTrace();
			}

			try{

				mRecorder.release();
			}
			catch (Exception e){

				e.printStackTrace();
			}

			mRecorder = null;
		}
	}
	

	private void checkTriggerFingerprint() {

		System.out.println("SoundListenerService.checkTriggerFingerprint()");

		try {

			Resample resample = new Resample();
			resample.create(RECORDING_SAMPLE_RATE, FINGERPRINT_SAMPLE_RATE, 1024, 1);

			int output_len = resample.resample(triggerByteBuffer, resampledTriggerByteBuffer, resampledTriggerByteBuffer.remaining());

			if(output_len <= 0) {

				mRecorderState = LISTENING;
			}
			else{

				resampledTriggerByteBuffer.rewind();

				mSimilarity = getSimilarity(referenceTriggerByteBuffer, FINGERPRINT_TRIGGER_BUFFER_SIZE / 2, resampledTriggerByteBuffer, FINGERPRINT_TRIGGER_BUFFER_SIZE / 2);

				System.out.println("similarity = " + mSimilarity);

				if (mSimilarity < SIMILARITY_THRESHOLD) {

					mRecorderState = LISTENING;
				}
			}

			resample.destroy();
		}
		catch (Exception e) {

			e.printStackTrace();
		}

		referenceTriggerByteBuffer.rewind();
		resampledTriggerByteBuffer.clear();
		resampledTriggerByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}


	private void searchTuneUrlFingerprint(){

		System.out.println("SoundListenerService.searchTuneUrlFingerprint()");

		try {

			Resample resample = new Resample();
			resample.create(RECORDING_SAMPLE_RATE, FINGERPRINT_SAMPLE_RATE, 1024, 1);

			int output_len = resample.resample(tuneUrlByteBuffer, resampledTuneUrlByteBuffer, resampledTuneUrlByteBuffer.remaining());

			if(output_len <= 0) {

				startListening();
			}
			else {

				String fingerprint_string = extractFingerprintFromByteBuffer(resampledTuneUrlByteBuffer, TUNE_URL_WAVE_LENGHT);

				Intent i = new Intent(this.getApplicationContext(), APIService.class);
				i.putExtra(ACTION, ACTION_SEARCH_FINGERPRINT);
				i.putExtra(FINGERPRINT, fingerprint_string);
				startService(i);
			}

			resample.destroy();
		}
		catch (Exception e){

			e.printStackTrace();
		}

		resampledTuneUrlByteBuffer.clear();
		resampledTuneUrlByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}


	private String extractFingerprintFromByteBuffer(ByteBuffer byteBuffer, int waveLength) {

		System.out.println("SoundListenerService.extractFingerprintFromByteBuffer()");

		String fingerprint = "";

		try {

			byte[] result_raw = extractFingerprint(byteBuffer, waveLength);

			String result = "";

			for(int i=0; i<result_raw.length; i++){

				result = result + (result_raw[i] & 0xff);

				if(i < result_raw.length - 1){

					result = result + ",";
				}
			}

			fingerprint = result;
		}
		catch (Exception e) {

			e.printStackTrace();
		}

		return fingerprint;
	}


	public native byte[] extractFingerprint(ByteBuffer byteBuffer, int waveLength);

	public native float getSimilarity(ByteBuffer byteBuffer1, int waveLength1, ByteBuffer byteBuffer2, int waveLength2);
}