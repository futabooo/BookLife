package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class Resource(var path: String?,
               var id: Int,
               var page: Int,
               var author: String?,
               var book: Book?,
               var owner: Owner?,
               var isDeletable: Boolean,
               @SerializedName("content_tag") var contentTag: String?,
               var content: String?,
               var isHighlight: Boolean,
               var isNewly: Boolean)
