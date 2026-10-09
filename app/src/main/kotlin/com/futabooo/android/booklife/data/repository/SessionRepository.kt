package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.HomeResource
import com.futabooo.android.booklife.data.network.BookmeterApi
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

sealed interface LoginResult {
    data object Success : LoginResult
    data object InvalidCredentials : LoginResult
    data class Error(val throwable: Throwable) : LoginResult
}

/** Login state is held only as session cookies; the password is never stored. */
@Singleton
class SessionRepository @Inject constructor(
    private val api: BookmeterApi,
    private val cookieJar: PersistentCookieJar,
    private val userPreferences: UserPreferences,
    private val json: Json,
) {
    /**
     * GET /login -> authenticity_token -> POST /login (with `session[keep]=1` so the server issues
     * a persistent cookie). Success iff the response has no alert and is not the login page.
     */
    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val loginHtml = api.loginPage().string()
            val token = HtmlParsers.authenticityToken(loginHtml)
            val resultHtml = api.login(email, password, token).string()
            if (HtmlParsers.hasLoginAlert(resultHtml) || HtmlParsers.isLoginPage(resultHtml)) {
                LoginResult.InvalidCredentials
            } else {
                // A different account may have been used before: force the user id to be resolved again.
                userPreferences.clear()
                LoginResult.Success
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResult.Error(e)
        }
    }

    /**
     * True when the stored cookies still open /home. Network errors are thrown (so callers can tell
     * "offline" from "logged out").
     */
    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val response = api.homeResponse()
        val finalPath = response.raw().request.url.encodedPath
        if (finalPath == "/login") return@withContext false
        val body = response.body()?.string() ?: return@withContext false
        !HtmlParsers.isLoginPage(body)
    }

    /** Clears the cookie jar and the stored preferences (user id). */
    suspend fun logout() = withContext(Dispatchers.IO) {
        cookieJar.clear()
        userPreferences.clear()
    }

    /**
     * The signed-in user's id: the stored one, otherwise scraped from /home (and /home.json as a
     * fallback) and then stored. Throws if it cannot be determined.
     */
    suspend fun resolveUserId(): Int = withContext(Dispatchers.IO) {
        userPreferences.userId.first()?.let { return@withContext it }
        val html = api.home().string()
        HtmlParsers.userId(html)?.let {
            userPreferences.setUserId(it)
            return@withContext it
        }
        val csrf = requireCsrfToken(HtmlParsers.csrfToken(html))
        val id = api.homeJson(csrf, 0, 10)
            .decodeResources(json, HomeResource.serializer())
            .firstOrNull()?.user?.id
            ?: throw IllegalStateException("user id not found")
        userPreferences.setUserId(id)
        id
    }
}
