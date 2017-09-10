package com.futabooo.android.booklife.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Netabare(@SerializedName("display_comment") var displayComment: Boolean,
                    @SerializedName("display_content") var displayContent: Boolean,
                    @SerializedName("is_clicked") var isClicked: Boolean,
                    var netabare: Boolean) : Serializable