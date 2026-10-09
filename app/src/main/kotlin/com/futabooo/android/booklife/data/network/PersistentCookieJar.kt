package com.futabooo.android.booklife.data.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * A [CookieJar] that keeps cookies in memory and mirrors them to a dedicated SharedPreferences
 * file as JSON, so the bookmeter session survives process death. Expired cookies are dropped.
 * Thread-safe. The file is excluded from backup (`android:allowBackup="false"`).
 */
class PersistentCookieJar(
    private val prefs: SharedPreferences,
    private val clock: () -> Long = System::currentTimeMillis,
) : CookieJar {

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

    private val lock = Any()
    private val cookies = mutableListOf<Cookie>()

    init {
        synchronized(lock) {
            cookies += load()
            if (removeExpired()) persist()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        synchronized(lock) {
            for (new in cookies) {
                this.cookies.removeAll { it.sameIdentity(new) }
                this.cookies += new
            }
            removeExpired()
            persist()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(lock) {
        if (removeExpired()) persist()
        cookies.filter { it.matches(url) }
    }

    /** Removes every stored cookie (sign-out). */
    fun clear() {
        synchronized(lock) {
            cookies.clear()
            prefs.edit().remove(KEY_COOKIES).commit()
        }
    }

    private fun removeExpired(): Boolean {
        val now = clock()
        return cookies.removeAll { it.expiresAt <= now }
    }

    private fun persist() {
        val json = Json.encodeToString(LIST_SERIALIZER, cookies.map(StoredCookie::from))
        prefs.edit().putString(KEY_COOKIES, json).commit()
    }

    private fun load(): List<Cookie> {
        val json = prefs.getString(KEY_COOKIES, null) ?: return emptyList()
        return try {
            Json.decodeFromString(LIST_SERIALIZER, json).mapNotNull(StoredCookie::toCookie)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun Cookie.sameIdentity(other: Cookie) =
        name == other.name && domain == other.domain && path == other.path

    @Serializable
    private data class StoredCookie(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean,
    ) {
        fun toCookie(): Cookie? = try {
            Cookie.Builder()
                .name(name)
                .value(value)
                .expiresAt(expiresAt)
                .apply { if (hostOnly) hostOnlyDomain(domain) else domain(domain) }
                .path(path)
                .apply {
                    if (secure) secure()
                    if (httpOnly) httpOnly()
                }
                .build()
        } catch (e: IllegalArgumentException) {
            null
        }

        companion object {
            fun from(c: Cookie) = StoredCookie(
                name = c.name,
                value = c.value,
                expiresAt = c.expiresAt,
                domain = c.domain,
                path = c.path,
                secure = c.secure,
                httpOnly = c.httpOnly,
                hostOnly = c.hostOnly,
            )
        }
    }

    private companion object {
        const val PREFS_NAME = "booklife_cookies"
        const val KEY_COOKIES = "cookies"
        val LIST_SERIALIZER = ListSerializer(StoredCookie.serializer())
    }
}
