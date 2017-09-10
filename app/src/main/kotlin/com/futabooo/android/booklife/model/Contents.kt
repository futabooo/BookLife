package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Contents(var book: Book?, @SerializedName("image_url") var imageUrl: String?) : Serializable
