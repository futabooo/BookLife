package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.SearchResultResource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class SearchPage(val csrfToken: String, val items: List<SearchResultResource>)

@Singleton
class SearchRepository @Inject constructor(
    private val api: BookmeterApi,
    private val json: Json,
) {
    /** Search by keyword/ISBN. [SearchPage.csrfToken] is reusable for registering books. */
    suspend fun search(keyword: String, offset: Int, limit: Int = 20): SearchPage =
        withContext(Dispatchers.IO) {
            val html = api.search(keyword).string()
            val csrf = requireCsrfToken(HtmlParsers.csrfToken(html))
            val items = api.searchJson(csrf, keyword, "recommended", "japanese", offset, limit)
                .decodeResources(json, SearchResultResource.serializer())
            SearchPage(csrf, items)
        }
}
