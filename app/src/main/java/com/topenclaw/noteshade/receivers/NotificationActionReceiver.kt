package com.topenclaw.noteshade.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.topenclaw.noteshade.data.AppDatabase
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1)
        if (noteId <= 0) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repo = NoteRepository(AppDatabase.get(context).noteDao())
            val settingsRepo = SettingsRepository(context)
            val note = repo.getNote(noteId) ?: run { pendingResult.finish(); return@launch }
            when (intent.action) {
                ACTION_ARCHIVE -> repo.upsert(note.copy(isArchived = true, showInNotification = false, updatedAt = System.currentTimeMillis()))
                ACTION_UNPIN_NOTIFICATION -> repo.upsert(note.copy(showInNotification = false, updatedAt = System.currentTimeMillis()))
            }
            NotificationHelper.syncPinnedNotifications(context, repo.getNotificationNotes(), settingsRepo.settings.first().notificationsEnabled)
            pendingResult.finish()
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
        const val ACTION_ARCHIVE = "com.topenclaw.noteshade.ARCHIVE"
        const val ACTION_UNPIN_NOTIFICATION = "com.topenclaw.noteshade.UNPIN_NOTIFICATION"
    }
}
