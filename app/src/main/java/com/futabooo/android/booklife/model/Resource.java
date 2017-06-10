package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class Resource {
  private String path;
  private int id;
  private int page;
  private String author;
  private Book book;
  private Owner owner;
  private boolean deletable;
  @SerializedName("content_tag")
  private String contentTag;
  private String content;
  private boolean highlight;
  private boolean newly;

  public Resource(String path, int id, int page, String author, Book book, Owner owner, boolean deletable,
      String contentTag, String content, boolean highlight, boolean newly) {
    this.path = path;
    this.id = id;
    this.page = page;
    this.author = author;
    this.book = book;
    this.owner = owner;
    this.deletable = deletable;
    this.contentTag = contentTag;
    this.content = content;
    this.highlight = highlight;
    this.newly = newly;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public Owner getOwner() {
    return owner;
  }

  public void setOwner(Owner owner) {
    this.owner = owner;
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
}
