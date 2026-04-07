package com.topenclaw.noteshade.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.topenclaw.noteshade.MainActivity
import com.topenclaw.noteshade.R
import com.topenclaw.noteshade.data.Note
import com.topenclaw.noteshade.receivers.NotificationActionReceiver

object NotificationHelper {
    private const val CHANNEL_ID = "pinned_notes"
    private const val NOTIFICATION_ID_OFFSET = 1000

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "Pinned notes", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Persistent note reminders in the notification shade"
        }
        manager.createNotificationChannel(channel)
    }

    fun syncPinnedNotifications(context: Context, notes: List<Note>, enabled: Boolean, knownNoteIds: List<Long> = notes.map { it.id }) {
        ensureChannels(context)
        val manager = NotificationManagerCompat.from(context)
        knownNoteIds.forEach { manager.cancel(notificationId(it)) }
        if (!enabled || !canPost(context)) return
        notes.filter { it.showInNotification && !it.isArchived }
            .forEach { note -> manager.notify(notificationId(note.id), buildNoteNotification(context, note)) }
    }

    fun cancel(context: Context, noteId: Long) {
        NotificationManagerCompat.from(context).cancel(noteId.toInt() + 1000)
    }

    private fun buildNoteNotification(context: Context, note: Note): android.app.Notification {
        val previewText = note.body.ifBlank {
            note.title.ifBlank { "Tap to open" }
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_note)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(previewText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(previewText))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent(context, note.id))
            .setDeleteIntent(broadcastIntent(context, note.id, NotificationActionReceiver.ACTION_RESTORE_NOTIFICATION))
            .addAction(0, "Edit", openIntent(context, note.id))
            .addAction(0, "Archive", broadcastIntent(context, note.id, NotificationActionReceiver.ACTION_ARCHIVE))
            .build()
    }

    private fun openIntent(context: Context, noteId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_note_id", noteId)
        }
        return PendingIntent.getActivity(context, noteId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun broadcastIntent(context: Context, noteId: Long, action: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_NOTE_ID, noteId)
        }
        return PendingIntent.getBroadcast(context, noteId.toInt() + action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun canPost(context: Context): Boolean = Build.VERSION.SDK_INT < 33 ||
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun notificationId(noteId: Long): Int = noteId.toInt() + NOTIFICATION_ID_OFFSET
}
