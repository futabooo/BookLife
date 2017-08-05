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
    set

  override fun getItem(position: Int): Fragment {
    when (bottomMenu) {
      MainBottomMenu.HOME -> return HomeFragment.newInstance()
      MainBottomMenu.BOOK -> {
        val bookListMenu = BookListMenu.fromPosition(position)
        return BookListFragment.newInstance(bookListMenu)
      }
      else -> return Fragment()
    }
  }

  override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE

  override fun getCount(): Int {
    when (bottomMenu) {
      MainBottomMenu.HOME -> return 1
      MainBottomMenu.BOOK -> return 4
      else -> return 0
    }
  }

  override fun getPageTitle(position: Int): CharSequence {
    when (bottomMenu) {
      MainBottomMenu.HOME -> return context.getString(R.string.home)
      MainBottomMenu.BOOK -> {
        val bookListMenu = BookListMenu.fromPosition(position)
        return context.getString(bookListMenu!!.titleResId)
      }
      else -> return ""
    }
  }

}
