package com.topenclaw.noteshade.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultSort: SortOrder = SortOrder.PINNED,
    val notificationsEnabled: Boolean = true,
    val autoSaveEnabled: Boolean = true,
    val firstRunDone: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val defaultSort = stringPreferencesKey("default_sort")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val autoSaveEnabled = booleanPreferencesKey("auto_save_enabled")
        val firstRunDone = booleanPreferencesKey("first_run_done")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            themeMode = prefs[Keys.themeMode]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
            defaultSort = prefs[Keys.defaultSort]?.let { SortOrder.valueOf(it) } ?: SortOrder.PINNED,
            notificationsEnabled = prefs[Keys.notificationsEnabled] ?: true,
            autoSaveEnabled = prefs[Keys.autoSaveEnabled] ?: true,
            firstRunDone = prefs[Keys.firstRunDone] ?: false
        )
    }

    suspend fun setThemeMode(value: ThemeMode) = context.dataStore.edit { it[Keys.themeMode] = value.name }
    suspend fun setDefaultSort(value: SortOrder) = context.dataStore.edit { it[Keys.defaultSort] = value.name }
    suspend fun setNotificationsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.notificationsEnabled] = value }
    suspend fun setAutoSaveEnabled(value: Boolean) = context.dataStore.edit { it[Keys.autoSaveEnabled] = value }
    suspend fun markFirstRunDone() = context.dataStore.edit { it[Keys.firstRunDone] = true }
}
