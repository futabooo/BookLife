package com.futabooo.android.booklife.screen;

public enum BookListMenu {
  READ(0, "READ","read"),
  READING(1,"READING","reading"),
  TO_READ(2, "TO READ","wish"),
  QUITTED(3, "QUITTED","stacked");

  private final int position;
  private final String title;
  private final String key;

  BookListMenu(int position, String title, String key){
    this.position = position;
    this.title = title;
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

  public String getTitle() {
    return title;
  }

  public String getKey() {
    return key;
  }
}
