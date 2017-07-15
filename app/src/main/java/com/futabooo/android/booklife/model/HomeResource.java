package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class HomeResource {
  private int id;
  private String path;
  @SerializedName("created_at") private String createdAt;
  private boolean deletable;
  @SerializedName("header_tag") private String headerTag;
  private Contents contents;
  private User user;

  public HomeResource(int id, String path, String createdAt, boolean deletable, String headerTag, Contents contents,
      User user) {
    this.id = id;
    this.path = path;
    this.createdAt = createdAt;
    this.deletable = deletable;
    this.headerTag = headerTag;
    this.contents = contents;
    this.user = user;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isDeletable() {
    return deletable;
  }

  public void setDeletable(boolean deletable) {
    this.deletable = deletable;
  }

  public String getHeaderTag() {
    return headerTag;
  }

  public void setHeaderTag(String headerTag) {
    this.headerTag = headerTag;
  }

  public Contents getContents() {
    return contents;
  }

  public void setContents(Contents contents) {
    this.contents = contents;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
