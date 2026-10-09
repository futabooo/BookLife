package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.HomeResource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.network.NetworkModule
import com.futabooo.android.booklife.data.network.PersistentCookieJar
import com.futabooo.android.booklife.data.parser.HtmlParsers
import com.futabooo.android.booklife.data.prefs.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import timber.log.Timber

sealed interface LoginResult {
    data object Success : LoginResult
    data object InvalidCredentials : LoginResult
    data class Error(val throwable: Throwable) : LoginResult
}

/** Outcome of [SessionRepository.checkSession]. */
sealed interface SessionState {
    data object LoggedIn : SessionState

    /** The server said so: redirected to / served the login page, or answered 401 / 403. */
    data object LoggedOut : SessionState

    /** Could not be determined (offline, timeout, 5xx, ...). */
    data class Unknown(val cause: Throwable? = null) : SessionState
}

/** Login state is held only as session cookies; the password is never stored. */
@Singleton
class SessionRepository @Inject constructor(
    private val api: BookmeterApi,
    private val cookieJar: PersistentCookieJar,
    private val userPreferences: UserPreferences,
    private val json: Json,
    private val csrfTokenProvider: CsrfTokenProvider,
) {
    /**
     * GET /login -> authenticity_token -> POST /login (with `session[keep]=1` so the server issues
     * a persistent cookie). Success iff the response has no alert and is not the login page.
     */
    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            csrfTokenProvider.invalidate()
            val loginHtml = api.loginPage().string()
            val token = HtmlParsers.authenticityToken(loginHtml)
            val resultHtml = api.login(email, password, token).string()
            if (HtmlParsers.hasLoginAlert(resultHtml) || HtmlParsers.isLoginPage(resultHtml)) {
                LoginResult.InvalidCredentials
            } else {
                // A different account may have been used before: forget the old user id (and the
                // CSRF token of the old session) before anything resolves them again.
                userPreferences.clear()
                csrfTokenProvider.invalidate()
                LoginResult.Success
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResult.Error(e)
        }
    }

    /**
     * Asks /home whether the stored cookies still work. Only an explicit answer of the server
     * (login page / 401 / 403) is [SessionState.LoggedOut]; network errors and other failures are
     * [SessionState.Unknown] so that being offline is not mistaken for being signed out.
     */
    suspend fun checkSession(): SessionState = withContext(Dispatchers.IO) {
        try {
            val response = api.homeResponse()
            try {
                val code = response.code()
                when {
                    response.raw().request.url.encodedPath == "/login" -> SessionState.LoggedOut
                    code == 401 || code == 403 -> SessionState.LoggedOut
                    !response.isSuccessful -> SessionState.Unknown(null)
                    else -> {
                        val body = response.body()?.string()
                        when {
                            body == null -> SessionState.Unknown(null)
                            HtmlParsers.isLoginPage(body) -> SessionState.LoggedOut
                            else -> SessionState.LoggedIn
                        }
                    }
                }
            } finally {
                // Never read the error body into memory; just release the connection.
                response.errorBody()?.close()
                response.body()?.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SessionState.Unknown(e)
        }
    }

    /** True when the cookie jar holds cookies for bookmeter (an offline user keeps their session). */
    fun hasSessionCookie(): Boolean =
        cookieJar.hasCookiesFor(NetworkModule.BASE_URL.toHttpUrl().host)

    /**
     * Clears the cookie jar and the stored preferences (user id). Both are cleared independently and
     * best-effort: a failure of one never skips the other, and none is propagated.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            cookieJar.clear()
        } catch (e: Exception) {
            Timber.w(e, "failed to clear cookies")
        }
        try {
            userPreferences.clear()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "failed to clear preferences")
        }
        csrfTokenProvider.invalidate()
    }

    /**
     * The signed-in user's id: the stored one, otherwise scraped from /home (and /home.json as a
     * fallback) and then stored. Throws if it cannot be determined.
     */
    suspend fun resolveUserId(): Int = withContext(Dispatchers.IO) {
        userPreferences.userId.first()?.let { return@withContext it }
        resolveUserIdFromHome(api.home().string())
    }

    /**
     * Like [resolveUserId] for a caller that already fetched the /home page [homeHtml]; resolves
     * (and stores) the id from it without another request unless it is not on the page.
     */
    suspend fun resolveUserIdFromHome(homeHtml: String): Int = withContext(Dispatchers.IO) {
        HtmlParsers.userId(homeHtml)?.let {
            userPreferences.setUserId(it)
            return@withContext it
        }
        val csrf = csrfTokenOrThrow(homeHtml)
        val id = api.homeJson(csrf, 0, 10)
            .decodeResources(json, HomeResource.serializer())
            .firstOrNull()?.user?.id
            ?: throw IllegalStateException("user id not found")
        userPreferences.setUserId(id)
        id
    }
}
