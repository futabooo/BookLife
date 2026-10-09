package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.model.BookListMenu
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
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class BookActionRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: BookActionRepository

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(BookmeterApi::class.java)
        val csrf = com.futabooo.android.booklife.data.network.CsrfTokenProvider(api)
        csrf.update("tok")
        repository = BookActionRepository(api, csrf)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun addBook_sendsJsonHeaders() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":1}"""))
        repository.addBook(7, BookListMenu.TO_READ, 99)
        val r = server.takeRequest()
        assertEquals("POST", r.method)
        assertEquals("application/json", r.getHeader("Accept"))
        assertEquals("XMLHttpRequest", r.getHeader("X-Requested-With"))
        assertEquals("tok", r.getHeader("X-CSRF-Token"))
        assertTrue(r.body.readUtf8().contains("book%5Bbook_id%5D=99"))
    }

    @Test
    fun addBook_htmlBodyOn2xx_isSuccess() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "text/html").setBody("<html>home</html>"))
        repository.addBook(7, BookListMenu.TO_READ, 99)
    }

    @Test(expected = HttpException::class)
    fun addBook_4xx_throws(): Unit = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422))
        server.enqueue(
            MockResponse().setHeader("Content-Type", "text/html")
                .setBody("""<html><head><meta name="csrf-token" content="fresh"/></head></html>"""),
        )
        server.enqueue(MockResponse().setResponseCode(422))
        repository.addBook(7, BookListMenu.TO_READ, 99)
    }

    @Test
    fun addBook_4xx_refreshesTokenAndRetriesOnce() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422))
        server.enqueue(
            MockResponse().setHeader("Content-Type", "text/html")
                .setBody("""<html><head><meta name="csrf-token" content="fresh"/></head></html>"""),
        )
        server.enqueue(MockResponse().setBody("""{"id":1}"""))
        repository.addBook(7, BookListMenu.TO_READ, 99)
        assertEquals("tok", server.takeRequest().getHeader("X-CSRF-Token"))
        assertEquals("/home", server.takeRequest().path)
        assertEquals("fresh", server.takeRequest().getHeader("X-CSRF-Token"))
    }

    @Test
    fun updateReadBook_omitsReadAtWhenNull() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":1}"""))
        repository.updateReadBook(reviewId = 12, bookId = 99, readAt = null, review = "great", netabare = true)
        val r = server.takeRequest()
        assertEquals("PUT", r.method)
        assertEquals("/read_books/12.json", r.path)
        val body = r.body.readUtf8()
        assertTrue(body, !body.contains("read_at"))
        assertTrue(body, body.contains("read_book%5Bbook_id%5D=99"))
        assertTrue(body, body.contains("read_book%5Breview%5D=great"))
        assertTrue(body, body.contains("read_book%5Breview_is_netabare%5D=1"))
    }

    @Test
    fun updateReadBook_sendsReadAtWhenPicked() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":1}"""))
        repository.updateReadBook(reviewId = 12, bookId = 99, readAt = "2024/3/5", review = "great", netabare = false)
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains("read_book%5Bread_at%5D=2024%2F3%2F5"))
    }
}
