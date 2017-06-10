package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class BookDetailResource {
  private int id;
  private String path;
  private boolean deletable;
  @SerializedName("created_at") private String createdAt;
  @SerializedName("read_at") private String readAt;
  private Contents contents;
  private Owner owner;
  private Review review;

  public BookDetailResource(int id, String path, boolean deletable, String createdAt, String readAt, Contents contents,
      Owner owner, Review review) {
    this.id = id;
    this.path = path;
    this.deletable = deletable;
    this.createdAt = createdAt;
    this.readAt = readAt;
    this.contents = contents;
    this.owner = owner;
    this.review = review;
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

  public boolean isDeletable() {
    return deletable;
  }

  public void setDeletable(boolean deletable) {
    this.deletable = deletable;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getReadAt() {
    return readAt;
  }

  public void setReadAt(String readAt) {
    this.readAt = readAt;
  }

  public Contents getContents() {
    return contents;
  }

  public void setContents(Contents contents) {
    this.contents = contents;
  }

  public Owner getOwner() {
    return owner;
  }

  public void setOwner(Owner owner) {
    this.owner = owner;
  }

  public Review getReview() {
    return review;
  }

  public void setReview(Review review) {
    this.review = review;
  }
}
