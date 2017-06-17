package com.futabooo.android.booklife.screen;

import com.futabooo.android.booklife.R;

public enum BookListMenu {
  READ(0, R.string.book_read,"read"),
  READING(1, R.string.book_reading,"reading"),
  TO_READ(2, R.string.book_to_read,"wish"),
  QUITTED(3, R.string.book_quitted,"stacked");

  private final int position;
  private final int titleResId;
  private final String key;

  BookListMenu(int position, int titleResId, String key){
    this.position = position;
    this.titleResId = titleResId;
    this.key = key;
  }

  public static BookListMenu fromPosition(int position) {
    for (BookListMenu bookListMenu : values()) {
      if (position == bookListMenu.getPosition()) {
        return bookListMenu;
      }
    }
    return null;
  }

  public int getPosition() {
    return position;
  }

  public int getTitleResId() {
    return titleResId;
  }

  public String getKey() {
    return key;
  }
}
