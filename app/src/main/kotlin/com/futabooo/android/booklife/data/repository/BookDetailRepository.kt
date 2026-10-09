package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookDetailResource
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class BookDetail(
    val title: String,
    val author: String,
    val thumbnail: String,
    val amazonUrl: String,
    val csrfToken: String,
)

@Singleton
class BookDetailRepository @Inject constructor(
    private val api: BookmeterApi,
    private val json: Json,
) {
    /** Scrapes /books/{id}. The returned [BookDetail.csrfToken] is needed by the other calls. */
    suspend fun fetchDetail(bookId: Int): BookDetail = withContext(Dispatchers.IO) {
        val html = api.bookDetail(bookId).string()
        val parsed = HtmlParsers.bookDetail(html)
        BookDetail(
            title = parsed.title,
            author = parsed.author,
            thumbnail = parsed.thumbnail,
            amazonUrl = parsed.amazonUrl,
            csrfToken = requireCsrfToken(HtmlParsers.csrfToken(html)),
        )
    }

    /** The signed-in user's own record/review of the book, or null if they have none. */
    suspend fun fetchMyReview(csrfToken: String, bookId: Int): BookDetailResource? =
        withContext(Dispatchers.IO) {
            api.bookDetailJson(csrfToken, bookId, 0, 1000)
                .decodeResources(json, BookDetailResource.serializer())
                .firstOrNull()
        }

    /** Other users' reviews (`review_filter=none`). */
    suspend fun fetchReviews(
        csrfToken: String,
        bookId: Int,
        offset: Int = 0,
        limit: Int = 20,
    ): List<Review> = withContext(Dispatchers.IO) {
        api.bookReviewsJson(csrfToken, bookId, "none", offset, limit)
            .decodeResources(json, Review.serializer())
    }
}
