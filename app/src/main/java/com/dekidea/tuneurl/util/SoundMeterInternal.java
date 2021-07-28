package com.dekidea.tuneurl.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.widget.Toast;

import com.dekidea.tuneurl.R;

public class SoundMeterInternal implements SoundMeter, Constants{

    private Context mContext;

    private int bufferSize = 0;
    private byte[] data;

    private AudioRecord mRecorder;

    public SoundMeterInternal(Context context){

        mContext = context;

        mRecorder = null;
    }


    public void start() {

        if (mRecorder == null) {

            try{

                bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4;
                data = new byte[bufferSize];

                if (Settings.useInternalAudio()) {

                    AudioPlaybackCaptureConfiguration config = Settings.getAudioPlaybackCaptureConfiguration();

                    if(config != null) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            mRecorder = new AudioRecord.Builder()
                                    .setAudioPlaybackCaptureConfig(config)
                                    .setAudioFormat(new AudioFormat.Builder()
                                            .setSampleRate(RECORDER_SAMPLERATE)
                                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                            .build())
                                    .setBufferSizeInBytes(bufferSize)
                                    .build();

                            System.out.println("mRecorder != null: " + (mRecorder != null));
                        }
                        else{

                            mRecorder = getDefaultAudioRecord();
                        }
                    }
                    else{

                        mRecorder = getDefaultAudioRecord();
                    }
                }
                else {

                    mRecorder = getDefaultAudioRecord();
                }

                if(mRecorder != null) {

                    int i = mRecorder.getState();

                    if (i == AudioRecord.STATE_INITIALIZED) {

                        mRecorder.startRecording();
                    }
                }
                else{

                    Toast.makeText(mContext, R.string.could_not_create_audio_record_object, Toast.LENGTH_LONG).show();
                }
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
        else{

            try {

                int i = mRecorder.getState();

                if (i == AudioRecord.STATE_INITIALIZED || i == AudioRecord.RECORDSTATE_STOPPED) {

                    System.out.println("SoundMeterInternal.start(): mRecorder.startRecording()");

                    mRecorder.startRecording();
                }
            }
            catch(Exception ex) {

                ex.printStackTrace();
            }
        }
    }


    private AudioRecord getDefaultAudioRecord(){

        return new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }


    public void stop() {

        System.out.println("SoundMeterInternal.stop()");

        if (mRecorder != null) {

            try{

                mRecorder.stop();
            }
            catch(Exception e){

                e.printStackTrace();
            }

            try {

                mRecorder.release();
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            mRecorder = null;
        }
    }


    public void pause() {

        System.out.println("SoundMeterInternal.pause()");

        if (mRecorder != null) {

            try{

                mRecorder.stop();
            }
            catch(Exception e){

                e.printStackTrace();
            }
        }
    }


    public double getAmplitude() {

        double db = 0;

        int amplitude = 0;

        try {

            if (mRecorder != null && (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {



                int read = mRecorder.read(data, 0, bufferSize);

                for (int i=0; i<read/2; i++) {

                    short curSample = getShort(data[i*2], data[i*2+1]);

                    if (curSample > amplitude) {

                        amplitude = curSample;
                    }
                }

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


    private short getShort(byte argB1, byte argB2) {

        return (short)(argB1 | (argB2 << 8));
    }
}