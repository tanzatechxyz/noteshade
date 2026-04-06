package com.topenclaw.noteshade.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }
    )

    val settings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            AppSettings(
                onboardingSeen = prefs[ONBOARDING_SEEN] ?: false,
                darkTheme = prefs[DARK_THEME] ?: false,
                dynamicColor = prefs[DYNAMIC_COLOR] ?: true,
                defaultSort = NoteSortOption.valueOf(prefs[DEFAULT_SORT] ?: NoteSortOption.RECENTLY_UPDATED.name),
            )
        }

    suspend fun setOnboardingSeen(seen: Boolean) {
        dataStore.edit { it[ONBOARDING_SEEN] = seen }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDefaultSort(sort: NoteSortOption) {
        dataStore.edit { it[DEFAULT_SORT] = sort.name }
    }

    companion object {
        private val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val DEFAULT_SORT = stringPreferencesKey("default_sort")
    }
}

data class AppSettings(
    val onboardingSeen: Boolean = false,
    val darkTheme: Boolean = false,
    val dynamicColor: Boolean = true,
    val defaultSort: NoteSortOption = NoteSortOption.RECENTLY_UPDATED,
)

enum class NoteSortOption(val label: String) {
    RECENTLY_UPDATED("Recently updated"),
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    PINNED_FIRST("Pinned first"),
}
