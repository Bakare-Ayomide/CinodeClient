package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JellyfinCyan,
    onPrimary = Color.White,
    primaryContainer = JellyfinSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = JellyfinPurple,
    onSecondary = Color.White,
    tertiary = JellyfinAccentGlow,
    background = JellyfinBackground,
    onBackground = TextPrimary,
    surface = JellyfinSurface,
    onSurface = TextPrimary,
    surfaceVariant = JellyfinSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted
)

@Composable
fun CinodeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun JellyfinTheme(
    content: @Composable () -> Unit
) {
    CinodeTheme(content = content)
}
