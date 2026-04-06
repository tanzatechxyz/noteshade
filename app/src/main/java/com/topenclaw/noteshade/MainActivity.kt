package com.topenclaw.noteshade

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.topenclaw.noteshade.navigation.NoteShadeNavHost
import com.topenclaw.noteshade.ui.NoteShadeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pendingOpenNoteId = MutableStateFlow<Long?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        maybeRequestNotificationPermission()

        val app = application as NoteShadeApp
        setContent {
            val settings = app.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = com.topenclaw.noteshade.data.AppSettings()
            )
            NoteShadeTheme(
                darkTheme = settings.value.darkTheme,
                dynamicColor = settings.value.dynamicColor,
            ) {
                NoteShadeNavHost(
                    noteRepository = app.noteRepository,
                    settingsRepository = app.settingsRepository,
                    pendingOpenNoteId = pendingOpenNoteId.asStateFlow(),
                    onPendingOpenHandled = { pendingOpenNoteId.value = null },
                    onMarkOnboardingSeen = {
                        lifecycleScope.launch { app.settingsRepository.setOnboardingSeen(true) }
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        pendingOpenNoteId.value = intent?.getLongExtra(EXTRA_OPEN_NOTE_ID, -1L)?.takeIf { it > 0L }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        const val EXTRA_OPEN_NOTE_ID = "open_note_id"
    }
}
