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
    private lateinit var csrf: CsrfTokenProvider

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
        val api = retrofit.create(BookmeterApi::class.java)
        csrf = CsrfTokenProvider(api)
        repository = SessionRepository(api, cookieJar, prefs, json, csrf)
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
    fun checkSession_loggedInWhenHomeIsServed() = runBlocking {
        server.enqueue(html(homeHtml))
        assertEquals(SessionState.LoggedIn, repository.checkSession())
    }

    @Test
    fun checkSession_loggedOutWhenRedirectedToLogin() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "/login"))
        server.enqueue(html(loginForm))
        assertEquals(SessionState.LoggedOut, repository.checkSession())
    }

    @Test
    fun checkSession_loggedOutWhenBodyIsLoginForm() = runBlocking {
        server.enqueue(html(loginForm))
        assertEquals(SessionState.LoggedOut, repository.checkSession())
    }

    @Test
    fun checkSession_loggedOutOn401And403() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("nope"))
        assertEquals(SessionState.LoggedOut, repository.checkSession())
        server.enqueue(MockResponse().setResponseCode(403))
        assertEquals(SessionState.LoggedOut, repository.checkSession())
    }

    @Test
    fun checkSession_unknownOn5xx() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository.checkSession() is SessionState.Unknown)
    }

    @Test
    fun checkSession_unknownWhenOffline() = runBlocking {
        server.shutdown()
        val state = repository.checkSession()
        assertTrue(state.toString(), state is SessionState.Unknown)
        assertTrue((state as SessionState.Unknown).cause is java.io.IOException)
    }

    @Test
    fun checkSession_unknownOnTimeout() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE))
        val client = OkHttpClient.Builder().cookieJar(cookieJar).readTimeout(200, java.util.concurrent.TimeUnit.MILLISECONDS).build()
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(BookmeterApi::class.java)
        val repo = SessionRepository(api, cookieJar, prefs, json, CsrfTokenProvider(api))
        assertTrue(repo.checkSession() is SessionState.Unknown)
    }

    @Test
    fun checkSession_isNotRejectedBySessionExpiryInterceptor() = runBlocking {
        // /home redirecting to /login would throw SessionExpiredException for normal requests.
        val client = OkHttpClient.Builder().cookieJar(cookieJar)
            .addInterceptor(com.futabooo.android.booklife.data.network.SessionExpiryInterceptor()).build()
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(BookmeterApi::class.java)
        val repo = SessionRepository(api, cookieJar, prefs, json, CsrfTokenProvider(api))
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "/login"))
        server.enqueue(html(loginForm))
        assertEquals(SessionState.LoggedOut, repo.checkSession())
        // The marker header is not sent to the server.
        assertNull(server.takeRequest().getHeader(com.futabooo.android.booklife.data.network.AuthFlow.HEADER))
    }

    @Test
    fun hasSessionCookie_reflectsCookieJar() {
        assertFalse(repository.hasSessionCookie())
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

    @Test
    fun logout_invalidatesCsrfToken() = runBlocking {
        server.enqueue(html(homeHtml))
        assertEquals("csrf789", csrf.get())
        repository.logout()
        server.enqueue(html(homeHtml.replace("csrf789", "csrfNew")))
        assertEquals("csrfNew", csrf.get())
    }

    @Test
    fun logout_clearsPreferencesEvenIfCookiesFail() = runBlocking {
        prefs.setUserId(5)
        val real = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("test_cookies_f", Context.MODE_PRIVATE)
        val throwingPrefs = java.lang.reflect.Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(android.content.SharedPreferences::class.java),
        ) { _, method, args ->
            if (method.name == "edit") throw IllegalStateException("disk full")
            method.invoke(real, *(args ?: emptyArray()))
        } as android.content.SharedPreferences
        val api = Retrofit.Builder().baseUrl(server.url("/")).build().create(BookmeterApi::class.java)
        val repo = SessionRepository(
            api, PersistentCookieJar(throwingPrefs), prefs, Json { ignoreUnknownKeys = true }, CsrfTokenProvider(api),
        )

        repo.logout() // must not throw

        assertNull(prefs.userId.first())
    }

    @Test
    fun login_invalidatesCsrfToken() = runBlocking {
        server.enqueue(html(homeHtml))
        csrf.get()
        server.enqueue(html(loginForm))
        server.enqueue(html(homeHtml))
        assertEquals(LoginResult.Success, repository.login("a@example.com", "secret"))
        server.enqueue(html(homeHtml.replace("csrf789", "csrfAfterLogin")))
        assertEquals("csrfAfterLogin", csrf.get())
    }
}
