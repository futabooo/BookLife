package com.futabooo.android.booklife.screen

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import com.futabooo.android.booklife.MainBottomMenu
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.screen.booklist.BookListFragment
import com.futabooo.android.booklife.screen.home.HomeFragment

class MainViewPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

  var bottomMenu: MainBottomMenu = MainBottomMenu.HOME

  override fun getItem(position: Int): Fragment {
    return when (bottomMenu) {
      MainBottomMenu.HOME -> HomeFragment.newInstance()
      MainBottomMenu.BOOK -> {
        val bookListMenu = BookListMenu.fromPosition(position)
        BookListFragment.newInstance(bookListMenu)
      }
    }
  }

  override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

  override fun getCount(): Int {
    return when (bottomMenu) {
      MainBottomMenu.HOME -> 1
      MainBottomMenu.BOOK -> 4
    }
  }

  override fun getPageTitle(position: Int): CharSequence {
    return when (bottomMenu) {
      MainBottomMenu.HOME -> context.getString(R.string.home)
      MainBottomMenu.BOOK -> {
        val bookListMenu = BookListMenu.fromPosition(position)
        context.getString(bookListMenu.titleResId)
      }
    }
  }

}
