package com.dekidea.tuneurl.di.module;

import com.dekidea.tuneurl.fragment.SavedInfoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract SavedInfoFragment contributeSavedInfoFragment();
}
