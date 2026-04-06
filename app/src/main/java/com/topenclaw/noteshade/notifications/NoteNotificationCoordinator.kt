package com.topenclaw.noteshade.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.topenclaw.noteshade.MainActivity
import com.topenclaw.noteshade.data.NoteEntity
import com.topenclaw.noteshade.data.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteNotificationCoordinator(
    private val context: Context,
    private val repository: NoteRepository,
    scope: CoroutineScope,
) {
    private val manager = NotificationManagerCompat.from(context)
    private var syncedIds: Set<Int> = emptySet()

    init {
        createChannel()
        scope.launch {
            repository.observeNotificationNotes().collectLatest { notes ->
                sync(notes)
            }
        }
    }

    private fun sync(notes: List<NoteEntity>) {
        val activeIds = notes.map { notificationId(it.id) }.toSet()
        notes.forEach { note -> manager.notify(notificationId(note.id), buildNotification(note)) }
        (syncedIds - activeIds).forEach { manager.cancel(it) }
        syncedIds = activeIds
    }

    private fun buildNotification(note: NoteEntity) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(note.title.ifBlank { "Untitled note" })
        .setContentText(note.body.ifBlank { "Tap to keep writing." })
        .setStyle(NotificationCompat.BigTextStyle().bigText(note.body.ifBlank { "Tap to keep writing." }))
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(openNoteIntent(note.id))
        .addAction(0, if (note.isPinned) "Unpin" else "Pin", actionPendingIntent(ACTION_PIN, note.id))
        .addAction(0, "Archive", actionPendingIntent(ACTION_ARCHIVE, note.id))
        .addAction(0, "Hide", actionPendingIntent(ACTION_HIDE, note.id))
        .build()

    private fun openNoteIntent(noteId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_NOTE_ID, noteId)
        }
        return PendingIntent.getActivity(context, noteId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun actionPendingIntent(action: String, noteId: Long): PendingIntent {
        val intent = Intent(context, NoteActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_NOTE_ID, noteId)
        }
        return PendingIntent.getBroadcast(context, (noteId + action.hashCode()).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Pinned notes", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Persistent note notifications"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun runAction(action: String, noteId: Long, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            when (action) {
                ACTION_PIN -> repository.togglePinned(noteId)
                ACTION_ARCHIVE -> repository.toggleArchived(noteId)
                ACTION_HIDE -> repository.toggleNotification(noteId)
            }
            onComplete()
        }
    }

    companion object {
        const val ACTION_PIN = "com.topenclaw.noteshade.PIN"
        const val ACTION_ARCHIVE = "com.topenclaw.noteshade.ARCHIVE"
        const val ACTION_HIDE = "com.topenclaw.noteshade.HIDE"
        const val EXTRA_NOTE_ID = "note_id"
        private const val CHANNEL_ID = "pinned_notes"
        private const val BASE_ID = 4100

        fun notificationId(noteId: Long): Int = BASE_ID + noteId.toInt()
    }
}
