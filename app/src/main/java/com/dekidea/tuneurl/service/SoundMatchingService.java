package com.dekidea.tuneurl.service;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.FileUtils;
import com.dekidea.tuneurl.util.Settings;
import com.dekidea.tuneurl.util.TimeUtils;
import com.dekidea.tuneurl.util.WakeLocker;
import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.provider.DocumentFile;

public class SoundMatchingService extends IntentService implements Constants {
	
	private final int RECORDER_BPP = 16;
	private final int TRIGGER_SAMPLE_RATE = 44100;
	private final int AUDIO_LABEL_SAMPLE_RATE = 10240;

	private final int RECORDING_TRIGGER_INTERVAL_IN_MILLIS = 1450;
	private final int AUDIO_LABEL_SIZE_MILLIS = 5000;
	private final int RECORDING_AUDIO_LABEL_SAFETY_INTERVAL_IN_MILLIS = 400;

	private final int AUDIO_LABEL_STANDARD_FILE_SIZE = (int)((double)AUDIO_LABEL_SAMPLE_RATE * ((double)RECORDER_BPP / 8d) * ((double)AUDIO_LABEL_SIZE_MILLIS / 1000d));

	private final float SIMILARITY_TRESHOLD = 0.22F;


	private int mTriggerBufferSize = 0;
	private int mAudioLabelBufferSize = 0;

	private AudioRecord mTriggerRecorder = null;
	private AudioRecord mAudioLabelRecorder;
	private short[] mBuffer;
	private File mRawFile;

	private boolean mIsRecordingTrigger;
	private boolean mIsRecordingAudioLabel;
	private boolean mCanTriggerInvite;

	private long mStartTime;
	private long mStartRecordingTime;


	static {

		System.loadLibrary("native-lib");
	}


	public SoundMatchingService() {

		super("SoundMatchingService");
	}


	public SoundMatchingService(String name) {

		super(name);
	}


	@Override
	protected void onHandleIntent(Intent intent) {

		try {

			int action = intent.getIntExtra(ACTION, -1);

			if(action == ACTION_STOP_SERVICE ){

				try{

					if(mTriggerRecorder != null){

						mIsRecordingTrigger = false;

						int i = mTriggerRecorder.getState();

						if(i == AudioRecord.STATE_INITIALIZED){

							mTriggerRecorder.stop();
						}

						mTriggerRecorder.release();

						mTriggerRecorder = null;
					}
				}
				catch (Exception e){

					e.printStackTrace();
				}

				try{

					if(mIsRecordingAudioLabel){

						mIsRecordingAudioLabel = false;

						if(mAudioLabelRecorder != null){

							try{

								int i = mAudioLabelRecorder.getState();

								if(i == AudioRecord.STATE_INITIALIZED){

									mAudioLabelRecorder.stop();
								}

								mAudioLabelRecorder.release();

								mAudioLabelRecorder = null;
							}
							catch(IllegalStateException e){

								e.printStackTrace();
							}
						}

						WakeLocker.release();
					}
				}
				catch (Exception e){

					e.printStackTrace();
				}

				stopSelf();
			}
			else {

				WakeLocker.acquirePartialWakeLock(this.getApplicationContext());

				mIsRecordingTrigger = false;
				mIsRecordingAudioLabel = false;
				mCanTriggerInvite = false;

				mStartTime = intent.getLongExtra(TIME, 0);

				mTriggerBufferSize = AudioRecord.getMinBufferSize(TRIGGER_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4;
				mAudioLabelBufferSize = AudioRecord.getMinBufferSize(AUDIO_LABEL_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4;
						
				startRecordingTriggerSound();
			}
		}
		catch(Exception e){

			e.printStackTrace();
		}
	}


	@Override
	public void onDestroy(){

		super.onDestroy();
	}


	private void startRecordingTriggerSound(){

		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

		int audio_source = MediaRecorder.AudioSource.MIC;

		if(audioManager.isWiredHeadsetOn()){

			audio_source = MediaRecorder.AudioSource.DEFAULT;
		}

		mTriggerRecorder = new AudioRecord(audio_source,
				TRIGGER_SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mTriggerBufferSize);

		int i = mTriggerRecorder.getState();

		if(i == AudioRecord.STATE_INITIALIZED){

			mTriggerRecorder.startRecording();

			mStartRecordingTime = TimeUtils.getCurrentTimeInMillis();

			mIsRecordingTrigger = true;


			new Thread(new Runnable() {

				@Override
				public void run() {

					writeTriggerDataToFile();
				}

			}, "TriggerRecordingThread").start();


			TimerTask timerTask = new TimerTask() {

				public void run() {

					stopRecordingTriggerSound();
	            }
	        };


	        long interval = RECORDING_TRIGGER_INTERVAL_IN_MILLIS - (mStartRecordingTime - mStartTime);

	        Timer timer = new Timer();
	        timer.schedule(timerTask, interval);
		}
		else{

			mTriggerRecorder.release();

			mTriggerRecorder = null;
		}
	}


	private void writeTriggerDataToFile(){

		String filename = FileUtils.getTriggerTempFilePath(this);
		OutputStream fos = null;

		try {

			fos = new FileOutputStream(filename);
		}
		catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		byte data[] = new byte[mTriggerBufferSize];
		int read = 0;

		if (fos != null) {

			while (mIsRecordingTrigger) {

				read = mTriggerRecorder.read(data, 0, mTriggerBufferSize);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {

					try {

						fos.write(data);
					}
					catch (IOException e) {

						e.printStackTrace();
					}
				}
			}

			try {

				fos.flush();
				fos.close();
			}
			catch (IOException e) {

				e.printStackTrace();
			}
		}

		fos = null;
	}


	private void stopRecordingTriggerSound(){

		if(mTriggerRecorder != null){

			mIsRecordingTrigger = false;

			int i = mTriggerRecorder.getState();

			if(i == AudioRecord.STATE_INITIALIZED){

				mTriggerRecorder.stop();
			}

			mTriggerRecorder.release();

			mTriggerRecorder = null;
		}
		
		if(mAudioLabelBufferSize > 0) {

			startRecordingAudioLabel(mAudioLabelBufferSize);

			createTriggerWaveFile();

			getTriggerFingerprint();
		}
		else{

			deleteTriggerTempFile();

			Settings.startListening(this);
		}
	}


	private void createTriggerWaveFile(){

		String inFilePath = FileUtils.getTriggerTempFilePath(this);
		String outFilePath = FileUtils.getTriggerFilePath(this);

		FileInputStream in = null;
		OutputStream out = null;

		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = TRIGGER_SAMPLE_RATE;
		int channels = 1;
		long byteRate = RECORDER_BPP * TRIGGER_SAMPLE_RATE * channels/8;

		byte[] data = new byte[mTriggerBufferSize];

		try {

			in = new FileInputStream(inFilePath);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			out = new FileOutputStream(outFilePath);

			writeTriggerFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

			while(in.read(data) != -1){

				out.write(data);
			}

			in.close();
			out.close();
		}
		catch (FileNotFoundException e) {

			e.printStackTrace();
		}
		catch (IOException e) {

			e.printStackTrace();
		}

		deleteTriggerTempFile();
	}


	private void writeTriggerFileHeader(OutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte)(channels & 0xFF);
		header[23] = (byte) ((channels >> 8) & 0xFF);
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) ((channels * RECORDER_BPP) / 8);
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}


	private void startRecordingAudioLabel(int bufferSize){

		try {

			if (bufferSize > 0) {

				AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

				int audio_source = MediaRecorder.AudioSource.MIC;

				if(audioManager.isWiredHeadsetOn()){

					audio_source = MediaRecorder.AudioSource.DEFAULT;
				}

				mBuffer = new short[bufferSize];

				//mAudioLabelRecorder = new AudioRecord(audio_source, AUDIO_FILE_MP3_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
				mAudioLabelRecorder = new AudioRecord(audio_source, AUDIO_LABEL_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

				mIsRecordingAudioLabel = true;

				mAudioLabelRecorder.startRecording();

				mRawFile = getAudioLabelTempFile();

				new Thread(new Runnable() {

					@Override
					public void run() {

						writeAudioLabelDataToFile(mRawFile);
					}
				}).start();


				TimerTask timerTask = new TimerTask() {

					public void run() {

						stopRecordingAudioLabel();
					}
				};

				Timer timer = new Timer();
				timer.schedule(timerTask, AUDIO_LABEL_SIZE_MILLIS + RECORDING_AUDIO_LABEL_SAFETY_INTERVAL_IN_MILLIS);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}


	private void writeAudioLabelDataToFile(File file){

		DataOutputStream output = null;
		int file_size = 0;

		try {

			output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

			while (mIsRecordingAudioLabel) {

				byteBuffer.clear();
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				int len = mAudioLabelRecorder.read(byteBuffer, 1024);

				if(len > 0 && file_size < AUDIO_LABEL_STANDARD_FILE_SIZE) {

					if(len > (AUDIO_LABEL_STANDARD_FILE_SIZE - file_size)){

						len = AUDIO_LABEL_STANDARD_FILE_SIZE - file_size;
					}

					byte[] byte_array = byteBuffer.array();

					output.write(byte_array, 0, len);

					file_size = file_size + len;
				}
			}

			System.out.println("raw file size: " + file_size);
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		finally {

			if (output != null) {

				try {

					output.flush();
				}
				catch (IOException e) {

					e.printStackTrace();
				}
				finally {

					try {

						output.close();
					}
					catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
		}
	}


	private void stopRecordingAudioLabel(){

		if(mIsRecordingAudioLabel){

			mIsRecordingAudioLabel = false;

			if(mAudioLabelRecorder != null){

				try{

					int i = mAudioLabelRecorder.getState();

					if(i == AudioRecord.STATE_INITIALIZED){

						mAudioLabelRecorder.stop();
					}

					mAudioLabelRecorder.release();

					mAudioLabelRecorder = null;
				}
				catch(IllegalStateException e){
					
					e.printStackTrace();
				}
			}
			
			if(mCanTriggerInvite){

				searchFingerprint(mRawFile.getAbsolutePath());
			}
			else{
				
				mCanTriggerInvite = true;
			}
			
			WakeLocker.release();
		}
	}
	
	
	private File getAudioLabelTempFile() {
		
		String raw_file_path = FileUtils.getAudioLabelTempFilePath(this);
		
		return new File(raw_file_path);
	}
		 
	
	private void deleteTriggerRecordedFile() {
		
		File file = new File(FileUtils.getTriggerFilePath(this));
		 
		file.delete();
	}
	
	
	private void deleteTriggerTempFile() {

		try {

			File file = new File(FileUtils.getTriggerTempFilePath(this));

			file.delete();
		}
		catch (Exception e){

			e.printStackTrace();
		}
	}
	
	
	private void deleteAudioLabelRawFile() {
		
		if(mRawFile != null){
			
			if(mRawFile.exists()){
				
				mRawFile.delete();
			}
		}
	}


	private float getMaxSimilarity(Wave wave){

		float max_similarity = 0;

		Wave[] benchmark_waves = FileUtils.getReferenceBenchmarkWaves(this);

		for(int i=0; i<benchmark_waves.length; i++){

			try{

				if(benchmark_waves[i] != null && wave != null){

					FingerprintSimilarity fs = benchmark_waves[i].getFingerprintSimilarity(wave);

					float similarity = fs.getSimilarity();

					System.out.println(i + " - similarity = " + similarity);

					if(similarity > max_similarity){

						max_similarity = similarity;
					}
				}
			}
			catch(Exception e){

				e.printStackTrace();
			}
		}

		return max_similarity;
	}


	private void getTriggerFingerprint(){

		int running_state = Settings.fetchIntSetting(this, SETTING_RUNNING_STATE, SETTING_RUNNING_STATE_STOPPED);

		String recorded_trigger_file_path = FileUtils.getTriggerFilePath(this);

		File file = new File(recorded_trigger_file_path);

		if (file.exists()) {

			Wave wave = new Wave(recorded_trigger_file_path);

			float similarity = getMaxSimilarity(wave);

			wave = null;

			deleteTriggerRecordedFile();

			System.out.println("similarity = " + similarity);

			if(similarity > SIMILARITY_TRESHOLD && running_state == SETTING_RUNNING_STATE_STARTED){

				if(mCanTriggerInvite){

					searchFingerprint(mRawFile.getAbsolutePath());
				}
				else{

					mCanTriggerInvite = true;
				}
			}
			else if(running_state == SETTING_RUNNING_STATE_STARTED){

				stopRecordingAudioLabel();

				Settings.startListening(this);
			}
		}
		else{

			stopRecordingAudioLabel();

			if(running_state == SETTING_RUNNING_STATE_STARTED) {

				Settings.startListening(this);
			}
		}
	}


	private void searchFingerprint(String raw_file_path){

		String fingerprint_string = extractFingerprintFromFile(raw_file_path);

		try {

			int count = (int)Math.floor((double)(fingerprint_string.length()) / 2000d) + 1;

			for(int i=0; i<count; i++){

				String current = "";

				if(i == count - 1){

					current = fingerprint_string.substring(i * 2000);
				}
				else{

					current = fingerprint_string.substring(i * 2000, (i + 1) * 2000);
				}

				System.out.println(current);
			}
		}
		catch (Exception e){

			e.printStackTrace();
		}

		Intent i = new Intent(this.getApplicationContext(), APIService.class);
		i.putExtra(ACTION, ACTION_SEARCH_FINGERPRINT);
		i.putExtra(FINGERPRINT, fingerprint_string);
		startService(i);
	}


	private String extractFingerprintFromByteBuffer(String raw_file_path) {

		String fingerprint = "";

		try {

			File raw_file = new File(raw_file_path);
			int waveLength = (int)raw_file.length();

			InputStream inputStream = new FileInputStream(raw_file_path);
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputStream.available());
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			Channels.newChannel(inputStream).read(byteBuffer);

			byte[] result_raw = extractFingerprint(byteBuffer, waveLength);

			String result = "";

			for(int i=0; i<result_raw.length; i++){

				result = result + (result_raw[i] & 0xff);

				if(i < result_raw.length - 1){

					result = result + ",";
				}
			}

			fingerprint = result;

			deleteAudioLabelRawFile();
		}
		catch (Exception e) {

			e.printStackTrace();
		}

		return fingerprint;
	}


	private String extractFingerprintFromFile(String raw_file_path) {

		String fingerprint = "";

		try {

			byte[] result_raw = extractFingerprintFromRawFile(raw_file_path);

			String result = "";

			for(int i=0; i<result_raw.length; i++){

				result = result + (result_raw[i] & 0xff);

				if(i < result_raw.length - 1){

					result = result + ",";
				}
			}

			fingerprint = result;

			deleteAudioLabelRawFile();
		}
		catch (Exception e) {

			e.printStackTrace();
		}

		return fingerprint;
	}

	public native byte[] extractFingerprint(ByteBuffer byteBuffer, int waveLength);

	public native byte[] extractFingerprintFromRawFile(String filePath);
}