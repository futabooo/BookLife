package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

/** Write endpoints. The CSRF token comes from [CsrfTokenProvider] (refreshed once on a 4xx). */
@Singleton
class BookActionRepository @Inject constructor(
    private val api: BookmeterApi,
    private val csrfTokenProvider: CsrfTokenProvider,
) {
    /** Registers a book in READING / TO_READ / QUITTED (use [addReadBook] for READ). */
    suspend fun addBook(userId: Int, menu: BookListMenu, bookId: Int) {
        withContext(Dispatchers.IO) {
            csrfTokenProvider.withToken { csrf ->
                api.addBook(csrf, userId, menu.key, bookId).requireSuccess()
            }
        }
    }

    /** Registers a finished book. [readAt] is formatted `yyyy/M/d`. */
    suspend fun addReadBook(
        userId: Int,
        bookId: Int,
        readAt: String,
        review: String,
        netabare: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            csrfTokenProvider.withToken { csrf ->
                api.addReadBook(csrf, userId, bookId, readAt, review, if (netabare) 1 else 0)
                    .requireSuccess()
            }
        }
    }

    /**
     * Updates an existing review ([reviewId] = `Review.id`). A null [readAt] leaves the stored read
     * date untouched (the field is not sent).
     */
    suspend fun updateReadBook(
        reviewId: Int,
        bookId: Int,
        readAt: String?,
        review: String,
        netabare: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            csrfTokenProvider.withToken { csrf ->
                api.updateReadBook(csrf, reviewId, bookId, readAt, review, if (netabare) 1 else 0)
            }
        }
    }

    /**
     * The body is deliberately not parsed: a redirect may be followed into an HTML page although the
     * registration was recorded, so any 2xx final response counts as success.
     */
    private fun Response<ResponseBody>.requireSuccess() {
        try {
            if (!isSuccessful) throw HttpException(this)
        } finally {
            body()?.close()
            errorBody()?.close()
        }
    }
}
