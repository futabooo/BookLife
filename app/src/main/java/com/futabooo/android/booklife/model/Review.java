package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class Review {
  private int id;
  private String path;
  private boolean deletable;
  @SerializedName("content_tag") private String contentTag;
  private String content;
  @SerializedName("created_at") private String createdAt;
  private boolean highlight;
  private boolean newly;
  private Contents contents;
  private Owner owner;

  public Review(int id, String path, boolean deletable, String contentTag, String content, String createdAt,
      boolean highlight, boolean newly, Contents contents, Owner owner) {
    this.id = id;
    this.path = path;
    this.deletable = deletable;
    this.contentTag = contentTag;
    this.content = content;
    this.createdAt = createdAt;
    this.highlight = highlight;
    this.newly = newly;
    this.contents = contents;
    this.owner = owner;
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

  public String getContentTag() {
    return contentTag;
  }

  public void setContentTag(String contentTag) {
    this.contentTag = contentTag;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isHighlight() {
    return highlight;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public boolean isNewly() {
    return newly;
  }

  public void setNewly(boolean newly) {
    this.newly = newly;
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
}
