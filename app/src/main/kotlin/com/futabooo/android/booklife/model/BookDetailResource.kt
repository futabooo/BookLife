package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class BookDetailResource(var id: Int,
                         var path: String?,
                         var isDeletable: Boolean,
                         @SerializedName("created_at") var createdAt: String?,
                         @SerializedName("read_at") var readAt: String?,
                         var contents: Contents?,
                         var owner: Owner?,
                         var review: Review?)
