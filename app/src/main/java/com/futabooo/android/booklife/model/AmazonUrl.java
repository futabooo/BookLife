package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class AmazonUrl {

  private String outline;
  private String registration;
  @SerializedName("wish_book") private String wishBook;

  public AmazonUrl(String outline, String registration, String wishBook) {
    this.outline = outline;
    this.registration = registration;
    this.wishBook = wishBook;
  }

  public String getOutline() {
    return outline;
  }

  public void setOutline(String outline) {
    this.outline = outline;
  }

  public String getRegistration() {
    return registration;
  }

  public void setRegistration(String registration) {
    this.registration = registration;
  }

  public String getWishBook() {
    return wishBook;
  }

  public void setWishBook(String wishBook) {
    this.wishBook = wishBook;
  }
}
