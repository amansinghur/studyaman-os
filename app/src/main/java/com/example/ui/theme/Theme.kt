package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianColorScheme = darkColorScheme(
    primary = AccentLinear,
    onPrimary = Color.White,
    secondary = AccentClickUp,
    onSecondary = Color.White,
    tertiary = AccentTeal,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceGloss,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force Obsidian Premium Dark Theme for the entire Academic OS experience
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        content = content
    )
}
