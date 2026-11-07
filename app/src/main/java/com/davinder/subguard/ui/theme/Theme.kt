package com.davinder.subguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.davinder.subguard.data.AppearancePreferences

// All base colours are OPAQUE (no transparency) so cards/shadows never glitch.
private val BaseLightColors = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFE6ECF5),
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF1F5F9),
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0),
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val BaseDarkColors = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2A3754),
    surfaceContainer = Color(0xFF16203A),
    surfaceContainerHigh = Color(0xFF1B2540),
    surfaceContainerHighest = Color(0xFF243152),
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2E3C5C),
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun SubGuardTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    SideEffect { AppearancePreferences.updateDarkMode(useDarkTheme) }

    val rawAccent = AppearancePreferences.currentAccentState.value
    val primary = resolveAccent(rawAccent, useDarkTheme)
    val onPrimary = primary.readableContent()

    val base = if (useDarkTheme) BaseDarkColors else BaseLightColors

    val colorScheme = base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        secondary = primary.copy(alpha = 0.85f),
        onSecondary = onPrimary,
        onSurface = if (useDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
        onSurfaceVariant = if (useDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SubGuardTypography,
        content = content
    )
}
