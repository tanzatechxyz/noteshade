package com.topenclaw.noteshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topenclaw.noteshade.data.Note
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.data.SortOrder
import com.topenclaw.noteshade.data.UserSettings
import com.topenclaw.noteshade.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val archived: List<Note> = emptyList(),
    val query: String = "",
    val selectedSort: SortOrder = SortOrder.PINNED,
    val settings: UserSettings = UserSettings(),
    val firstRunVisible: Boolean = true
)

class NotesViewModel(
    private val repo: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedSort = MutableStateFlow<SortOrder?>(null)

    val state: StateFlow<NotesUiState> = combine(repo.observeNotes(), settingsRepository.settings, query, selectedSort) { notes, settings, q, chosenSort ->
        val sort = chosenSort ?: settings.defaultSort
        val filtered = notes.filter { (it.title + "\n" + it.body).contains(q, ignoreCase = true) }
        val active = sortNotes(filtered.filterNot { it.isArchived }, sort)
        val archived = sortNotes(filtered.filter { it.isArchived }, sort)
        NotesUiState(active, archived, q, sort, settings, !settings.firstRunDone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun updateQuery(value: String) { query.value = value }
    fun updateSort(sortOrder: SortOrder) { selectedSort.value = sortOrder }
    fun dismissFirstRun() = viewModelScope.launch { settingsRepository.markFirstRunDone() }

    fun togglePinned(note: Note) = mutate(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    fun toggleNotification(note: Note) = mutate(note.copy(showInNotification = !note.showInNotification, updatedAt = System.currentTimeMillis()))
    fun archive(note: Note) = mutate(note.copy(isArchived = true, showInNotification = false, updatedAt = System.currentTimeMillis()))
    fun unarchive(note: Note) = mutate(note.copy(isArchived = false, updatedAt = System.currentTimeMillis()))
    fun delete(note: Note) = viewModelScope.launch { repo.delete(note); syncNotifications() }

    private fun mutate(note: Note) = viewModelScope.launch {
        repo.upsert(note)
        syncNotifications()
    }

    private suspend fun syncNotifications() {
        NotificationHelper.syncPinnedNotifications(context, repo.getNotificationNotes(), state.value.settings.notificationsEnabled)
    }

    private fun sortNotes(notes: List<Note>, sort: SortOrder): List<Note> = when (sort) {
        SortOrder.PINNED -> notes.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
        SortOrder.NEWEST -> notes.sortedByDescending { it.createdAt }
        SortOrder.OLDEST -> notes.sortedBy { it.createdAt }
        SortOrder.RECENTLY_UPDATED -> notes.sortedByDescending { it.updatedAt }
    }
}
