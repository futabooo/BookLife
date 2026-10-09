package com.futabooo.android.booklife.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.futabooo.android.booklife.data.network.BookmeterApi
import com.futabooo.android.booklife.data.network.CsrfTokenProvider
import com.futabooo.android.booklife.data.network.PersistentCookieJar
import com.futabooo.android.booklife.data.network.SessionExpiredException
import com.futabooo.android.booklife.data.prefs.UserPreferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@RunWith(RobolectricTestRunner::class)
class HomeRepositoryTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var scope: CoroutineScope
    private lateinit var prefs: UserPreferences
    private lateinit var csrf: CsrfTokenProvider
    private lateinit var repository: HomeRepository

    private val homeHtml = """
        <html><head><meta name="csrf-token" content="csrfH"/></head><body>
        <div class="bm-block-side__content"><dl class="user-profiles"><dt class="user-profiles__avatar"><a href="/users/321"></a></dt></dl></div>
        <div class="stats__thismonth"><ul class="thismonth__list">
          <li class="list__item"><span class="item__number">1000</span></li>
          <li class="list__item"><span class="item__number">20</span></li>
          <li class="list__item"><span class="item__number">33</span></li>
        </ul></div></body></html>
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val jar = PersistentCookieJar(context.getSharedPreferences("test_cookies_home", Context.MODE_PRIVATE))
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        prefs = UserPreferences(
            PreferenceDataStoreFactory.create(scope = scope) { File(tmp.root, "prefs.preferences_pb") },
        )
        val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(BookmeterApi::class.java)
        csrf = CsrfTokenProvider(api)
        repository = HomeRepository(api, prefs, SessionRepository(api, jar, prefs, json, csrf), csrf)
    }

    @After
    fun tearDown() {
        server.shutdown()
        scope.cancel()
    }

    private fun html(body: String) = MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setBody(body)

    @Test
    fun fetchHomeStats_singleRequest_storesUserId_noHomeJson() = runBlocking {
        server.enqueue(html(homeHtml))

        val stats = repository.fetchHomeStats()

        assertEquals(HomeStats("1000", "20", "33"), stats)
        assertEquals(1, server.requestCount)
        assertEquals("/home", server.takeRequest().path)
        assertEquals(321, prefs.userId.first())
        // The page's token primes the shared provider: no further request.
        assertEquals("csrfH", csrf.get())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun fetchHomeStats_knownUserId_neverResolvesAgain() = runBlocking {
        prefs.setUserId(5)
        server.enqueue(html(homeHtml))
        repository.fetchHomeStats()
        assertEquals(1, server.requestCount)
        assertEquals(5, prefs.userId.first())
    }

    @Test
    fun fetchHomeStats_loginPage_throwsSessionExpired() {
        server.enqueue(html("""<html><form><input name="session[email_address]"/></form></html>"""))
        try {
            runBlocking { repository.fetchHomeStats() }
            fail()
        } catch (e: SessionExpiredException) {
            // expected
        }
    }
}
