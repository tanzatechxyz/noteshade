package com.topenclaw.noteshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topenclaw.noteshade.data.Note
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

data class NoteEditorState(
    val noteId: Long = 0,
    val title: String = "",
    val body: String = "",
    val isPinned: Boolean = false,
    val showInNotification: Boolean = false,
    val isArchived: Boolean = false,
    val autoSaveEnabled: Boolean = true,
    val saved: Boolean = true,
    val error: String? = null
)

class NoteDetailViewModel(
    private val repo: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    fun load(noteId: Long?) = viewModelScope.launch {
        val settings = settingsRepository.settings.first()
        val note = noteId?.takeIf { it > 0 }?.let { repo.getNote(it) }
        _state.value = if (note == null) {
            NoteEditorState(autoSaveEnabled = settings.autoSaveEnabled)
        } else {
            NoteEditorState(
                noteId = note.id,
                title = note.title,
                body = note.body,
                isPinned = note.isPinned,
                showInNotification = note.showInNotification,
                isArchived = note.isArchived,
                autoSaveEnabled = settings.autoSaveEnabled,
                saved = true
            )
        }
    }

    fun updateTitle(value: String) { _state.value = _state.value.copy(title = value, saved = false) }
    fun updateBody(value: String) { _state.value = _state.value.copy(body = value, saved = false) }
    fun togglePinned() {
        val willBePinned = !_state.value.isPinned
        _state.value = _state.value.copy(
            isPinned = willBePinned,
            showInNotification = if (willBePinned) true else _state.value.showInNotification,
            saved = false
        )
    }
    fun toggleNotification() { _state.value = _state.value.copy(showInNotification = !_state.value.showInNotification, saved = false) }
    fun archive(onDone: () -> Unit = {}) = viewModelScope.launch {
        _state.value = _state.value.copy(isArchived = true, showInNotification = false, saved = false)
        save { onDone() }
    }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun save(onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        try {
            val now = System.currentTimeMillis()
            val previous = _state.value.noteId.takeIf { it > 0 }?.let { repo.getNote(it) }
            val id = repo.upsert(
                Note(
                    id = _state.value.noteId,
                    title = _state.value.title.trim(),
                    body = _state.value.body,
                    isPinned = _state.value.isPinned,
                    showInNotification = _state.value.showInNotification,
                    isArchived = _state.value.isArchived,
                    createdAt = previous?.createdAt ?: now,
                    updatedAt = now
                )
            )
            _state.value = _state.value.copy(noteId = id, saved = true)
            NotificationHelper.syncPinnedNotifications(
                context,
                repo.getNotificationNotes(),
                settingsRepository.settings.first().notificationsEnabled,
                repo.getAllNoteIds()
            )
            onDone(id)
        } catch (t: Throwable) {
            _state.value = _state.value.copy(error = t.message ?: "Failed to save note")
        }
    }

    fun exportAll(outputStream: OutputStream, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        try {
            val array = JSONArray()
            repo.observeNotes().first().forEach { note ->
                array.put(JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("body", note.body)
                    put("isPinned", note.isPinned)
                    put("isArchived", note.isArchived)
                    put("showInNotification", note.showInNotification)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                })
            }
            outputStream.writer().use { it.write(array.toString(2)) }
            onDone(true)
        } catch (_: Throwable) {
            onDone(false)
        }
    }
}
