package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.network.SessionExpiredException
import com.futabooo.android.booklife.data.parser.HtmlParsers
import com.futabooo.android.booklife.data.prefs.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** This month's reading stats, as displayed text. */
data class HomeStats(val pages: String, val volumes: String, val pagesPerDay: String)

@Singleton
class HomeRepository @Inject constructor(
    private val api: BookmeterApi,
    private val userPreferences: UserPreferences,
    private val session: SessionRepository,
    private val csrfTokenProvider: CsrfTokenProvider,
) {
    /**
     * One GET /home (HTML: stats, user id, csrf token). The user id is resolved through
     * [SessionRepository] only when it is not stored yet (normally from this very page; `/home.json`
     * is only a last-resort fallback inside it). Throws on network/parse errors and
     * [SessionExpiredException] when /home is the login page.
     */
    suspend fun fetchHomeStats(): HomeStats = withContext(Dispatchers.IO) {
        val html = api.home().string()
        if (HtmlParsers.isLoginPage(html)) throw SessionExpiredException()
        val stats = HtmlParsers.homeStats(html)
            ?: throw IllegalStateException("home stats not found")
        HtmlParsers.csrfToken(html)?.let(csrfTokenProvider::update)
        if (userPreferences.userId.first() == null) session.resolveUserIdFromHome(html)
        HomeStats(stats.pages, stats.volumes, stats.pagesPerDay)
    }
}
