package com.topenclaw.noteshade

import android.app.Application
import com.topenclaw.noteshade.notifications.NotificationHelper

class NoteShadeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }
}
