package com.futabooo.android.booklife.data.network

import com.futabooo.android.booklife.data.repository.csrfTokenOrThrow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

/**
 * The bookmeter CSRF token is per session (one value works for every JSON / write endpoint), so it
 * is scraped from `/home` once and cached. [invalidate] must be called when the session changes
 * (login / logout).
 */
@Singleton
class CsrfTokenProvider @Inject constructor(
    private val api: BookmeterApi,
) {
    private val mutex = Mutex()

    @Volatile private var cached: String? = null

    /** Incremented by [invalidate] so that a fetch started before it cannot store a stale token. */
    @Volatile private var generation = 0

    /** The cached token, scraping it first if needed. */
    suspend fun get(): String = cached ?: mutex.withLock { cached ?: fetch() }

    /** Scrapes a new token from `/home` (throws [SessionExpiredException] on the login page). */
    suspend fun refresh(): String = mutex.withLock { fetch() }

    fun invalidate() {
        generation++
        cached = null
    }

    /** Stores a token seen on a page that was fetched anyway. */
    fun update(token: String) {
        cached = token
    }

    /** Runs [block] with the token; on a 4xx answer the token is refreshed and [block] retried once. */
    suspend fun <T> withToken(block: suspend (token: String) -> T): T = try {
        block(get())
    } catch (e: HttpException) {
        if (e.code() !in 400..499) throw e
        block(refresh())
    }

    private suspend fun fetch(): String {
        val startedAt = generation
        val token = csrfTokenOrThrow(api.home().string())
        if (startedAt == generation) cached = token
        return token
    }
}
