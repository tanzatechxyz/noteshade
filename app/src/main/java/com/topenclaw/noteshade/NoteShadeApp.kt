package com.topenclaw.noteshade

import android.app.Application
import androidx.room.Room
import com.topenclaw.noteshade.data.AppDatabase
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.notifications.NoteNotificationCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NoteShadeApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set
    lateinit var noteRepository: NoteRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var notificationCoordinator: NoteNotificationCoordinator
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "noteshade.db").build()
        noteRepository = NoteRepository(database.noteDao())
        settingsRepository = SettingsRepository(this)
        notificationCoordinator = NoteNotificationCoordinator(this, noteRepository, applicationScope)
    }
}
