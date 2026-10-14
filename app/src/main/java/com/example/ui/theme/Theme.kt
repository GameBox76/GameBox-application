package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF2E006A),
    secondaryContainer = Color(0xFF4C0099),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = NeonPink,
    onTertiary = Color(0xFF5E002B),
    background = GameDarkBackground,
    onBackground = TextPrimary,
    surface = GameDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = GameDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceDim = Color(0xFF090C14),
    surfaceBright = Color(0xFF1F2639),
    surfaceContainerLowest = Color(0xFF070A11),
    surfaceContainerLow = Color(0xFF101522),
    surfaceContainer = Color(0xFF161C2C),
    surfaceContainerHigh = Color(0xFF1E253A),
    surfaceContainerHighest = Color(0xFF262F47),
    outline = GameDarkBorder,
    outlineVariant = Color(0xFF232B40)
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyanDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F8FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = Color(0xFF2E006A),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = GameLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = GameLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceDim = Color(0xFFE2E8F0),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = GameLightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun GameBoxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
