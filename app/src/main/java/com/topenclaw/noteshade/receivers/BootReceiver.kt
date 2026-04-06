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

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repo = NoteRepository(AppDatabase.get(context).noteDao())
            val settings = SettingsRepository(context).settings.first()
            NotificationHelper.syncPinnedNotifications(context, repo.getNotificationNotes(), settings.notificationsEnabled, repo.getAllNoteIds())
            pendingResult.finish()
        }
    }
}
