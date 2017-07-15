package com.futabooo.android.booklife.model;

public class User {
  private int id;
  private String path;
  private String name;
  private String image;

  public User(int id, String path, String name, String image) {
    this.id = id;
    this.path = path;
    this.name = name;
    this.image = image;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }
}
