package com.futabooo.android.booklife.di.components;

import com.futabooo.android.booklife.screen.home.HomeActivity;
import com.futabooo.android.booklife.di.modules.AppModule;
import com.futabooo.android.booklife.di.modules.NetModule;
import com.futabooo.android.booklife.screen.booklist.BookListActivity;
import com.futabooo.android.booklife.screen.login.LoginActivity;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class, NetModule.class }) public interface NetComponent {
  void inject(HomeActivity mainActivity);
  void inject(LoginActivity loginActivity);
  void inject(BookListActivity bookListActivity);
}
