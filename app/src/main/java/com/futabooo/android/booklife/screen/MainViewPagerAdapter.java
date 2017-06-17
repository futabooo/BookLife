package com.futabooo.android.booklife.screen;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.futabooo.android.booklife.MainBottomMenu;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.screen.booklist.BookListFragment;
import com.futabooo.android.booklife.screen.home.HomeFragment;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

  private Context context;
  private MainBottomMenu bottomMenu;

  public MainViewPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
    this.context = context;
    this.bottomMenu = MainBottomMenu.HOME;
  }

  @Override public Fragment getItem(int position) {
    switch (bottomMenu) {
      case HOME:
        return HomeFragment.newInstance();
      case BOOK:
        BookListMenu bookListMenu = BookListMenu.fromPosition(position);
        return BookListFragment.newInstance(bookListMenu);
      default:
        return new Fragment();
    }
  }

  @Override public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  @Override public int getCount() {
    switch (bottomMenu) {
      case HOME:
        return 1;
      case BOOK:
        return 4;
      default:
        return 0;
    }
  }

  @Override public CharSequence getPageTitle(int position) {
    switch (bottomMenu) {
      case HOME:
        return context.getString(R.string.home);
      case BOOK:
        BookListMenu bookListMenu = BookListMenu.fromPosition(position);
        return context.getString(bookListMenu.getTitleResId());
      default:
        return "";
    }
  }

  public void setBottomMenu(MainBottomMenu bottomMenu) {
    this.bottomMenu = bottomMenu;
  }
}
