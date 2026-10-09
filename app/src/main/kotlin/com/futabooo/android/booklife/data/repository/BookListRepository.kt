package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookListMenu
import com.futabooo.android.booklife.data.model.Resource
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class BookListRepository @Inject constructor(
    private val api: BookmeterApi,
    private val session: SessionRepository,
    private val csrfTokenProvider: CsrfTokenProvider,
    private val json: Json,
) {
    /**
     * One page of [menu] as JSON with `attach_review=true` (the CSRF token comes from the shared
     * [CsrfTokenProvider]; the first call primes it). The page is empty when [offset] is past the end.
     */
    suspend fun fetch(menu: BookListMenu, offset: Int, limit: Int = 10): List<Resource> =
        withContext(Dispatchers.IO) {
            val userId = session.resolveUserId()
            csrfTokenProvider.withToken { csrf ->
                api.bookListJson(csrf, userId, menu.key, "true", offset, limit)
            }.decodeResources(json, Resource.serializer())
        }
}
