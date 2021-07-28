// Generated by Dagger (https://google.github.io/dagger).
package com.dekidea.tuneurl.di.module;

import com.google.gson.Gson;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class AppModule_ProvideGsonFactory implements Factory<Gson> {
  private final AppModule module;

  public AppModule_ProvideGsonFactory(AppModule module) {
    this.module = module;
  }

  @Override
  public Gson get() {
    return provideInstance(module);
  }

  public static Gson provideInstance(AppModule module) {
    return proxyProvideGson(module);
  }

  public static AppModule_ProvideGsonFactory create(AppModule module) {
    return new AppModule_ProvideGsonFactory(module);
  }

  public static Gson proxyProvideGson(AppModule instance) {
    return Preconditions.checkNotNull(
        instance.provideGson(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
