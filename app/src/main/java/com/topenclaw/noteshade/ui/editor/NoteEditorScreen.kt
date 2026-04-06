package com.topenclaw.noteshade.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.ui.components.formatTimestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    noteRepository: NoteRepository,
    onBack: () -> Unit,
) {
    val note by noteRepository.observeNote(noteId).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var title by rememberSaveable(noteId) { mutableStateOf("") }
    var body by rememberSaveable(noteId) { mutableStateOf("") }

    LaunchedEffect(note?.id) {
        note?.let {
            title = it.title
            body = it.body
        }
    }

    LaunchedEffect(noteId, title, body) {
        if (note == null) return@LaunchedEffect
        delay(500)
        if (title != note?.title || body != note?.body) {
            noteRepository.saveNote(noteId, title, body)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(note?.title?.ifBlank { "Untitled note" } ?: "Loading…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { noteRepository.togglePinned(noteId) } }) {
                        Icon(Icons.Default.PushPin, contentDescription = "Toggle pin")
                    }
                    IconButton(onClick = { scope.launch { noteRepository.toggleArchived(noteId) } }) {
                        Icon(Icons.Default.Archive, contentDescription = "Toggle archive")
                    }
                    IconButton(onClick = { scope.launch { noteRepository.toggleNotification(noteId) } }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Toggle notification")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                label = { Text("Write your note") },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 10,
            )
            Text(
                text = note?.let { "Last updated ${formatTimestamp(it.updatedAt)}" } ?: "Preparing note…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Auto-save is on. Changes are stored locally even when you're offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
