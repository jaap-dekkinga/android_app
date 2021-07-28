package com.dekidea.tuneurl.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;

public class SoundMeterExternal implements SoundMeter, Constants{

	private String mRecordingPath;
	private MediaRecorder mRecorder;
	
	public SoundMeterExternal(Context context){

		mRecorder = null;

		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

			mRecordingPath = "/dev/null";
		}
		else{

			mRecordingPath = context.getFilesDir().getPath() + "/" + AUDIO_RECORDER_FOLDER + "/" + RECORDED_TEMP_FILE;
		}
	}


	public void start() {
		
		if (mRecorder == null) {
				
			try{
					
				mRecorder = new MediaRecorder();
				mRecorder.reset();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			    mRecorder.setOutputFile(mRecordingPath);
			    mRecorder.prepare();
			    mRecorder.start();
			}
			catch(Exception e){
					
				e.printStackTrace();

				try {

					stop();
				}
				catch(Exception ex) {

					ex.printStackTrace();
				}

				try {

					start();
				}
				catch(Exception ex) {

					ex.printStackTrace();
				}
			}
		}
	}
	
	
	public void stop() {
		
		if (mRecorder != null) {
			
			try{

				mRecorder.stop();				
			}
			catch(Exception e){
					
				e.printStackTrace();
			}

            try{

                mRecorder.reset();
            }
            catch(Exception e){

                e.printStackTrace();
            }

            try{

                mRecorder.release();
            }
            catch(Exception e){

                e.printStackTrace();
            }

			mRecorder = null;
		}
	}

	@Override
	public void pause() {

		stop();
	}

	public double getAmplitude() {
		
		double db = 0;

		try {

            if (mRecorder != null) {

                double amplitude = (double) mRecorder.getMaxAmplitude();

                if (amplitude > 0) {

                    db = (20d * Math.log10(amplitude * 10));
                }

                System.out.println("Log amplitude: " + (int) db);
            }
        }
        catch (Exception e){

		    e.printStackTrace();
        }

		return db;
	}
}