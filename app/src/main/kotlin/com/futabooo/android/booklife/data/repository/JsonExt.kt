package com.futabooo.android.booklife.data.repository

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

internal fun requireCsrfToken(token: String?): String =
    token ?: throw IllegalStateException("csrf-token meta tag not found")
