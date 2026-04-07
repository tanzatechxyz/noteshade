package com.topenclaw.noteshade.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.topenclaw.noteshade.data.Note
import com.topenclaw.noteshade.data.SortOrder
import com.topenclaw.noteshade.viewmodel.NotesUiState
import java.text.DateFormat
import java.util.Date

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
            SmallTopAppBar(
                title = { Text("NoteShade") },
                actions = {
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "Open settings") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, contentDescription = "Create note") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            label = { Text("Search notes") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            SortMenu(state.selectedSort, onSortSelected)
                            AssistChip(onClick = {}, label = { Text("${state.notes.size} active") })
                            AssistChip(onClick = {}, label = { Text("${state.archived.size} archived") })
                        }
                    }
                }
            }
            if (state.firstRunVisible) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Welcome to NoteShade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Pin important notes, keep selected ones in the notification shade, and let drafts auto-save locally while you work.")
                            FilledTonalButton(onClick = onDismissFirstRun) { Text("Got it") }
                        }
                    }
                }
            }
            item {
                SectionHeader("Notes", "Tap to open, long-press to pin.")
            }
            if (state.notes.isEmpty()) item { EmptyState("No notes yet", "Create a note to start building your quick-access stack.") }
            items(state.notes, key = { it.id }) { note ->
                NoteRow(note, onOpenNote, onTogglePinned, onToggleNotification, onArchive, onDelete, archived = false)
            }
            item {
                SectionHeader("Archive", "Completed or parked notes live here.", trailing = { AssistChip(onClick = {}, label = { Text(state.archived.size.toString()) }) })
            }
            if (state.archived.isEmpty()) item { EmptyState("Archive is empty", "Archived notes stay out of the way until you need them again.") }
            items(state.archived, key = { "archived-${it.id}" }) { note ->
                NoteRow(note, onOpenNote, onTogglePinned, onToggleNotification, onUnarchive, onDelete, archived = true)
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun SortMenu(selected: SortOrder, onSelected: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("Sort: ${selected.name.replace('_', ' ')}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOrder.entries.forEach {
                DropdownMenuItem(text = { Text(it.name.replace('_', ' ')) }, onClick = { onSelected(it); expanded = false })
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
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
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { onOpenNote(note.id) }, onLongClick = { onTogglePinned(note) }),
        colors = cardColors
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        if (note.isPinned) AssistChip(onClick = { onTogglePinned(note) }, label = { Text("Pinned") })
                        if (note.showInNotification) AssistChip(onClick = { onToggleNotification(note) }, label = { Text("Shade") })
                    }
                    Text(note.body.ifBlank { "No content yet" }, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Updated ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(note.updatedAt))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ActionIconButton(note.isPinned, Icons.Default.PushPin, "Pin") { onTogglePinned(note) }
                ActionIconButton(note.showInNotification, Icons.Default.Notifications, "Toggle notification") { onToggleNotification(note) }
                IconButton(onClick = { onArchiveOrUnarchive(note) }) { Icon(Icons.Default.Archive, contentDescription = if (archived) "Restore note" else "Archive note") }
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete note") }
            }
        }
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            confirmButton = {
                TextButton(onClick = { onDelete(note); showDelete = false }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } },
            title = { Text("Delete note?") },
            text = { Text(if (archived) "This archived note will be removed permanently." else "This note will be removed permanently.") }
        )
    }
}

@Composable
private fun ActionIconButton(active: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Icon(icon, contentDescription = label, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
