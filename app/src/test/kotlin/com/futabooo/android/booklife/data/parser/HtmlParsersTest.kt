package com.futabooo.android.booklife.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlParsersTest {

    private val loginHtml: String =
        javaClass.classLoader!!.getResourceAsStream("bookmeter_login.html")!!
            .bufferedReader().use { it.readText() }

    @Test
    fun loginPage_hasTokens_andIsLoginPage() {
        assertTrue(HtmlParsers.authenticityToken(loginHtml).isNotBlank())
        assertTrue(HtmlParsers.csrfToken(loginHtml).orEmpty().isNotBlank())
        assertTrue(HtmlParsers.isLoginPage(loginHtml))
        assertFalse(HtmlParsers.hasLoginAlert(loginHtml))
    }

    @Test
    fun authenticityToken_missing_isEmpty() {
        assertEquals("", HtmlParsers.authenticityToken("<html></html>"))
        assertNull(HtmlParsers.csrfToken("<html></html>"))
        assertFalse(HtmlParsers.isLoginPage("<html></html>"))
    }

    @Test
    fun loginAlert_detected() {
        val html = """<div class="container"><ul><li class="bm-flash-item--alert">NG</li></ul></div>"""
        assertTrue(HtmlParsers.hasLoginAlert(html))
        assertFalse(HtmlParsers.hasLoginAlert("""<ul><li class="bm-flash-item--alert">NG</li></ul>"""))
    }

    @Test
    fun csrfToken_parsed() {
        val html = """<head><meta name="csrf-token" content="abc=="/></head>"""
        assertEquals("abc==", HtmlParsers.csrfToken(html))
    }

    @Test
    fun homeStats_parsed() {
        val html = """
            <div class="stats__thismonth"><ul class="thismonth__list">
              <li class="list__item"><span class="item__number">1,234</span></li>
              <li class="list__item"><span class="item__number">5</span></li>
              <li class="list__item"><span class="item__number">41</span></li>
            </ul></div>
        """.trimIndent()
        val stats = HtmlParsers.homeStats(html)!!
        assertEquals("1,234", stats.pages)
        assertEquals("5", stats.volumes)
        assertEquals("41", stats.pagesPerDay)
        assertNull(HtmlParsers.homeStats("<div></div>"))
    }

    @Test
    fun userId_parsed() {
        val html = """
            <div class="bm-block-side__content"><dl class="user-profiles">
              <dt class="user-profiles__avatar"><a href="/users/123456"><img></a></dt>
            </dl></div>
        """.trimIndent()
        assertEquals(123456, HtmlParsers.userId(html))
        assertNull(HtmlParsers.userId("<div></div>"))
    }

    @Test
    fun bookDetail_parsed() {
        val html = """
            <div class="header__inner">
              <h1 class="inner__title">吾輩は猫である</h1>
              <ul class="header__authors"><li>夏目漱石</li></ul>
            </div>
            <div class="group__image"><a href="https://amazon.example/x"><img src="https://img.example/t.jpg"></a></div>
        """.trimIndent()
        val detail = HtmlParsers.bookDetail(html)
        assertEquals("吾輩は猫である", detail.title)
        assertEquals("夏目漱石", detail.author)
        assertEquals("https://img.example/t.jpg", detail.thumbnail)
        assertEquals("https://amazon.example/x", detail.amazonUrl)
    }

    private val searchPartialHtml: String =
        javaClass.classLoader!!.getResourceAsStream("search_partial.html")!!
            .bufferedReader().use { it.readText() }

    @Test
    fun searchResults_parsesFixture() {
        val items = HtmlParsers.searchResults(searchPartialHtml)
        assertEquals(20, items.size)
        val book = items.first().contents.book
        assertEquals(12901965, book.id)
        assertEquals("やさしいKotlin入門", book.title)
        assertEquals("野崎 英一", book.author.name)
        assertEquals(17, book.registrationCount)
        assertEquals(315, book.page)
        assertEquals("https://m.media-amazon.com/images/I/51qL1Vv2URL._SL500_.jpg", book.imageUrl)
        assertEquals("", items.first().status)
        assertTrue(items.all { it.contents.book.id != 0 })
    }

    @Test
    fun searchResults_empty() {
        assertTrue(HtmlParsers.searchResults("<span id=\"contents\"></span>").isEmpty())
    }
}
