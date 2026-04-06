package com.topenclaw.noteshade.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topenclaw.noteshade.data.NoteSortOption
import com.topenclaw.noteshade.data.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val settings = settingsRepository.settings.collectAsStateWithLifecycle(initialValue = com.topenclaw.noteshade.data.AppSettings())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingRow(
                title = "Dark theme",
                description = "Prefer a dark interface for the whole app.",
                control = {
                    Switch(
                        checked = settings.value.darkTheme,
                        onCheckedChange = { scope.launch { settingsRepository.setDarkTheme(it) } }
                    )
                }
            )
            SettingRow(
                title = "Dynamic color",
                description = "Use Material You colors where Android supports it.",
                control = {
                    Switch(
                        checked = settings.value.dynamicColor,
                        onCheckedChange = { scope.launch { settingsRepository.setDynamicColor(it) } }
                    )
                }
            )
            Text("Default sort", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteSortOption.entries.forEach { option ->
                    TextButton(
                        onClick = { scope.launch { settingsRepository.setDefaultSort(option) } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(option.label) }
                }
            }
            TextButton(onClick = { scope.launch { settingsRepository.setOnboardingSeen(false) } }) {
                Text("Show first-run guidance again")
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, description: String, control: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        control()
    }
}
