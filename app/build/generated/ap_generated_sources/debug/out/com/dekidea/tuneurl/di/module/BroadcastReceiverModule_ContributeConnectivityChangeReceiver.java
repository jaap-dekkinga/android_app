package com.dekidea.tuneurl.di.module;

import android.content.BroadcastReceiver;
import com.dekidea.tuneurl.receiver.ConnectivityChangeReceiver;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.BroadcastReceiverKey;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents =
      BroadcastReceiverModule_ContributeConnectivityChangeReceiver
          .ConnectivityChangeReceiverSubcomponent.class
)
public abstract class BroadcastReceiverModule_ContributeConnectivityChangeReceiver {
  private BroadcastReceiverModule_ContributeConnectivityChangeReceiver() {}

  @Binds
  @IntoMap
  @BroadcastReceiverKey(ConnectivityChangeReceiver.class)
  abstract AndroidInjector.Factory<? extends BroadcastReceiver> bindAndroidInjectorFactory(
      ConnectivityChangeReceiverSubcomponent.Builder builder);

  @Subcomponent
  public interface ConnectivityChangeReceiverSubcomponent
      extends AndroidInjector<ConnectivityChangeReceiver> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ConnectivityChangeReceiver> {}
  }
}
