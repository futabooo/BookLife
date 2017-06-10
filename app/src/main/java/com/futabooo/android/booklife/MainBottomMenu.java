package com.futabooo.android.booklife;

import android.support.design.widget.TabLayout;
import android.view.View;

public enum MainBottomMenu {
  HOME(0, R.string.home, View.GONE, TabLayout.MODE_FIXED),
  BOOK(1, R.string.book, View.VISIBLE, TabLayout.MODE_FIXED);

  private int position;
  private int titleId;
  private int tabLayoutVisibility;
  private int tabLayoutMode;

  MainBottomMenu(int position, int titleId, int tabLayoutVisibility, int tabLayoutMode) {
    this.position = position;
    this.titleId = titleId;
    this.tabLayoutVisibility = tabLayoutVisibility;
    this.tabLayoutMode = tabLayoutMode;
  }

  public static MainBottomMenu fromPosition(int position) {
    for (MainBottomMenu bottomMenu : values()) {
      if (position == bottomMenu.getPosition()) {
        return bottomMenu;
      }
    }
    return null;
  }

  public int getPosition() {
    return position;
  }

  public int getTitleId() {
    return titleId;
  }

  public int getTabLayoutVisibility() {
    return tabLayoutVisibility;
  }

  public int getTabLayoutMode() {
    return tabLayoutMode;
  }
}
