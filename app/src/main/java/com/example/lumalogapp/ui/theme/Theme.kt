package com.example.lumalogapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LumaGreenDark,
    secondary = LumaGreen,
    tertiary = LumaGreenSoft,
    background = LumaDarkBg,
    surface = LumaDarkSurface,
    surfaceVariant = LumaDarkSurfaceSoft,
    onPrimary = LumaDarkBg,
    onBackground = LumaDarkText,
    onSurface = LumaDarkText,
    onSurfaceVariant = LumaDarkMuted,
    outline = LumaDarkBorder,
)

private val LightColorScheme = lightColorScheme(
    primary = LumaGreen,
    secondary = LumaGreenDark,
    tertiary = LumaGreenSoft,
    background = LumaBg,
    surface = LumaSurface,
    surfaceVariant = LumaSurfaceSoft,
    onPrimary = LumaSurface,
    onBackground = LumaText,
    onSurface = LumaText,
    onSurfaceVariant = LumaMuted,
    outline = LumaBorder,
)

@Composable
fun LumaLogAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
