package com.dekidea.tuneurl.util;

public interface SoundMeter {

    public static final int RECORDER_SAMPLERATE = 44100;

    public void start();

    public void stop();

    public void pause();

    public double getAmplitude();
}
