package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BookActionRepository @Inject constructor(
    private val api: BookmeterApi,
) {
    /** A fresh CSRF token (from /home), for callers that did not get one from another page. */
    suspend fun fetchCsrfToken(): String = withContext(Dispatchers.IO) {
        requireCsrfToken(HtmlParsers.csrfToken(api.home().string()))
    }

    /** Registers a book in READING / TO_READ / QUITTED (use [addReadBook] for READ). */
    suspend fun addBook(csrfToken: String, userId: Int, menu: BookListMenu, bookId: Int) {
        withContext(Dispatchers.IO) { api.addBook(csrfToken, userId, menu.key, bookId) }
    }

    /** Registers a finished book. [readAt] is formatted `yyyy/M/d`. */
    suspend fun addReadBook(
        csrfToken: String,
        userId: Int,
        bookId: Int,
        readAt: String,
        review: String,
        netabare: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            api.addReadBook(csrfToken, userId, bookId, readAt, review, if (netabare) 1 else 0)
        }
    }

    /** Updates an existing review ([reviewId] = `Review.id`). */
    suspend fun updateReadBook(
        csrfToken: String,
        reviewId: Int,
        bookId: Int,
        readAt: String,
        review: String,
        netabare: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            api.updateReadBook(csrfToken, reviewId, bookId, readAt, review, if (netabare) 1 else 0)
        }
    }
}
