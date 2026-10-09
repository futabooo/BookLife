package com.futabooo.android.booklife.data.parser

import com.futabooo.android.booklife.data.model.Author
import com.futabooo.android.booklife.data.model.Book
import com.futabooo.android.booklife.data.model.SearchResultContents
import com.futabooo.android.booklife.data.model.SearchResultResource
import org.jsoup.Jsoup

/** Pure functions that scrape bookmeter.com HTML pages. Selectors are ported from the old app. */
object HtmlParsers {

    data class HomeStatsHtml(val pages: String, val volumes: String, val pagesPerDay: String)

    data class BookDetailHtml(
        val title: String,
        val author: String,
        val thumbnail: String,
        val amazonUrl: String,
    )

    /** Login form hidden token. Empty when not found. */
    fun authenticityToken(html: String): String =
        Jsoup.parse(html).select("form input[name=authenticity_token]").attr("value")

    /** `<meta name="csrf-token">`; null when the page has none. */
    fun csrfToken(html: String): String? =
        Jsoup.parse(html).select("meta[name=csrf-token]").firstOrNull()?.attr("content")

    /** True when the page shows the "wrong e-mail or password" flash alert. */
    fun hasLoginAlert(html: String): Boolean =
        Jsoup.parse(html).select("div.container li.bm-flash-item--alert").isNotEmpty()

    /** True when the page contains the login form. */
    fun isLoginPage(html: String): Boolean =
        Jsoup.parse(html).select("form input[name=session[email_address]]").isNotEmpty()

    /** This month's stats on /home: index 0 pages, 1 volumes, 2 pages/day. Null if absent. */
    fun homeStats(html: String): HomeStatsHtml? {
        val items = Jsoup.parse(html)
            .select("div.stats__thismonth ul.thismonth__list li.list__item span.item__number")
        if (items.size < 3) return null
        return HomeStatsHtml(items[0].text(), items[1].text(), items[2].text())
    }

    /** Signed-in user's id from the profile avatar link (`/users/<id>`); null if absent. */
    fun userId(html: String): Int? {
        val href = Jsoup.parse(html)
            .select("div.bm-block-side__content dl.user-profiles dt.user-profiles__avatar a")
            .attr("href")
        if (href.length <= 7) return null
        return href.substring(7).toIntOrNull()
    }

    fun bookDetail(html: String): BookDetailHtml {
        val doc = Jsoup.parse(html)
        return BookDetailHtml(
            title = doc.select("div.header__inner h1.inner__title").text(),
            author = doc.select("div.header__inner ul.header__authors").text(),
            thumbnail = doc.select("div.group__image img").attr("src"),
            amazonUrl = doc.select("div.group__image a").attr("href"),
        )
    }

    private const val DEFAULT_ACTION_TEXT = "本を登録する"

    /**
     * Items of the partial search response (`li.group__book`). Empty list = no (more) results.
     * The read-status mark is taken from `div.cover__icon` text, else `span.action__text` when it is
     * not the default "本を登録する", else "".
     */
    fun searchResults(html: String): List<SearchResultResource> =
        Jsoup.parse(html).select("li.group__book").mapNotNull { li ->
            val href = li.select("div.detail__title a").attr("href")
                .ifEmpty { li.select("div.thumbnail__cover a").attr("href") }
            val path = href.substringBefore('?').trimEnd('/')
            val id = path.substringAfterLast('/').toIntOrNull() ?: return@mapNotNull null
            val authorLink = li.select("ul.detail__authors li a").firstOrNull()
            val authorName = li.select("ul.detail__authors li").firstOrNull()?.text()?.trim()
            val iconText = li.select("div.cover__icon").text().trim()
            val actionText = li.select("span.action__text").text().trim()
            val status = iconText.ifEmpty { actionText.takeUnless { it == DEFAULT_ACTION_TEXT }.orEmpty() }
            SearchResultResource(
                contents = SearchResultContents(
                    book = Book(
                        id = id,
                        title = li.select("div.detail__title a").text().trim()
                            .ifEmpty { li.select("img.cover__image").attr("alt") },
                        imageUrl = li.select("img.cover__image").attr("src"),
                        path = path,
                        page = li.select("div.detail__page").text().trim().toIntOrNull() ?: 0,
                        registrationCount = li.select("dl.detail__options dd.options__item").firstOrNull()
                            ?.text()?.trim()?.replace(",", "")?.toIntOrNull() ?: 0,
                        author = Author(name = authorName, path = authorLink?.attr("href")),
                    ),
                ),
                status = status,
                statusText = status,
            )
        }
}
