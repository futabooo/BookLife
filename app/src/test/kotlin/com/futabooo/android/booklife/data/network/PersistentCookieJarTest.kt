package com.futabooo.android.booklife.data.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersistentCookieJarTest {

    private val url = "https://bookmeter.com/home".toHttpUrl()
    private var now = 1_000_000L
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("booklife_cookies", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun newJar() = PersistentCookieJar(
        context.getSharedPreferences("booklife_cookies", Context.MODE_PRIVATE),
        clock = { now },
    )

    private fun cookie(name: String, value: String, expiresAt: Long = now + 60_000) =
        Cookie.Builder().name(name).value(value).domain("bookmeter.com").path("/")
            .secure().httpOnly().expiresAt(expiresAt).build()

    @Test
    fun saveAndLoad_roundTripsAcrossInstances() {
        newJar().saveFromResponse(url, listOf(cookie("_session", "abc")))

        val loaded = newJar().loadForRequest(url)

        assertEquals(1, loaded.size)
        assertEquals("_session", loaded[0].name)
        assertEquals("abc", loaded[0].value)
        assertTrue(loaded[0].secure)
        assertTrue(loaded[0].httpOnly)
    }

    @Test
    fun sameNameReplacesOldValue() {
        val jar = newJar()
        jar.saveFromResponse(url, listOf(cookie("k", "1")))
        jar.saveFromResponse(url, listOf(cookie("k", "2")))

        assertEquals(listOf("2"), newJar().loadForRequest(url).map { it.value })
    }

    @Test
    fun expiredCookiesAreDropped() {
        val jar = newJar()
        jar.saveFromResponse(url, listOf(cookie("short", "x", expiresAt = now + 1_000), cookie("long", "y")))
        assertEquals(2, jar.loadForRequest(url).size)

        now += 2_000

        assertEquals(listOf("long"), jar.loadForRequest(url).map { it.name })
        assertEquals(listOf("long"), newJar().loadForRequest(url).map { it.name })
    }

    @Test
    fun cookiesOnlyMatchTheirHost() {
        val jar = newJar()
        jar.saveFromResponse(url, listOf(cookie("a", "1")))

        assertTrue(jar.loadForRequest("https://example.com/".toHttpUrl()).isEmpty())
    }

    @Test
    fun clearRemovesEverything() {
        val jar = newJar()
        jar.saveFromResponse(url, listOf(cookie("a", "1")))

        jar.clear()

        assertTrue(jar.loadForRequest(url).isEmpty())
        assertTrue(newJar().loadForRequest(url).isEmpty())
    }
}
