package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class Review(var id: Int,
             var path: String?,
             var isDeletable: Boolean,
             @SerializedName("content_tag") var contentTag: String?,
             var content: String?,
             @SerializedName("created_at") var createdAt: String?,
             var isHighlight: Boolean,
             var isNewly: Boolean,
             var contents: Contents?,
             var owner: Owner?)
