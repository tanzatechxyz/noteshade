package com.topenclaw.noteshade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.topenclaw.noteshade.data.ThemeMode

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun NoteShadeTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
