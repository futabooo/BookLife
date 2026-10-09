package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookDetailResource
import com.futabooo.android.booklife.data.model.BookRegistrationStatus
import com.futabooo.android.booklife.data.model.Review
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.network.SessionExpiredException
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

data class BookDetail(
    val title: String,
    val author: String,
    val thumbnail: String,
    val amazonUrl: String,
)

@Singleton
class BookDetailRepository @Inject constructor(
    private val api: BookmeterApi,
    private val session: SessionRepository,
    private val csrfTokenProvider: CsrfTokenProvider,
    private val json: Json,
) {
    /**
     * Scrapes /books/{id} for title / author / thumbnail / amazon link. The CSRF token is taken from
     * [CsrfTokenProvider]; the page's own token is only used to keep the provider up to date.
     */
    suspend fun fetchDetail(bookId: Int): BookDetail = withContext(Dispatchers.IO) {
        val html = api.bookDetail(bookId).string()
        if (HtmlParsers.isLoginPage(html)) throw SessionExpiredException()
        HtmlParsers.csrfToken(html)?.let(csrfTokenProvider::update)
        val parsed = HtmlParsers.bookDetail(html)
        BookDetail(
            title = parsed.title,
            author = parsed.author,
            thumbnail = parsed.thumbnail,
            amazonUrl = parsed.amazonUrl,
        )
    }

    /** Which lists the signed-in user has the book in (status.json). */
    suspend fun fetchRegistrationStatus(bookId: Int): BookRegistrationStatus =
        withContext(Dispatchers.IO) { registrationStatus(session.resolveUserId(), bookId) }

    private suspend fun registrationStatus(userId: Int, bookId: Int): BookRegistrationStatus =
        csrfTokenProvider.withToken { csrf -> api.bookRegistrationStatus(csrf, userId, bookId) }

    /**
     * The signed-in user's own review of the book, or null if they have none. The id of a review
     * equals the id of the read_book, so it is matched against `read_book_ids` (or the user id)
     * while paging through the public reviews.
     *
     * Limitation: bookmeter offers no endpoint for "my review of book X", so this scans the public
     * reviews (ordered by the server, not by us) up to [MAX_REVIEWS] entries; a review further down is
     * not found. `limit=100` is requested first and the page size actually served is taken from
     * `metadata.limit` (fallback [FALLBACK_PAGE_SIZE]). `reviews.json` items normally carry no
     * `read_at`, so [BookDetailResource.readAt] is only set if the server sent one.
     */
    suspend fun fetchMyReview(bookId: Int): BookDetailResource? =
        withContext(Dispatchers.IO) {
            val userId = session.resolveUserId()
            val readIds = registrationStatus(userId, bookId).readBookIds
            if (readIds.isEmpty()) return@withContext null
            var offset = 0
            var pageSize = REQUESTED_PAGE_SIZE
            while (offset < MAX_REVIEWS) {
                val page = reviewsPage(bookId, offset, pageSize)
                page.reviews.firstOrNull { it.id in readIds || it.user.id == userId }?.let {
                    return@withContext BookDetailResource(
                        id = it.id,
                        path = it.path,
                        createdAt = it.createdAt,
                        readAt = it.readAt,
                        contents = it.contents,
                        review = it,
                    )
                }
                // The server may serve fewer than requested per page; page by what it really serves.
                val served = page.servedLimit
                    ?: if (page.reviews.size == FALLBACK_PAGE_SIZE && page.reviews.size < pageSize) {
                        FALLBACK_PAGE_SIZE
                    } else {
                        pageSize
                    }
                if (page.reviews.size < served) break
                pageSize = served
                offset += page.reviews.size
            }
            null
        }

    /** Other users' reviews (`review_filter=none`). */
    suspend fun fetchReviews(bookId: Int, offset: Int = 0, limit: Int = 20): List<Review> =
        withContext(Dispatchers.IO) { reviewsPage(bookId, offset, limit).reviews }

    private class ReviewsPage(val reviews: List<Review>, val servedLimit: Int?)

    private suspend fun reviewsPage(bookId: Int, offset: Int, limit: Int): ReviewsPage {
        val body: JsonObject = csrfTokenProvider.withToken { csrf ->
            api.bookReviewsJson(csrf, bookId, "none", offset, limit)
        }
        val served = (body["metadata"] as? JsonObject)?.get("limit")
            ?.let { runCatching { it.jsonPrimitive.int }.getOrNull() }
            ?.takeIf { it > 0 }
        return ReviewsPage(body.decodeResources(json, Review.serializer()), served)
    }

    private companion object {
        const val REQUESTED_PAGE_SIZE = 100
        const val FALLBACK_PAGE_SIZE = 40
        const val MAX_REVIEWS = 400
    }
}
