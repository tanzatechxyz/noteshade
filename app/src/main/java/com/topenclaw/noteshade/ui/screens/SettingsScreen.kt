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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topenclaw.noteshade.data.SortOrder
import com.topenclaw.noteshade.data.ThemeMode
import com.topenclaw.noteshade.data.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UserSettings,
    onBack: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onSort: (SortOrder) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onAutosave: (Boolean) -> Unit,
    onExport: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    EnumMenuButton("Theme", state.themeMode, ThemeMode.entries.toTypedArray(), onThemeMode, Icons.Default.Palette)
                    EnumMenuButton("Default sort", state.defaultSort, SortOrder.entries.toTypedArray(), onSort, Icons.Default.Sort)
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Behaviour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SettingToggle("Pinned note notifications", "Allow notes marked for the shade to stay visible.", state.notificationsEnabled, onNotifications, Icons.Default.Notifications)
                    SettingToggle("Auto-save while editing", "Saves drafts in place without closing the editor.", state.autoSaveEnabled, onAutosave, Icons.Default.Save)
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Export a JSON snapshot of all notes for safekeeping.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Text("  Export notes")
                    }
                }
            }
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Text("How NoteShade works", fontWeight = FontWeight.SemiBold)
                    }
                    Text("Everything stays on-device. Drafts auto-save locally, notification updates come from local state, and backups export as plain JSON.")
                }
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumMenuButton(label: String, selected: T, values: Array<T>, onChange: (T) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.name.replace('_', ' '))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = { Text(value.name.replace('_', ' ')) }, onClick = { onChange(value); expanded = false })
            }
        }
    }
}

@Composable
private fun SettingToggle(label: String, subtitle: String, checked: Boolean, onChanged: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
