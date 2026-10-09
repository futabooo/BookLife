package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class SearchPage(val csrfToken: String, val items: List<SearchResultResource>)

@Singleton
class SearchRepository @Inject constructor(
    private val api: BookmeterApi,
) {
    private val mutex = Mutex()
    private var cachedCsrf: String? = null

    private suspend fun csrf(keyword: String, refresh: Boolean): String = mutex.withLock {
        cachedCsrf.takeUnless { refresh } ?: requireCsrfToken(HtmlParsers.csrfToken(api.search(keyword).string()))
            .also { cachedCsrf = it }
    }

    /**
     * Search by keyword/ISBN. bookmeter serves fixed pages of 20, so `page = offset / 20 + 1` and
     * [limit] is ignored by the server. [SearchPage.csrfToken] is reusable for registering books.
     */
    suspend fun search(keyword: String, offset: Int, limit: Int = PAGE_SIZE): SearchPage =
        withContext(Dispatchers.IO) {
            val page = offset / PAGE_SIZE + 1
            suspend fun fetch(token: String) =
                api.searchPartial(
                    csrfToken = token,
                    keyword = keyword,
                    sort = "recommended",
                    type = "japanese_v2",
                    page = page,
                ).string()

            var token = csrf(keyword, refresh = false)
            val html = try {
                fetch(token)
            } catch (e: HttpException) {
                if (e.code() !in 400..499) throw e
                token = csrf(keyword, refresh = true)
                fetch(token)
            }
            SearchPage(token, HtmlParsers.searchResults(html))
        }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
