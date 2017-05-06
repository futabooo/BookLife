package com.futabooo.android.booklife.di.modules;

import android.app.Application;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.futabooo.android.booklife.BuildConfig;
import com.futabooo.android.booklife.HostSelectionInterceptor;
import com.futabooo.android.booklife.LoggingInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module public class NetModule {

  String baseUrl;

  // Constructor needs one parameter to instantiate.
  public NetModule(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @Provides @Singleton Cache provideOkHttpCache(Application application) {
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(application.getCacheDir(), cacheSize);
    return cache;
  }

  @Singleton @Provides public HostSelectionInterceptor provideHostSelectionInterceptor() {
    return new HostSelectionInterceptor();
  }

  @Singleton @Provides public LoggingInterceptor provideLoggingInterceptor() {
    return new LoggingInterceptor();
  }

  @Singleton @Provides public PersistentCookieJar providePersistentCookieJar(Application application) {
    return new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application));
  }

  @Provides @Singleton OkHttpClient provideOkHttpClient(Cache cache, HostSelectionInterceptor hostSelectionInterceptor,
      LoggingInterceptor loggingInterceptor, PersistentCookieJar persistentCookieJar) {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder().cookieJar(persistentCookieJar).cache(cache).addInterceptor(hostSelectionInterceptor);

    if (BuildConfig.DEBUG) {
      builder.addInterceptor(loggingInterceptor);
    }

    return builder.build();
  }

  @Provides @Singleton Retrofit provideRetrofit(OkHttpClient okHttpClient) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        //.addConverterFactory(JsoupConverterFactory.create())
        .build();
    return retrofit;
  }
}
