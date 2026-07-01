package com.example.lumalogapp.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            val useDarkSystemBarIcons = !darkTheme

            insetsController.isAppearanceLightStatusBars = useDarkSystemBarIcons
            insetsController.isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
