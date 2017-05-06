package com.futabooo.android.booklife.screen.search;

public enum ActionType {
  READING("now"), TO_READ("pre"), READ("read"), QUITTED("tun"),;

  private final String type;

  public String getAddBookParam() {
    return this.type;
  }

  ActionType(String type) {
    this.type = type;
  }
}
