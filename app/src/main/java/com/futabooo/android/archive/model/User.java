package com.futabooo.android.archive.model;

import com.ekchang.jsouper.SoupQuery;

@SoupQuery("span.log_list_username") public class User {
  private final String name;

  public User(String name) {
    this.name = name;
  }
}
