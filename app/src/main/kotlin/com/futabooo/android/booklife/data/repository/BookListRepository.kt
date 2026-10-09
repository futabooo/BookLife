package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.parser.HtmlParsers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class BookListRepository @Inject constructor(
    private val api: BookmeterApi,
    private val session: SessionRepository,
    private val json: Json,
) {
    /**
     * One page of [menu]. HTML first (csrf token), then JSON with `attach_review=true`.
     * The page is empty when [offset] is past the end.
     */
    suspend fun fetch(menu: BookListMenu, offset: Int, limit: Int = 10): List<Resource> =
        withContext(Dispatchers.IO) {
            val userId = session.resolveUserId()
            val html = api.bookList(userId, menu.key).string()
            val csrf = requireCsrfToken(HtmlParsers.csrfToken(html))
            api.bookListJson(csrf, userId, menu.key, "true", offset, limit)
                .decodeResources(json, Resource.serializer())
        }
}
