package com.futabooo.android.booklife.di.modules

import android.app.Application
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.futabooo.android.booklife.BuildConfig
import com.futabooo.android.booklife.HostSelectionInterceptor
import com.futabooo.android.booklife.LoggingInterceptor
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module class NetModule(val baseUrl: String) {

  @Provides @Singleton fun provideOkHttpCache(application: Application): Cache {
    val cacheSize = 10 * 1024 * 1024 // 10 MiB
    val cache = Cache(application.cacheDir, cacheSize.toLong())
    return cache
  }

  @Singleton @Provides fun provideHostSelectionInterceptor(): HostSelectionInterceptor = HostSelectionInterceptor()

  @Singleton @Provides fun provideLoggingInterceptor(): LoggingInterceptor = LoggingInterceptor()

  @Singleton @Provides fun providePersistentCookieJar(application: Application): PersistentCookieJar =
      PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(application))


  @Provides @Singleton fun provideOkHttpClient(cache: Cache,
                                               hostSelectionInterceptor: HostSelectionInterceptor,
                                               loggingInterceptor: LoggingInterceptor,
                                               persistentCookieJar: PersistentCookieJar): OkHttpClient {
    val builder = OkHttpClient.Builder().cookieJar(persistentCookieJar).cache(cache).addInterceptor(
        hostSelectionInterceptor)

    if (BuildConfig.DEBUG) {
      builder.addInterceptor(loggingInterceptor)
    }

    return builder.build()
  }

  @Provides @Singleton fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val retrofit = Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient).addCallAdapterFactory(
        RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create())
        //.addConverterFactory(JsoupConverterFactory.create())
        .build()
    return retrofit
  }
}
