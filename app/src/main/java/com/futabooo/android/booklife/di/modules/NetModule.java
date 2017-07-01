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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

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

    // Create a trust manager that does not validate certificate chains
    final TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
              throws CertificateException {
          }

          @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
              throws CertificateException {
          }

          @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
          }
        }
    };

    try {
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
      builder.hostnameVerifier(new HostnameVerifier() {
        @Override public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      Timber.e(e);
    }

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
