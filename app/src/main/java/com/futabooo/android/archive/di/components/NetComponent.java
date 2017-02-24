package com.futabooo.android.archive.di.components;

import com.futabooo.android.archive.screen.home.HomeActivity;
import com.futabooo.android.archive.di.modules.AppModule;
import com.futabooo.android.archive.di.modules.NetModule;
import com.futabooo.android.archive.screen.booklist.BookListActivity;
import com.futabooo.android.archive.screen.login.LoginActivity;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class, NetModule.class }) public interface NetComponent {
  void inject(HomeActivity mainActivity);
  void inject(LoginActivity loginActivity);
  void inject(BookListActivity bookListActivity);
}
