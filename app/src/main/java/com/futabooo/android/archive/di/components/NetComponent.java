package com.futabooo.android.archive.di.components;

import com.futabooo.android.archive.LoginActivity;
import com.futabooo.android.archive.MainActivity;
import com.futabooo.android.archive.di.modules.AppModule;
import com.futabooo.android.archive.di.modules.NetModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class, NetModule.class }) public interface NetComponent {
  void inject(MainActivity mainActivity);
  void inject(LoginActivity loginActivity);
}
