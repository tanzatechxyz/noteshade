package com.topenclaw.noteshade.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.topenclaw.noteshade.NoteShadeApp

class NoteActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as NoteShadeApp
        val noteId = intent.getLongExtra(NoteNotificationCoordinator.EXTRA_NOTE_ID, -1L)
        if (noteId <= 0L) return
        val pending = goAsync()
        app.notificationCoordinator.runAction(intent.action.orEmpty(), noteId) {
            pending.finish()
        }
    }
}
