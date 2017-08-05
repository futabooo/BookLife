package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class HomeResource(var id: Int,
                   var path: String?,
                   @SerializedName("created_at") var createdAt: String?,
                   var isDeletable: Boolean,
                   @SerializedName("header_tag") var headerTag: String?,
                   var contents: Contents?,
                   var user: User)
