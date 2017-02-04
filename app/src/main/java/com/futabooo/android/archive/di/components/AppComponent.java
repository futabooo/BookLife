package com.futabooo.android.archive.di.components;

import com.futabooo.android.archive.Archive;
import com.futabooo.android.archive.di.modules.AppModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class }) public interface AppComponent {
  void inject(Archive archive);
}
