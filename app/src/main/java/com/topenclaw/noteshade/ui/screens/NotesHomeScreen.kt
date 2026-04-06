package com.topenclaw.noteshade.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.topenclaw.noteshade.data.Note
import com.topenclaw.noteshade.data.SortOrder
import com.topenclaw.noteshade.viewmodel.NotesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesHomeScreen(
    state: NotesUiState,
    onQueryChange: (String) -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onCreate: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onTogglePinned: (Note) -> Unit,
    onToggleNotification: (Note) -> Unit,
    onArchive: (Note) -> Unit,
    onUnarchive: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    onDismissFirstRun: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("NoteShade") }, actions = {
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = null) }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(value = state.query, onValueChange = onQueryChange, label = { Text("Search notes") }, modifier = Modifier.fillMaxWidth())
            }
            item { SortMenu(state.selectedSort, onSortSelected) }
            if (state.firstRunVisible) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Welcome to NoteShade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Tech stack: Kotlin + Compose + Room + DataStore + MVVM. Local-first storage keeps notes offline. Notifications can surface selected notes persistently in the shade.")
                            TextButton(onClick = onDismissFirstRun) { Text("Got it") }
                        }
                    }
                }
            }
            if (state.notes.isEmpty()) item { EmptyState("No notes yet", "Create your first note or tweak your search.") }
            items(state.notes, key = { it.id }) { note ->
                NoteRow(note, onOpenNote, onTogglePinned, onToggleNotification, onArchive, onDelete, archived = false)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Archived", style = MaterialTheme.typography.titleMedium)
                    AssistChip(onClick = {}, label = { Text(state.archived.size.toString()) })
                }
            }
            if (state.archived.isEmpty()) item { EmptyState("Archive is empty", "Archived notes land here instead of disappearing.") }
            items(state.archived, key = { "archived-${it.id}" }) { note ->
                NoteRow(note, onOpenNote, onTogglePinned, onToggleNotification, onUnarchive, onDelete, archived = true)
            }
        }
    }
}

@Composable
private fun SortMenu(selected: SortOrder, onSelected: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("Sort: ${selected.name.replace('_', ' ')}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOrder.entries.forEach {
                DropdownMenuItem(text = { Text(it.name.replace('_', ' ')) }, onClick = { onSelected(it); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteRow(
    note: Note,
    onOpenNote: (Long) -> Unit,
    onTogglePinned: (Note) -> Unit,
    onToggleNotification: (Note) -> Unit,
    onArchiveOrUnarchive: (Note) -> Unit,
    onDelete: (Note) -> Unit,
    archived: Boolean
) {
    var showDelete by remember { mutableStateOf(false) }
    val cardColors = if (note.isPinned) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onOpenNote(note.id) }, onLongClick = { onTogglePinned(note) }),
        colors = cardColors
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold)
                        if (note.isPinned) {
                            AssistChip(onClick = { onTogglePinned(note) }, label = { Text("Pinned") })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(note.body.ifBlank { "No content yet" }, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
            Divider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onTogglePinned(note) }) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onToggleNotification(note) }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (note.showInNotification) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onArchiveOrUnarchive(note) }) { Icon(Icons.Default.Archive, contentDescription = null) }
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, contentDescription = null) }
            }
        }
    }
    if (showDelete) {
        AlertDialog(onDismissRequest = { showDelete = false }, confirmButton = {
            TextButton(onClick = { onDelete(note); showDelete = false }) { Text("Delete") }
        }, dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }, title = { Text("Delete note?") }, text = { Text(if (archived) "This archived note will be removed permanently." else "This note will be removed permanently.") })
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body)
        }
    }
}
