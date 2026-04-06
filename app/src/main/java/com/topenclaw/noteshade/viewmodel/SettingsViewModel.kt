package com.topenclaw.noteshade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.data.SortOrder
import com.topenclaw.noteshade.data.ThemeMode
import com.topenclaw.noteshade.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val state: StateFlow<UserSettings> = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())
    fun setThemeMode(value: ThemeMode) = viewModelScope.launch { repository.setThemeMode(value) }
    fun setDefaultSort(value: SortOrder) = viewModelScope.launch { repository.setDefaultSort(value) }
    fun setNotificationsEnabled(value: Boolean) = viewModelScope.launch { repository.setNotificationsEnabled(value) }
    fun setAutoSaveEnabled(value: Boolean) = viewModelScope.launch { repository.setAutoSaveEnabled(value) }
}
