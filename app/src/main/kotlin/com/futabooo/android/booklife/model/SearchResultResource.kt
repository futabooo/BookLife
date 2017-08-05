package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class SearchResultResource(var contents: SearchResultContents, var status: String, @SerializedName("status_text")
var statusText: String)
