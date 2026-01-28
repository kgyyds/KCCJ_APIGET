package com.kgapp.kccjUltra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun KccjTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = HackerGreen,
        onPrimary = HackerBlack,
        secondary = HackerCyan,
        onSecondary = HackerBlack,
        background = HackerBlack,
        onBackground = HackerText,
        surface = HackerSurface,
        onSurface = HackerText,
        surfaceVariant = HackerCard,
        onSurfaceVariant = HackerTextSecondary,
        outline = HackerBorder
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = HackerTypography,
        content = content
    )
}
