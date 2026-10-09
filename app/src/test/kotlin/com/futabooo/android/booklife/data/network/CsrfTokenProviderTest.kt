package com.futabooo.android.booklife.data.network

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class CsrfTokenProviderTest {

    private lateinit var server: MockWebServer
    private lateinit var provider: CsrfTokenProvider

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build().create(BookmeterApi::class.java)
        provider = CsrfTokenProvider(api)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun home(token: String) = MockResponse().setHeader("Content-Type", "text/html")
        .setBody("""<html><head><meta name="csrf-token" content="$token"/></head><body></body></html>""")

    @Test
    fun get_isCached() = runBlocking {
        server.enqueue(home("a"))
        assertEquals("a", provider.get())
        assertEquals("a", provider.get())
        assertEquals(1, server.requestCount)
        assertEquals("/home", server.takeRequest().path)
    }

    @Test
    fun refresh_replacesCache() = runBlocking {
        server.enqueue(home("a"))
        server.enqueue(home("b"))
        provider.get()
        assertEquals("b", provider.refresh())
        assertEquals("b", provider.get())
        assertEquals(2, server.requestCount)
    }

    @Test
    fun invalidate_forcesRefetch() = runBlocking {
        server.enqueue(home("a"))
        server.enqueue(home("b"))
        provider.get()
        provider.invalidate()
        assertEquals("b", provider.get())
    }

    @Test
    fun withToken_refreshesOn4xxAndRetriesOnce() = runBlocking {
        server.enqueue(home("a"))
        server.enqueue(home("b"))
        val used = mutableListOf<String>()
        val result = provider.withToken { token ->
            used += token
            if (token == "a") throw HttpException(retrofit2.Response.error<Any>(422, okhttp3.ResponseBody.create(null, "")))
            "done"
        }
        assertEquals("done", result)
        assertEquals(listOf("a", "b"), used)
    }

    @Test
    fun withToken_doesNotRetryOn5xx() {
        server.enqueue(home("a"))
        try {
            runBlocking {
                provider.withToken {
                    throw HttpException(retrofit2.Response.error<Any>(500, okhttp3.ResponseBody.create(null, "")))
                }
            }
            fail()
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun refresh_onLoginPage_throwsSessionExpired() {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "text/html").setBody(
                """<html><head><meta name="csrf-token" content="x"/></head>
                   <form><input name="session[email_address]"/></form></html>""",
            ),
        )
        try {
            runBlocking { provider.refresh() }
            fail()
        } catch (e: SessionExpiredException) {
            assertTrue(true)
        }
    }

    @Test
    fun update_setsCache() = runBlocking {
        provider.update("z")
        assertEquals("z", provider.get())
        assertEquals(0, server.requestCount)
    }
}
