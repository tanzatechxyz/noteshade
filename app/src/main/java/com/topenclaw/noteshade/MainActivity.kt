package com.topenclaw.noteshade

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topenclaw.noteshade.data.AppDatabase
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.ui.navigation.NoteShadeAppRoot
import com.topenclaw.noteshade.ui.theme.NoteShadeTheme
import com.topenclaw.noteshade.viewmodel.NoteDetailViewModel
import com.topenclaw.noteshade.viewmodel.NotesViewModel
import com.topenclaw.noteshade.viewmodel.SettingsViewModel
import com.topenclaw.noteshade.viewmodel.ViewModelFactories

class MainActivity : ComponentActivity() {
    private var openNoteId by mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openNoteId = intent.noteIdToOpen()
        val repo = NoteRepository(AppDatabase.get(this).noteDao())
        val settingsRepo = SettingsRepository(this)
        setContent {
            val notesVm: NotesViewModel = viewModel(factory = ViewModelFactories.notes(repo, settingsRepo, applicationContext))
            val settingsVm: SettingsViewModel = viewModel(factory = ViewModelFactories.settings(settingsRepo))
            val detailVm: NoteDetailViewModel = viewModel(factory = ViewModelFactories.detail(repo, settingsRepo, applicationContext))
            val settings by settingsVm.state.collectAsState()
            val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            NoteShadeTheme(settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoteShadeAppRoot(notesVm, detailVm, settingsVm, openNoteId = openNoteId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openNoteId = intent.noteIdToOpen()
    }
}

private fun Intent?.noteIdToOpen(): Long = this?.getLongExtra("open_note_id", 0L) ?: 0L
