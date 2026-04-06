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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
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
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EnumMenuButton("Theme", state.themeMode, ThemeMode.entries.toTypedArray(), onThemeMode)
            EnumMenuButton("Default sort", state.defaultSort, SortOrder.entries.toTypedArray(), onSort)
            SettingToggle("Pinned note notifications", state.notificationsEnabled, onNotifications)
            SettingToggle("Auto-save while editing", state.autoSaveEnabled, onAutosave)
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) { Text("Export notes as JSON backup") }
            Text("Storage: Room database on-device, offline-first. Notifications update from local state. Backup export writes a JSON snapshot.")
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumMenuButton(label: String, selected: T, values: Array<T>, onChange: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label)
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
private fun SettingToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
