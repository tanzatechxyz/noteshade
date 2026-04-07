package com.topenclaw.noteshade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.topenclaw.noteshade.viewmodel.NoteEditorState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    noteId: Long,
    quickCapture: Boolean,
    onLoad: (Long?) -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTogglePinned: () -> Unit,
    onToggleNotification: () -> Unit,
    onAutoSave: () -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val editorNoteId = state.noteId.takeIf { it > 0 } ?: noteId
    val isNewNote = editorNoteId == 0L
    val quickCaptureMode = quickCapture && isNewNote
    val titleFocusRequester = remember { FocusRequester() }
    val bodyFocusRequester = remember { FocusRequester() }

    LaunchedEffect(noteId) { onLoad(noteId.takeIf { it > 0 }) }
    LaunchedEffect(state.title, state.body, state.isPinned, state.showInNotification, state.autoSaveEnabled, state.hasLoaded) {
        if (!state.hasLoaded || state.saved || !state.autoSaveEnabled) return@LaunchedEffect
        delay(1500)
        onAutoSave()
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            onClearError()
        }
    }
    LaunchedEffect(quickCaptureMode) {
        if (quickCaptureMode) {
            bodyFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when {
                                quickCaptureMode -> "Quick Capture"
                                isNewNote -> "New note"
                                else -> "Edit note"
                            }
                        )
                        Text(
                            if (state.saved) "Saved locally" else "Saving draft automatically",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (!isNewNote) {
                        IconButton(onClick = onArchive) { Icon(Icons.Default.Archive, contentDescription = "Archive note") }
                    }
                    IconButton(onClick = onSave) { Icon(Icons.Default.Save, contentDescription = "Save note") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (quickCaptureMode) "Capture first, organize later" else if (isNewNote) "Quick capture" else "Note details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (quickCaptureMode) {
                        OutlinedTextField(
                            value = state.body,
                            onValueChange = onBodyChange,
                            label = { Text("Note") },
                            placeholder = { Text("Jot it down…") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 280.dp)
                                .focusRequester(bodyFocusRequester),
                            minLines = 8,
                            maxLines = 12,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { titleFocusRequester.requestFocus() })
                        )
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            label = { Text("Title (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(titleFocusRequester),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.body,
                            onValueChange = onBodyChange,
                            label = { Text("Body") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 240.dp),
                            minLines = 6,
                            maxLines = 10
                        )
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Visibility", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SettingRow(
                        icon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        title = "Pin in list",
                        subtitle = "Keep this note near the top.",
                        checked = state.isPinned,
                        onCheckedChange = { onTogglePinned() }
                    )
                    SettingRow(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        title = "Show in notification shade",
                        subtitle = "Enabled by default for fast access.",
                        checked = state.showInNotification,
                        onCheckedChange = { onToggleNotification() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
