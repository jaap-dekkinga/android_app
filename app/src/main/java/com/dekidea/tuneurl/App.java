package com.dekidea.tuneurl;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.app.Fragment;

import dagger.android.AndroidInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;
import com.dekidea.tuneurl.di.component.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;


public class App extends Application implements HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingBroadcastReceiverInjector;


    public static Context context;

    @Override
    public void onCreate() {

        super.onCreate();

        this.initDagger();

        context = getApplicationContext();
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {

        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {

        return dispatchingFragmentInjector;
    }

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {

        return dispatchingServiceInjector;
    }

    @Override
    public DispatchingAndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {

        return dispatchingBroadcastReceiverInjector;
    }

    // ---

    private void initDagger(){

        DaggerAppComponent.builder().application(this).build().inject(this);
    }
}
