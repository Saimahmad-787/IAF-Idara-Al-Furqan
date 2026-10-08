package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IafDarkEmerald,
    onPrimary = Color(0xFF00391A),
    primaryContainer = IafDarkEmeraldContainer,
    onPrimaryContainer = Color(0xFFA5F4BF),
    secondary = IafDarkGold,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = IafDarkGoldContainer,
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = Color(0xFF8CD4B0),
    onTertiary = Color(0xFF003822),
    background = IafDarkBackground,
    onBackground = Color(0xFFE2E7E2),
    surface = IafDarkSurface,
    onSurface = Color(0xFFE2E7E2),
    surfaceVariant = IafDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0CCC2),
    outline = IafOutline,
    outlineVariant = Color(0xFF414D45),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = IafEmerald,
    onPrimary = Color.White,
    primaryContainer = IafEmeraldContainer,
    onPrimaryContainer = IafOnEmeraldContainer,
    secondary = IafGold,
    onSecondary = Color.White,
    secondaryContainer = IafGoldContainer,
    onSecondaryContainer = IafOnGoldContainer,
    tertiary = IafEmeraldLight,
    onTertiary = Color.White,
    background = IafBackground,
    onBackground = Color(0xFF171D1A),
    surface = IafSurface,
    onSurface = Color(0xFF171D1A),
    surfaceVariant = IafSurfaceVariant,
    onSurfaceVariant = Color(0xFF414D45),
    outline = IafOutline,
    outlineVariant = IafOutlineVariant,
    error = IafStatusError,
    onError = Color.White,
    errorContainer = IafStatusErrorContainer,
    onErrorContainer = Color(0xFF991B1B)
)

val LocalIsDarkMode = androidx.compose.runtime.compositionLocalOf { false }

@Composable
fun appBackgroundColor(): Color = if (LocalIsDarkMode.current) Color(0xFF101412) else Color(0xFFF7F9F5)

@Composable
fun appCardColor(): Color = if (LocalIsDarkMode.current) Color(0xFF1B241F) else Color.White

@Composable
fun appTextColor(): Color = if (LocalIsDarkMode.current) Color(0xFFF1F5F9) else Color(0xFF1E293B)

@Composable
fun appSubtextColor(): Color = if (LocalIsDarkMode.current) Color(0xFF94A3B8) else Color(0xFF64748B)

@Composable
fun appBorderColor(): Color = if (LocalIsDarkMode.current) Color(0xFF2D3B32) else Color(0xFFE2E8F0)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    androidx.compose.runtime.CompositionLocalProvider(LocalIsDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
