package com.dekidea.tuneurl.di.module;

import com.dekidea.tuneurl.activity.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();
}
