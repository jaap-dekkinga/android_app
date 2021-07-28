package com.dekidea.tuneurl.di.module;

import com.dekidea.tuneurl.receiver.ConnectivityChangeReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract ConnectivityChangeReceiver contributeConnectivityChangeReceiver();
}
