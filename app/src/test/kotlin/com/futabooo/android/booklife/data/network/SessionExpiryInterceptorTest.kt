package com.futabooo.android.booklife.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SessionExpiryInterceptorTest {

    private lateinit var server: MockWebServer
    private val client = OkHttpClient.Builder().addInterceptor(SessionExpiryInterceptor()).build()

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() = server.shutdown()

    private fun get(path: String, authFlow: Boolean = false) =
        client.newCall(
            Request.Builder().url(server.url(path)).apply {
                if (authFlow) header(AuthFlow.HEADER, "1")
            }.build(),
        ).execute()

    @Test
    fun redirectToLogin_throwsSessionExpired() {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "/login"))
        server.enqueue(MockResponse().setBody("login"))
        try {
            get("/home")
            fail("expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            assertTrue(e.isSessionExpired())
        }
    }

    @Test
    fun unauthorized_throwsSessionExpired() {
        server.enqueue(MockResponse().setResponseCode(401))
        try {
            get("/home.json")
            fail("expected SessionExpiredException")
        } catch (e: SessionExpiredException) {
            // expected
        }
    }

    @Test
    fun normalResponses_passThrough() {
        server.enqueue(MockResponse().setBody("ok"))
        server.enqueue(MockResponse().setResponseCode(422))
        server.enqueue(MockResponse().setResponseCode(500))
        assertEquals("ok", get("/home").body.string())
        assertEquals(422, get("/x").code)
        assertEquals(500, get("/y").code)
    }

    @Test
    fun requestStartingAtLogin_isNotExpiry() {
        server.enqueue(MockResponse().setBody("login form"))
        assertEquals("login form", get("/login").body.string())
    }

    @Test
    fun authFlowRequest_isNotChecked_andHeaderIsStripped() {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", "/login"))
        server.enqueue(MockResponse().setBody("login"))
        server.enqueue(MockResponse().setResponseCode(401))
        val ok = get("/home", authFlow = true)
        assertEquals("/login", ok.request.url.encodedPath)
        ok.close()
        assertNull(server.takeRequest().getHeader(AuthFlow.HEADER))
        assertNull(server.takeRequest().getHeader(AuthFlow.HEADER))
        assertEquals(401, get("/home", authFlow = true).code)
        assertFalse(server.takeRequest().headers.names().contains(AuthFlow.HEADER))
    }

    @Test
    fun isSessionExpired_followsCauses() {
        assertTrue(RuntimeException("x", SessionExpiredException()).isSessionExpired())
        assertFalse(RuntimeException("x").isSessionExpired())
    }
}
