package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookDetailResource
import com.futabooo.android.booklife.data.model.BookRegistrationStatus
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
    private val session: SessionRepository,
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

    /** Which lists the signed-in user has the book in (status.json). */
    suspend fun fetchRegistrationStatus(csrfToken: String, bookId: Int): BookRegistrationStatus =
        withContext(Dispatchers.IO) {
            api.bookRegistrationStatus(csrfToken, session.resolveUserId(), bookId)
        }

    /**
     * The signed-in user's own review of the book, or null if they have none. The id of a review
     * equals the id of the read_book, so it is matched against `read_book_ids` (or the user id)
     * while paging through the public reviews.
     */
    suspend fun fetchMyReview(csrfToken: String, bookId: Int): BookDetailResource? =
        withContext(Dispatchers.IO) {
            val readIds = fetchRegistrationStatus(csrfToken, bookId).readBookIds
            if (readIds.isEmpty()) return@withContext null
            val userId = session.resolveUserId()
            for (page in 0 until MAX_PAGES) {
                val reviews = fetchReviews(csrfToken, bookId, page * PAGE_SIZE, PAGE_SIZE)
                reviews.firstOrNull { it.id in readIds || it.user.id == userId }?.let {
                    return@withContext BookDetailResource(
                        id = it.id,
                        path = it.path,
                        createdAt = it.createdAt,
                        contents = it.contents,
                        review = it,
                    )
                }
                if (reviews.size < PAGE_SIZE) break
            }
            null
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

    private companion object {
        const val PAGE_SIZE = 40
        const val MAX_PAGES = 5
    }
}
