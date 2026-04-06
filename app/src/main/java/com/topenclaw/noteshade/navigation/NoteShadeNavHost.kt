package com.topenclaw.noteshade.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.ui.editor.NoteEditorScreen
import com.topenclaw.noteshade.ui.home.HomeScreen
import com.topenclaw.noteshade.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.StateFlow

private object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val EDITOR = "editor/{noteId}"
    fun editor(noteId: Long) = "editor/$noteId"
}

@Composable
fun NoteShadeNavHost(
    noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    pendingOpenNoteId: StateFlow<Long?>,
    onPendingOpenHandled: () -> Unit,
    onMarkOnboardingSeen: () -> Unit,
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val pendingNoteId by pendingOpenNoteId.collectAsState()
    val settings by settingsRepository.settings.collectAsState(initial = com.topenclaw.noteshade.data.AppSettings())

    LaunchedEffect(pendingNoteId) {
        pendingNoteId?.let {
            navController.navigate(Routes.editor(it))
            onPendingOpenHandled()
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                noteRepository = noteRepository,
                settingsRepository = settingsRepository,
                defaultSort = settings.defaultSort,
                showOnboarding = !settings.onboardingSeen,
                onDismissOnboarding = onMarkOnboardingSeen,
                onCreateNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenNote = { noteId -> navController.navigate(Routes.editor(noteId)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = remember(backStackEntry) { backStackEntry.arguments?.getLong("noteId") ?: 0L }
            NoteEditorScreen(
                noteId = noteId,
                noteRepository = noteRepository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
