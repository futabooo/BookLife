package com.futabooo.android.archive.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class NetModule {

  String baseUrl;

  // Constructor needs one parameter to instantiate.
  public NetModule(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  // Dagger will only look for methods annotated with @Provides
  @Provides @Singleton
  // Application reference must come from AppModule.class
  SharedPreferences providesSharedPreferences(Application application) {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }

  @Provides @Singleton Cache provideOkHttpCache(Application application) {
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(application.getCacheDir(), cacheSize);
    return cache;
  }

  @Provides @Singleton OkHttpClient provideOkHttpClient(Cache cache) {
    OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
    return client;
  }

  @Provides @Singleton Retrofit provideRetrofit(OkHttpClient okHttpClient) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).build();
    return retrofit;
  }
}
