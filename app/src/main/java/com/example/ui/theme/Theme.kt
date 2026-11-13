package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = Color(0xFF041829),
    primaryContainer = Color(0xFF132B45),
    onPrimaryContainer = Color(0xFFC7E3FF),
    secondary = StudioPurple,
    onSecondary = Color(0xFF280B4A),
    secondaryContainer = Color(0xFF381B5E),
    onSecondaryContainer = Color(0xFFECCBFF),
    tertiary = StudioGreen,
    onTertiary = Color(0xFF002208),
    tertiaryContainer = Color(0xFF13451E),
    onTertiaryContainer = Color(0xFFC7FFD1),
    background = StudioBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = StudioSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = StudioBorderDark,
    outlineVariant = Color(0xFF21262D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0969DA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4FF),
    onPrimaryContainer = Color(0xFF052B58),
    secondary = Color(0xFF8250DF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1E5FF),
    onSecondaryContainer = Color(0xFF33096E),
    tertiary = Color(0xFF1A7F37),
    onTertiary = Color.White,
    background = StudioBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = StudioSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioCardLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = StudioBorderLight
)

@Composable
fun NatnaelCodeTheme(
    darkTheme: Boolean = true, // Dark mode default as specified
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
