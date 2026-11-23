package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LumenPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LumenDarkCard,
    onPrimaryContainer = LumenCyanSecondary,
    secondary = LumenCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = LumenDarkSurfaceHighlight,
    onSecondaryContainer = LumenCyanLight,
    background = LumenDarkBackground,
    onBackground = LumenTextPrimary,
    surface = LumenDarkSurface,
    onSurface = LumenTextPrimary,
    surfaceVariant = LumenDarkCard,
    onSurfaceVariant = LumenTextSecondary,
    outline = LumenBorder
)

@Composable
fun LumenStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
