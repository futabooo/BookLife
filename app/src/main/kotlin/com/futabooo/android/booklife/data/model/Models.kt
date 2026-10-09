package com.futabooo.android.booklife.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// JSON models for bookmeter.com. They mirror the old Gson models; every field is optional /
// default-valued so that a missing key never fails decoding (Json { ignoreUnknownKeys = true }).

@Serializable
data class Author(
    val name: String? = null,
    val path: String? = null,
)

@Serializable
data class AmazonUrl(
    val outline: String? = null,
    val registration: String? = null,
    @SerialName("wish_book") val wishBook: String? = null,
)

@Serializable
data class Book(
    val id: Int = 0,
    val title: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    val path: String = "",
    val page: Int = 0,
    val isOriginal: Boolean = false,
    @SerialName("registration_count") val registrationCount: Int = 0,
    val author: Author = Author(),
    @SerialName("amazon_urls") val amazonUrl: AmazonUrl = AmazonUrl(),
)

@Serializable
data class Contents(
    val book: Book? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class Owner(
    val id: Int = 0,
    val path: String? = null,
    val name: String? = null,
    val image: String? = null,
)

@Serializable
data class User(
    val id: Int = 0,
    val path: String? = null,
    val name: String? = null,
    val image: String? = null,
)

@Serializable
data class Netabare(
    @SerialName("display_comment") val displayComment: Boolean = false,
    @SerialName("display_content") val displayContent: Boolean = false,
    @SerialName("is_clicked") val isClicked: Boolean = false,
    val netabare: Boolean = false,
)

@Serializable
data class Review(
    val id: Int = 0,
    val path: String? = null,
    val isDeletable: Boolean = false,
    @SerialName("content_tag") val contentTag: String? = null,
    val content: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val isHighlight: Boolean = false,
    val netabare: Netabare = Netabare(),
    val isNewly: Boolean = false,
    val contents: Contents? = null,
    val user: User = User(),
)

/** An element of `resources` in `/users/{id}/books/{menu}.json`. */
@Serializable
data class Resource(
    val path: String? = null,
    val id: Int = 0,
    val page: Int = 0,
    val author: String? = null,
    val book: Book? = null,
    val owner: Owner? = null,
    val isDeletable: Boolean = false,
    @SerialName("content_tag") val contentTag: String? = null,
    val content: String? = null,
    val isHighlight: Boolean = false,
    val isNewly: Boolean = false,
)

/** An element of `resources` in `/home.json`. */
@Serializable
data class HomeResource(
    val id: Int = 0,
    val path: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val isDeletable: Boolean = false,
    @SerialName("header_tag") val headerTag: String? = null,
    val contents: Contents? = null,
    val user: User = User(),
)

/** An element of `resources` in `/books/{id}.json` (the signed-in user's own record of the book). */
@Serializable
data class BookDetailResource(
    val id: Int = 0,
    val path: String? = null,
    val isDeletable: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    val contents: Contents? = null,
    val owner: Owner? = null,
    val review: Review? = null,
)

@Serializable
data class SearchResultContents(
    val book: Book = Book(),
)

/** An element of `resources` in `/search.json`. */
@Serializable
data class SearchResultResource(
    val contents: SearchResultContents = SearchResultContents(),
    val status: String = "",
    @SerialName("status_text") val statusText: String = "",
)
