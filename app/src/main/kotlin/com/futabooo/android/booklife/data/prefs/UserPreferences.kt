package com.futabooo.android.booklife.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** App settings stored in DataStore. Only the bookmeter user id is kept (no credentials). */
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** bookmeter user id, or null when it has not been resolved yet. */
    val userId: Flow<Int?> = dataStore.data.map { it[USER_ID] }

    suspend fun setUserId(id: Int) {
        dataStore.edit { it[USER_ID] = id }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val USER_ID = intPreferencesKey("user_id")
    }
}
