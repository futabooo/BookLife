package com.futabooo.android.archive.di.components;

import com.futabooo.android.archive.di.modules.AppModule;
import com.futabooo.android.archive.di.modules.NetModule;
import dagger.Component;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Singleton @Component(modules = { AppModule.class, NetModule.class }) public interface NetComponent {
  Retrofit retrofit();
  OkHttpClient okHttpClient();
}
