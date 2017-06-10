package com.futabooo.android.booklife.model;

import com.google.gson.annotations.SerializedName;

public class SearchResultResource {
  private SearchResultContents contents;
  private String status;
  @SerializedName("status_text")
  private String statusText;

  public SearchResultResource(SearchResultContents contents, String status, String statusText) {
    this.contents = contents;
    this.status = status;
    this.statusText = statusText;
  }

  public SearchResultContents getContents() {
    return contents;
  }

  public void setContents(SearchResultContents contents) {
    this.contents = contents;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusText() {
    return statusText;
  }

  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }
}
