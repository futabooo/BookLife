package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class Contents {
  private Book book;
  @SerializedName("image_url") private String imageUrl;

  public Contents(Book book, String imageUrl) {
    this.book = book;
    this.imageUrl = imageUrl;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
