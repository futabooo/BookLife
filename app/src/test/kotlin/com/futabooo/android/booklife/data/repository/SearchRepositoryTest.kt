package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.network.BookmeterApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class SearchRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: SearchRepository

    private val searchPage = """<html><head><meta name="csrf-token" content="csrfX"/></head><body></body></html>"""
    private val partial: String =
        javaClass.classLoader!!.getResourceAsStream("search_partial.html")!!
            .bufferedReader().use { it.readText() }

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = SearchRepository(retrofit.create(BookmeterApi::class.java))
    }

    @After
    fun tearDown() = server.shutdown()

    private fun html(body: String) = MockResponse().setHeader("Content-Type", "text/html; charset=utf-8").setBody(body)

    @Test
    fun search_usesPartialEndpoint_andPages() = runBlocking {
        server.enqueue(html(searchPage))
        server.enqueue(html(partial))
        server.enqueue(html(partial))

        val first = repository.search("kotlin", 0)
        assertEquals("csrfX", first.csrfToken)
        assertEquals(20, first.items.size)
        repository.search("kotlin", 20)

        assertTrue(server.takeRequest().path!!.startsWith("/search?keyword=kotlin"))
        val r1 = server.takeRequest()
        val p1 = r1.path!!
        assertTrue(p1, p1.contains("partial=true"))
        assertTrue(p1, p1.contains("type=japanese_v2"))
        assertTrue(p1, p1.contains("sort=recommended"))
        assertTrue(p1, p1.contains("page=1"))
        assertEquals("csrfX", r1.getHeader("X-CSRF-Token"))
        assertEquals("XMLHttpRequest", r1.getHeader("X-Requested-With"))
        // csrf token is cached: no second HTML page fetch.
        val p2 = server.takeRequest().path!!
        assertTrue(p2, p2.contains("page=2"))
    }

    @Test
    fun search_refreshesCsrfOn4xx() = runBlocking {
        server.enqueue(html(searchPage))
        server.enqueue(MockResponse().setResponseCode(422))
        server.enqueue(html(searchPage.replace("csrfX", "csrfY")))
        server.enqueue(html(partial))

        val page = repository.search("kotlin", 0)

        assertEquals("csrfY", page.csrfToken)
        assertEquals(20, page.items.size)
        assertEquals(4, server.requestCount)
    }
}
