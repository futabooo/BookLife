package com.futabooo.android.booklife.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.PersistentCookieJar
import com.futabooo.android.booklife.data.prefs.UserPreferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@RunWith(RobolectricTestRunner::class)
class BookDetailRepositoryTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var scope: CoroutineScope
    private lateinit var repository: BookDetailRepository

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cookieJar = PersistentCookieJar(context.getSharedPreferences("test_cookies_bd", Context.MODE_PRIVATE))
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val prefs = UserPreferences(
            PreferenceDataStoreFactory.create(scope = scope) { File(tmp.root, "prefs.preferences_pb") },
        )
        runBlocking { prefs.setUserId(77) }
        val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().cookieJar(cookieJar).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(BookmeterApi::class.java)
        repository = BookDetailRepository(api, SessionRepository(api, cookieJar, prefs, json), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        scope.cancel()
    }

    private fun jsonResponse(body: String) =
        MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun status(readIds: String) =
        jsonResponse("""{"read_book_ids":$readIds,"reading_book_id":null,"stacked_book_id":null,"wish_book_id":null}""")

    private fun reviews(ids: List<Int>) = jsonResponse(
        """{"metadata":{"count":${ids.size}},"resources":[${
            ids.joinToString(",") { """{"id":$it,"content":"c$it","user":{"id":${1000 + it}}}""" }
        }]}""",
    )

    @Test
    fun fetchRegistrationStatus_parses() = runBlocking {
        server.enqueue(jsonResponse("""{"read_book_ids":[1,2],"reading_book_id":3,"stacked_book_id":null,"wish_book_id":4}"""))
        val s = repository.fetchRegistrationStatus("tok", 55)
        assertEquals(listOf(1, 2), s.readBookIds)
        assertEquals(3, s.readingBookId)
        assertNull(s.stackedBookId)
        assertEquals(4, s.wishBookId)
        val r = server.takeRequest()
        assertEquals("/users/77/books/55/status.json", r.path)
        assertEquals("tok", r.getHeader("X-CSRF-Token"))
        assertEquals("XMLHttpRequest", r.getHeader("X-Requested-With"))
    }

    @Test
    fun fetchMyReview_noReadBookIds_returnsNullWithoutFetchingReviews() = runBlocking {
        server.enqueue(status("[]"))
        assertNull(repository.fetchMyReview("tok", 55))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun fetchMyReview_foundOnFirstPage() = runBlocking {
        server.enqueue(status("[5]"))
        server.enqueue(reviews(listOf(1, 5, 9)))
        val mine = repository.fetchMyReview("tok", 55)
        assertEquals(5, mine?.review?.id)
        assertEquals("c5", mine?.review?.content)
        server.takeRequest()
        val p = server.takeRequest().path!!
        assertTrue(p, p.contains("review_filter=none"))
        assertTrue(p, p.contains("limit=40"))
        assertEquals(2, server.requestCount)
    }

    @Test
    fun fetchMyReview_foundAfterPaging() = runBlocking {
        server.enqueue(status("[500]"))
        server.enqueue(reviews((1..40).toList()))
        server.enqueue(reviews(listOf(500, 501)))
        val mine = repository.fetchMyReview("tok", 55)
        assertNotNull(mine)
        assertEquals(500, mine?.review?.id)
        server.takeRequest()
        server.takeRequest()
        val p = server.takeRequest().path!!
        assertTrue(p, p.contains("offset=40"))
    }

    @Test
    fun fetchMyReview_matchesByUserId_andStopsOnShortPage() = runBlocking {
        server.enqueue(status("[999999]"))
        server.enqueue(jsonResponse("""{"resources":[{"id":1,"user":{"id":3}}]}"""))
        assertNull(repository.fetchMyReview("tok", 55))
        assertEquals(2, server.requestCount)
    }
}
