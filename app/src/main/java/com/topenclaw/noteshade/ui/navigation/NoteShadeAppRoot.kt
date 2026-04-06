package com.topenclaw.noteshade.ui.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.topenclaw.noteshade.ui.screens.NoteEditorScreen
import com.topenclaw.noteshade.ui.screens.NotesHomeScreen
import com.topenclaw.noteshade.ui.screens.SettingsScreen
import com.topenclaw.noteshade.viewmodel.NoteDetailViewModel
import com.topenclaw.noteshade.viewmodel.NotesViewModel
import com.topenclaw.noteshade.viewmodel.SettingsViewModel

@Composable
fun NoteShadeAppRoot(
    notesVm: NotesViewModel,
    detailVm: NoteDetailViewModel,
    settingsVm: SettingsViewModel,
    initialOpenNoteId: Long
) {
    val nav = rememberNavController()
    val notesState by notesVm.state.collectAsState()
    val detailState by detailVm.state.collectAsState()
    val settingsState by settingsVm.state.collectAsState()
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { detailVm.exportAll(it) { } }
        }
    }

    LaunchedEffect(initialOpenNoteId) {
        if (initialOpenNoteId > 0) nav.navigate("editor/$initialOpenNoteId")
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            NotesHomeScreen(
                state = notesState,
                onQueryChange = notesVm::updateQuery,
                onSortSelected = notesVm::updateSort,
                onCreate = { nav.navigate("editor/0") },
                onOpenSettings = { nav.navigate("settings") },
                onOpenNote = { nav.navigate("editor/$it") },
                onTogglePinned = notesVm::togglePinned,
                onToggleNotification = notesVm::toggleNotification,
                onArchive = notesVm::archive,
                onUnarchive = notesVm::unarchive,
                onDelete = notesVm::delete,
                onDismissFirstRun = notesVm::dismissFirstRun
            )
        }
        composable("editor/{noteId}", arguments = listOf(navArgument("noteId") { type = NavType.LongType })) { backStack ->
            val noteId = backStack.arguments?.getLong("noteId") ?: 0L
            NoteEditorScreen(
                state = detailState,
                noteId = noteId,
                onLoad = detailVm::load,
                onBack = { nav.popBackStack() },
                onTitleChange = detailVm::updateTitle,
                onBodyChange = detailVm::updateBody,
                onTogglePinned = detailVm::togglePinned,
                onToggleNotification = detailVm::toggleNotification,
                onSave = { detailVm.save { nav.popBackStack() } },
                onArchive = { detailVm.archive { nav.popBackStack() } },
                onClearError = detailVm::clearError
            )
        }
        composable("settings") {
            SettingsScreen(
                state = settingsState,
                onBack = { nav.popBackStack() },
                onThemeMode = settingsVm::setThemeMode,
                onSort = settingsVm::setDefaultSort,
                onNotifications = settingsVm::setNotificationsEnabled,
                onAutosave = settingsVm::setAutoSaveEnabled,
                onExport = { exportLauncher.launch("noteshade-backup.json") }
            )
        }
    }
}
