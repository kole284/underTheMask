package com.underthemask.android.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF0B84D),
    onPrimary = Color(0xFF17120A),
    secondary = Color(0xFF64C9A5),
    background = Color(0xFF0B0C0E),
    surface = Color(0xFF17181C),
    surfaceVariant = Color(0xFF23252A),
    error = Color(0xFFFF6B6B),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF765700),
    secondary = Color(0xFF006C51),
    background = Color(0xFFF7F5F0),
    surface = Color.White,
    error = Color(0xFFBA1A1A),
)

@Composable
fun UnderTheMaskTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
