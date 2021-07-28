package com.dekidea.tuneurl.di.module;

import android.app.Service;
import com.dekidea.tuneurl.service.APIService;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.ServiceKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = ServiceModule_ContributeAPIService.APIServiceSubcomponent.class)
public abstract class ServiceModule_ContributeAPIService {
  private ServiceModule_ContributeAPIService() {}

  @Binds
  @IntoMap
  @ServiceKey(APIService.class)
  abstract AndroidInjector.Factory<? extends Service> bindAndroidInjectorFactory(
      APIServiceSubcomponent.Builder builder);

  @Subcomponent
  public interface APIServiceSubcomponent extends AndroidInjector<APIService> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<APIService> {}
  }
}
