package com.example.currencyconverter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Shared light palette used by every screen when light mode is selected; surface/background stay soft. */
private val LightColors = lightColorScheme(
    primary = Color(0xFF176B5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0DF),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF52645D),
    secondaryContainer = Color(0xFFD5E8DE),
    background = Color(0xFFF5F8F5),
    surface = Color(0xFFF5F8F5),
    surfaceVariant = Color(0xFFE2EAE4)
)

/** Shared dark palette; keeping it centralized preserves consistent contrast between screens. */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD5C0),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF005142),
    onPrimaryContainer = Color(0xFFB8F0DF),
    secondary = Color(0xFFB9CCC2),
    secondaryContainer = Color(0xFF394B43)
)

/**
 * Selects the requested or system theme and provides its Material color scheme to [content].
 * Unknown preference values use the operating system setting, which keeps the app resilient to
 * older or manually edited preferences.
 */
@Composable
fun AppTheme(themeMode: String = "system", content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
