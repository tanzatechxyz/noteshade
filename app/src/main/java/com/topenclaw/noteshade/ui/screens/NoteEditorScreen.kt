package com.topenclaw.noteshade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.topenclaw.noteshade.viewmodel.NoteEditorState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    noteId: Long,
    onLoad: (Long?) -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTogglePinned: () -> Unit,
    onToggleNotification: () -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(noteId) { onLoad(noteId.takeIf { it > 0 }) }
    LaunchedEffect(state.saved, state.title, state.body, state.autoSaveEnabled) {
        if (!state.saved && state.autoSaveEnabled) {
            delay(700)
            onSave()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            onClearError()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (noteId == 0L) "New note" else "Edit note") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            }, actions = {
                if (noteId > 0L) {
                    IconButton(onClick = onArchive) { Icon(Icons.Default.Archive, contentDescription = null) }
                }
                IconButton(onClick = onSave) { Icon(Icons.Default.Save, contentDescription = null) }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = state.title, onValueChange = onTitleChange, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.body, onValueChange = onBodyChange, label = { Text("Body") }, modifier = Modifier.fillMaxWidth().weight(1f, false), minLines = 10)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PushPin, contentDescription = null)
                    Text("Pin in list")
                }
                Switch(checked = state.isPinned, onCheckedChange = { onTogglePinned() })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Text("Show in notification shade")
                }
                Switch(checked = state.showInNotification, onCheckedChange = { onToggleNotification() })
            }
            Text(if (state.saved) "All changes saved" else "Unsaved changes", modifier = Modifier.padding(top = 8.dp))
        }
    }
}
