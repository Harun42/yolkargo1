package com.gogo.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GoGoPrimary,
    onPrimary = Color.White,
    secondary = GoGoSecondary,
    onSecondary = Color.White,
    background = GoGoSurface,
    surface = Color.White,
    onSurface = GoGoSecondary,
    tertiary = GoGoPrimary
)

private val DarkColors = darkColorScheme(
    primary = GoGoPrimary,
    onPrimary = Color.White,
    secondary = GoGoSecondary,
    onSecondary = Color.White,
    background = Color(0xFF09090B),
    surface = Color(0xFF1F1B2E),
    onSurface = Color.White,
    tertiary = GoGoPrimary
)

@Composable
fun GoGoTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
