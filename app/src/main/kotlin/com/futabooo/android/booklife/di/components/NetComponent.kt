package com.futabooo.android.booklife.di.components

import com.futabooo.android.booklife.screen.addbook.AddBookDialogFragment
import com.futabooo.android.booklife.screen.bookdetail.BookDetailActivity
import com.futabooo.android.booklife.screen.home.HomeFragment
import com.futabooo.android.booklife.di.modules.AppModule
import com.futabooo.android.booklife.di.modules.NetModule
import com.futabooo.android.booklife.screen.booklist.BookListFragment
import com.futabooo.android.booklife.screen.login.LoginActivity
import com.futabooo.android.booklife.screen.search.SearchActivity
import com.futabooo.android.booklife.screen.splash.SplashActivity
import dagger.Component
import javax.inject.Singleton

@Singleton @Component(modules = arrayOf(AppModule::class, NetModule::class)) interface NetComponent {
  fun inject(splashActivity: SplashActivity)
  fun inject(homeFragment: HomeFragment)
  fun inject(loginActivity: LoginActivity)
  fun inject(bookListActivity: BookListFragment)
  fun inject(bookDetailActivity: BookDetailActivity)
  fun inject(searchActivity: SearchActivity)
  fun inject(addBookDialogFragment: AddBookDialogFragment)
}
