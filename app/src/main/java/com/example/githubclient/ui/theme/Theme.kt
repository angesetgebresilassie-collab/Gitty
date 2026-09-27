package com.example.githubclient.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = HubBlue,
    onPrimary = Color_White,
    primaryContainer = Color(0xFFDCE7FF),
    secondary = MergedPurple,
    background = SurfaceLight,
    surface = Color_White,
    surfaceVariant = SurfaceDim,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    error = ClosedRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FA0FF),
    onPrimary = Color(0xFF00204D),
    primaryContainer = HubBlueDark,
    secondary = Color(0xFFC9A6FF),
    background = SurfaceDark,
    surface = SurfaceDarkElevated,
    surfaceVariant = Color(0xFF1E242C),
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = Color(0xFFFF7B72)
)

private val Color_White = androidx.compose.ui.graphics.Color.White

@Composable
fun GitHubClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep brand colors consistent rather than wallpaper-based
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HubTypography,
        shapes = HubShapes,
        content = content
    )
}
