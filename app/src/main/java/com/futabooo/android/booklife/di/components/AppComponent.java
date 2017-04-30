package com.futabooo.android.booklife.di.components;

import android.content.SharedPreferences;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.di.modules.AppModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class }) public interface AppComponent {
  void inject(BookLife bookLife);
  SharedPreferences sharedPreferences();
}
