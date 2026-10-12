package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color.Black,
    primaryContainer = JarvisCyanDark,
    onPrimaryContainer = JarvisTextPrimary,
    secondary = JarvisNeonGlow,
    onSecondary = Color.Black,
    secondaryContainer = JarvisSurfaceElevated,
    onSecondaryContainer = JarvisTextSecondary,
    tertiary = JarvisCyanBright,
    onTertiary = Color.Black,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCyan.copy(alpha = 0.4f),
    outlineVariant = JarvisCyan.copy(alpha = 0.2f),
    error = JarvisAccentRed,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
