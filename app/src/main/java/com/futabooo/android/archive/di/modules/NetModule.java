package com.futabooo.android.archive.di.modules;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.futabooo.android.archive.LoggingInterceptor;
import com.futabooo.android.archive.BuildConfig;
import com.futabooo.android.archive.HostSelectionInterceptor;
import com.futabooo.android.archive.LoggingInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import dagger.Module;
import dagger.Provides;
import java.net.CookieManager;
import javax.inject.Singleton;
import okhttp3.Cache;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module public class NetModule {

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

  @Singleton @Provides public HostSelectionInterceptor provideHostSelectionInterceptor() {
    return new HostSelectionInterceptor();
  }

  @Singleton @Provides public LoggingInterceptor provideLoggingInterceptor() {
    return new LoggingInterceptor();
  }

  @Singleton @Provides public JavaNetCookieJar provideJavaNetCookieJar(CookieManager cookieManager) {
    return new JavaNetCookieJar(cookieManager);
  }

  @Singleton @Provides public PersistentCookieJar providePersistentCookieJar(Application application) {
    return new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application));
  }

  @Singleton @Provides public CookieManager provideCookieManager() {
    return new CookieManager();
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
        //.addConverterFactory(JsoupConverterFactory.create())
        .build();
    return retrofit;
  }
}
