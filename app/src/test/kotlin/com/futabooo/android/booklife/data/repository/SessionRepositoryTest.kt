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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class SessionRepositoryTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var prefs: UserPreferences
    private lateinit var repository: SessionRepository
    private lateinit var scope: CoroutineScope

    private val loginForm = """
        <html><head><meta name="csrf-token" content="csrf123"/></head><body>
        <form action="/login" method="post">
          <input type="hidden" name="authenticity_token" value="tok456"/>
          <input type="text" name="session[email_address]"/>
          <input type="password" name="session[password]"/>
        </form></body></html>
    """.trimIndent()
    private val homeHtml = """
        <html><head><meta name="csrf-token" content="csrf789"/></head><body><div>home</div></body></html>
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("test_cookies", Context.MODE_PRIVATE).edit().clear().commit()
        cookieJar = PersistentCookieJar(context.getSharedPreferences("test_cookies", Context.MODE_PRIVATE))
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        prefs = UserPreferences(
            PreferenceDataStoreFactory.create(scope = scope) { File(tmp.root, "prefs.preferences_pb") },
        )
        val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
        val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = SessionRepository(retrofit.create(BookmeterApi::class.java), cookieJar, prefs, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
        scope.cancel()
    }

    private fun html(body: String) = MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setBody(body)

    @Test
    fun login_success() = runBlocking {
        prefs.setUserId(1)
        server.enqueue(html(loginForm))
        server.enqueue(html(homeHtml))

        val result = repository.login("a@example.com", "secret")

        assertEquals(LoginResult.Success, result)
        val get = server.takeRequest()
        assertEquals("GET", get.method)
        assertEquals("/login", get.path)
        val post = server.takeRequest()
        assertEquals("POST", post.method)
        val body = post.body.readUtf8()
        assertTrue(body, body.contains("authenticity_token=tok456"))
        assertTrue(body, body.contains("session%5Bemail_address%5D=a%40example.com"))
        assertTrue(body, body.contains("session%5Bpassword%5D=secret"))
        assertTrue(body, body.contains("session%5Bkeep%5D=1"))
        // A new login forgets the previous user id.
        assertNull(prefs.userId.first())
    }

    @Test
    fun login_invalidCredentials_whenAlertShown() = runBlocking {
        val withAlert = loginForm.replace(
            "<form",
            """<div class="container"><ul><li class="bm-flash-item--alert">NG</li></ul></div><form""",
        )
        server.enqueue(html(loginForm))
        server.enqueue(html(withAlert))

        assertEquals(LoginResult.InvalidCredentials, repository.login("a@example.com", "bad"))
    }

    @Test
    fun login_invalidCredentials_whenLoginPageReturned() = runBlocking {
        server.enqueue(html(loginForm))
        server.enqueue(html(loginForm))

        assertEquals(LoginResult.InvalidCredentials, repository.login("a@example.com", "bad"))
    }

    @Test
    fun login_networkFailure_isError() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))

        assertTrue(repository.login("a@example.com", "x") is LoginResult.Error)
    }

    @Test
    fun isLoggedIn_trueWhenHomeIsServed() = runBlocking {
        server.enqueue(html(homeHtml))
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun isLoggedIn_falseWhenRedirectedToLogin() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "/login"))
        server.enqueue(html(loginForm))
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun isLoggedIn_falseWhenBodyIsLoginForm() = runBlocking {
        server.enqueue(html(loginForm))
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun logout_clearsCookiesAndUserId() = runBlocking {
        prefs.setUserId(42)
        cookieJar.saveFromResponse(
            server.url("/"),
            listOf(okhttp3.Cookie.parse(server.url("/"), "s=1; Max-Age=3600")!!),
        )
        assertEquals(1, cookieJar.loadForRequest(server.url("/")).size)

        repository.logout()

        assertTrue(cookieJar.loadForRequest(server.url("/")).isEmpty())
        assertNull(prefs.userId.first())
    }
}
