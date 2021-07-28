package com.dekidea.tuneurl.di.module;

import com.dekidea.tuneurl.service.APIService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract APIService contributeAPIService();
}
