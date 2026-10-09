package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SearchPage(val csrfToken: String, val items: List<SearchResultResource>)

@Singleton
class SearchRepository @Inject constructor(
    private val api: BookmeterApi,
    private val csrfTokenProvider: CsrfTokenProvider,
) {
    /**
     * Search by keyword/ISBN. bookmeter serves fixed pages of 20, so `page = offset / 20 + 1` and
     * [limit] is ignored by the server. [SearchPage.csrfToken] is the token that was used. On a 4xx
     * answer the token is refreshed and the request retried once.
     */
    suspend fun search(keyword: String, offset: Int, limit: Int = PAGE_SIZE): SearchPage =
        withContext(Dispatchers.IO) {
            val page = offset / PAGE_SIZE + 1
            var used = ""
            val html = csrfTokenProvider.withToken { token ->
                used = token
                api.searchPartial(
                    csrfToken = token,
                    keyword = keyword,
                    sort = "recommended",
                    type = "japanese_v2",
                    page = page,
                ).string()
            }
            SearchPage(used, HtmlParsers.searchResults(html))
        }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
