package com.dekidea.tuneurl.di.module;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.dekidea.tuneurl.di.key.ViewModelKey;
import com.dekidea.tuneurl.view_model.FactoryViewModel;
import com.dekidea.tuneurl.view_model.SavedInfoViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SavedInfoViewModel.class)
    abstract ViewModel bindNewsListViewModel(SavedInfoViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(FactoryViewModel factory);
}
