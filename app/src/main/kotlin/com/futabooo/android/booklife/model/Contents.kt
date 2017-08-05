package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class Contents(var book: Book?, @SerializedName("image_url") var imageUrl: String?)
