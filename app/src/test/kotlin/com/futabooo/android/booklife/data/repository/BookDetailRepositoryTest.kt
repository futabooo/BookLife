package com.futabooo.android.booklife.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
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
        val csrf = CsrfTokenProvider(api)
        csrf.update("tok")
        repository = BookDetailRepository(api, SessionRepository(api, cookieJar, prefs, json, csrf), csrf, json)
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

    private fun reviews(ids: List<Int>, limit: Int? = null) = jsonResponse(
        """{"metadata":{"count":${ids.size}${limit?.let { ""","limit":$it""" } ?: ""}},"resources":[${
            ids.joinToString(",") { """{"id":$it,"content":"c$it","user":{"id":${1000 + it}}}""" }
        }]}""",
    )

    @Test
    fun fetchRegistrationStatus_parses() = runBlocking {
        server.enqueue(jsonResponse("""{"read_book_ids":[1,2],"reading_book_id":3,"stacked_book_id":null,"wish_book_id":4}"""))
        val s = repository.fetchRegistrationStatus(55)
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
        assertNull(repository.fetchMyReview(55))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun fetchMyReview_foundOnFirstPage() = runBlocking {
        server.enqueue(status("[5]"))
        server.enqueue(reviews(listOf(1, 5, 9)))
        val mine = repository.fetchMyReview(55)
        assertEquals(5, mine?.review?.id)
        assertEquals("c5", mine?.review?.content)
        server.takeRequest()
        val p = server.takeRequest().path!!
        assertTrue(p, p.contains("review_filter=none"))
        assertTrue(p, p.contains("limit=100"))
        assertEquals(2, server.requestCount)
    }

    @Test
    fun fetchMyReview_foundAfterPaging() = runBlocking {
        server.enqueue(status("[500]"))
        server.enqueue(reviews((1..40).toList(), limit = 40))
        server.enqueue(reviews(listOf(500, 501), limit = 40))
        val mine = repository.fetchMyReview(55)
        assertNotNull(mine)
        assertEquals(500, mine?.review?.id)
        server.takeRequest()
        server.takeRequest()
        val p = server.takeRequest().path!!
        assertTrue(p, p.contains("offset=40"))
        assertTrue(p, p.contains("limit=40"))
    }

    @Test
    fun fetchMyReview_pagesBy100WhenServerAllowsIt() = runBlocking {
        server.enqueue(status("[150]"))
        server.enqueue(reviews((1..100).toList(), limit = 100))
        server.enqueue(reviews(listOf(150), limit = 100))
        assertEquals(150, repository.fetchMyReview(55)?.review?.id)
        server.takeRequest()
        assertTrue(server.takeRequest().path!!.contains("offset=0&limit=100"))
        assertTrue(server.takeRequest().path!!.contains("offset=100&limit=100"))
    }

    @Test
    fun fetchMyReview_readAtOnlyWhenServerSendsIt() = runBlocking {
        server.enqueue(status("[5]"))
        server.enqueue(jsonResponse("""{"resources":[{"id":5,"read_at":"2020/1/2","user":{"id":1}}]}"""))
        assertEquals("2020/1/2", repository.fetchMyReview(55)?.readAt)
        server.enqueue(status("[5]"))
        server.enqueue(reviews(listOf(5)))
        assertNull(repository.fetchMyReview(55)?.readAt)
    }

    @Test
    fun fetchMyReview_stopsAfter400Reviews() = runBlocking {
        server.enqueue(status("[999999]"))
        repeat(4) { page ->
            server.enqueue(reviews(((page * 100 + 1)..(page * 100 + 100)).toList(), limit = 100))
        }
        assertNull(repository.fetchMyReview(55))
        assertEquals(5, server.requestCount) // status + 4 pages
    }

    @Test
    fun fetchDetail_loginPage_throwsSessionExpired() {
        server.enqueue(MockResponse().setHeader("Content-Type", "text/html").setBody(
            """<html><form><input name="session[email_address]"/></form></html>""",
        ))
        try {
            runBlocking { repository.fetchDetail(55) }
            org.junit.Assert.fail("expected SessionExpiredException")
        } catch (e: com.futabooo.android.booklife.data.network.SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun fetchDetail_doesNotRequireTokenOnPage_andReusesProvider() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "text/html").setBody(
            """<html><head><meta name="csrf-token" content="pageTok"/></head><body>
               <div class="header__inner"><h1 class="inner__title">T</h1></div></body></html>""",
        ))
        assertEquals("T", repository.fetchDetail(55).title)
        server.enqueue(status("[]"))
        repository.fetchRegistrationStatus(55)
        server.takeRequest()
        assertEquals("pageTok", server.takeRequest().getHeader("X-CSRF-Token"))
    }

    @Test
    fun fetchMyReview_matchesByUserId_andStopsOnShortPage() = runBlocking {
        server.enqueue(status("[999999]"))
        server.enqueue(jsonResponse("""{"resources":[{"id":1,"user":{"id":3}}]}"""))
        assertNull(repository.fetchMyReview(55))
        assertEquals(2, server.requestCount)
    }
}
