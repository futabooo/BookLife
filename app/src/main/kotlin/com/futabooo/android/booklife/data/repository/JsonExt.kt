package com.futabooo.android.booklife.data.repository

import com.futabooo.android.booklife.data.network.SessionExpiredException
import com.futabooo.android.booklife.data.parser.HtmlParsers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/** Decodes the `resources` array of a bookmeter JSON response (empty list when absent). */
internal fun <T> JsonObject.decodeResources(json: Json, serializer: KSerializer<T>): List<T> {
    val array = this["resources"] as? JsonArray ?: return emptyList()
    return json.decodeFromJsonElement(ListSerializer(serializer), array)
}

/**
 * The `csrf-token` meta tag of [html]. Throws [SessionExpiredException] when the page is the login
 * form (the session is gone) and [IllegalStateException] when there is no token.
 */
internal fun csrfTokenOrThrow(html: String): String {
    if (HtmlParsers.isLoginPage(html)) throw SessionExpiredException()
    return HtmlParsers.csrfToken(html) ?: throw IllegalStateException("csrf-token meta tag not found")
}
