package com.topenclaw.noteshade.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topenclaw.noteshade.data.NoteEntity
import com.topenclaw.noteshade.data.NoteRepository
import com.topenclaw.noteshade.data.NoteSortOption
import com.topenclaw.noteshade.data.SettingsRepository
import com.topenclaw.noteshade.ui.components.formatTimestamp
import com.topenclaw.noteshade.ui.components.searchAndSort
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    defaultSort: NoteSortOption,
    showOnboarding: Boolean,
    onDismissOnboarding: () -> Unit,
    onCreateNote: (Long) -> Unit,
    onOpenNote: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val notes by noteRepository.observeNotes().collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(defaultSort) }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val visibleNotes = remember(notes, query, showArchived, sort) {
        notes.searchAndSort(query = query, archived = showArchived, sort = sort)
    }

    LaunchedEffect(defaultSort) { sort = defaultSort }

    if (showOnboarding) {
        AlertDialog(
            onDismissRequest = onDismissOnboarding,
            title = { Text("Welcome to NoteShade") },
            text = {
                Text("Create fast notes, keep important ones pinned, archive clutter, and surface selected notes as ongoing notifications.")
            },
            confirmButton = { TextButton(onClick = onDismissOnboarding) { Text("Got it") } },
        )
    }

    pendingDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete note?") },
            text = { Text("This permanently deletes \"${note.title.ifBlank { "Untitled note" }}\".") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        noteRepository.delete(note.id)
                        snackbarHostState.showSnackbar("Note deleted")
                    }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (showArchived) "Archived notes" else "NoteShade") },
                actions = {
                    Box {
                        TextButton(onClick = { sortMenuOpen = true }) { Text(sort.label) }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            NoteSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sort = option
                                        sortMenuOpen = false
                                        scope.launch { settingsRepository.setDefaultSort(option) }
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    val newId = noteRepository.createBlankNote()
                    onCreateNote(newId)
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "New note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text(if (showArchived) "Search archived notes" else "Search notes") },
                singleLine = true,
                keyboardActions = KeyboardActions.Default
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { showArchived = false }, label = { Text("Active") })
                AssistChip(onClick = { showArchived = true }, label = { Text("Archived") })
            }
            if (visibleNotes.isEmpty()) {
                EmptyState(
                    title = if (showArchived) "No archived notes" else "No notes yet",
                    description = if (query.isNotBlank()) {
                        "Nothing matches your search."
                    } else if (showArchived) {
                        "Archived notes stay searchable here."
                    } else {
                        "Tap the plus button to create your first note."
                    }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(visibleNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onOpen = { onOpenNote(note.id) },
                            onTogglePinned = { scope.launch { noteRepository.togglePinned(note.id) } },
                            onToggleArchive = { scope.launch { noteRepository.toggleArchived(note.id) } },
                            onToggleNotification = { scope.launch { noteRepository.toggleNotification(note.id) } },
                            onDelete = { pendingDelete = note }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onOpen: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleNotification: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = note.title.ifBlank { "Untitled note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = note.body.ifBlank { "No content yet" },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (note.isPinned) {
                    Icon(Icons.Default.PushPin, contentDescription = null)
                }
            }
            Text(
                text = "Updated ${formatTimestamp(note.updatedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePinned) { Icon(Icons.Default.PushPin, contentDescription = "Toggle pin") }
                IconButton(onClick = onToggleArchive) { Icon(Icons.Default.Archive, contentDescription = "Toggle archive") }
                IconButton(onClick = onToggleNotification) {
                    Icon(
                        if (note.showInNotification) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = "Toggle notification"
                    )
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
