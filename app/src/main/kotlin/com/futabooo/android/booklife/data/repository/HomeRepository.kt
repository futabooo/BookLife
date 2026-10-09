package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.HomeResource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import com.futabooo.android.booklife.data.prefs.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** This month's reading stats, as displayed text. */
data class HomeStats(val pages: String, val volumes: String, val pagesPerDay: String)

@Singleton
class HomeRepository @Inject constructor(
    private val api: BookmeterApi,
    private val userPreferences: UserPreferences,
    private val json: Json,
) {
    /**
     * GET /home (HTML: stats + user id) then /home.json (csrf, offset 0, limit 10). The user id is
     * stored when absent. Throws on network/parse errors.
     */
    suspend fun fetchHomeStats(): HomeStats = withContext(Dispatchers.IO) {
        val html = api.home().string()
        val stats = HtmlParsers.homeStats(html)
            ?: throw IllegalStateException("home stats not found")
        if (userPreferences.userId.first() == null) {
            HtmlParsers.userId(html)?.let { userPreferences.setUserId(it) }
        }
        val csrf = requireCsrfToken(HtmlParsers.csrfToken(html))
        val resources = api.homeJson(csrf, 0, 10).decodeResources(json, HomeResource.serializer())
        if (userPreferences.userId.first() == null) {
            resources.firstOrNull()?.let { userPreferences.setUserId(it.user.id) }
        }
        HomeStats(stats.pages, stats.volumes, stats.pagesPerDay)
    }
}
