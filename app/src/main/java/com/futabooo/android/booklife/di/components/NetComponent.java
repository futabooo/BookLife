package com.futabooo.android.booklife.di.components;

import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity;
import com.futabooo.android.booklife.screen.home.HomeFragment;
import com.futabooo.android.booklife.di.modules.AppModule;
import com.futabooo.android.booklife.di.modules.NetModule;
import com.futabooo.android.booklife.screen.booklist.BookListFragment;
import com.futabooo.android.booklife.screen.login.LoginActivity;
import com.futabooo.android.booklife.screen.splash.SplashActivity;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = { AppModule.class, NetModule.class }) public interface NetComponent {
  void inject(SplashActivity splashActivity);
  void inject(HomeFragment homeFragment);
  void inject(LoginActivity loginActivity);
  void inject(BookListFragment bookListActivity);
  void inject(BookDetailActivity bookDetailActivity);
}
