package com.dekidea.tuneurl.di.component;

import android.app.Application;

import com.dekidea.tuneurl.App;
import com.dekidea.tuneurl.di.module.ActivityModule;
import com.dekidea.tuneurl.di.module.AppModule;
import com.dekidea.tuneurl.di.module.BroadcastReceiverModule;
import com.dekidea.tuneurl.di.module.FragmentModule;
import com.dekidea.tuneurl.di.module.ServiceModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;


@Singleton
@Component(modules={AndroidSupportInjectionModule.class, AppModule.class, ActivityModule.class, FragmentModule.class,
        ServiceModule.class, BroadcastReceiverModule.class})

public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }

    void inject(App app);
}
