package com.futabooo.android.archive.model;

import com.ekchang.jsouper.SoupQuery;

@SoupQuery("body") public class Body {
  public final String body;

  public Body(String body) {
    this.body = body;
  }
}
