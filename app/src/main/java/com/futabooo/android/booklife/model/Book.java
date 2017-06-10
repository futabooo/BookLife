package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class Book {
  Long id;
  String title;
  @SerializedName("image_url")
  String imageUrl;
  String path;
  int page;
  boolean original;
  @SerializedName("registration_count")
  int registrationCount;

  public Book(Long id, String title, String imageUrl, String path, int page, boolean original, int registrationCount) {
    this.id = id;
    this.title = title;
    this.imageUrl = imageUrl;
    this.path = path;
    this.page = page;
    this.original = original;
    this.registrationCount = registrationCount;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public boolean isOriginal() {
    return original;
  }

  public void setOriginal(boolean original) {
    this.original = original;
  }

  public int getRegistrationCount() {
    return registrationCount;
  }

  public void setRegistrationCount(int registrationCount) {
    this.registrationCount = registrationCount;
  }
}
