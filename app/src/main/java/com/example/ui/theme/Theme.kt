package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PureWhite = Color(0xFFFFFFFF)
val OffWhiteSurface = Color(0xFFF8F9FA)
val SearchBarFill = Color(0xFFF1F3F7)
val TextBlack = Color(0xFF111827)
val TextMuted = Color(0xFF6B7280)
val BorderLight = Color(0xFFE5E7EB)

private val OlinamLightColorScheme = lightColorScheme(
    primary = OlinamPrimary,
    onPrimary = Color.White,
    primaryContainer = OlinamPrimaryContainer,
    onPrimaryContainer = OlinamOnPrimaryContainer,
    secondary = OlinamSecondary,
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = SearchBarFill,
    onSurface = TextBlack,
    onSurfaceVariant = TextMuted,
    outline = BorderLight,
    outlineVariant = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force pure light theme matching design screenshot
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OlinamLightColorScheme,
        typography = Typography,
        content = content
    )
}
