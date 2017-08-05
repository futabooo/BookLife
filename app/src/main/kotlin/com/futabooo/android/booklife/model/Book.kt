package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName

class Book(var id: Int,
           var title: String,
           @SerializedName("image_url") var imageUrl: String,
           var path: String,
           var page: Int,
           var isOriginal: Boolean,
           @SerializedName("registration_count") var registrationCount: Int,
           var author: Author,
           @SerializedName("amazon_urls") var amazonUrl: AmazonUrl)
