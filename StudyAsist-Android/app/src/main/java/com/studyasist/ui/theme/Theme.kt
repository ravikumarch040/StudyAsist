package com.studyasist.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun StudyAsistTheme(
    appTheme: AppTheme = AppTheme.MINIMAL_LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDynamic = appTheme.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val config = appTheme.config
    val targetScheme = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(context)
        useDynamic -> dynamicLightColorScheme(context)
        config.forceDark -> config.darkScheme
        darkTheme -> config.darkScheme
        else -> config.lightScheme
    }
    val isDark = config.forceDark || darkTheme

    val animSpec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 400)
    val colorScheme = ColorScheme(
        primary = animateColorAsState(targetScheme.primary, animSpec).value,
        onPrimary = animateColorAsState(targetScheme.onPrimary, animSpec).value,
        primaryContainer = animateColorAsState(targetScheme.primaryContainer, animSpec).value,
        onPrimaryContainer = animateColorAsState(targetScheme.onPrimaryContainer, animSpec).value,
        inversePrimary = animateColorAsState(targetScheme.inversePrimary, animSpec).value,
        secondary = animateColorAsState(targetScheme.secondary, animSpec).value,
        onSecondary = animateColorAsState(targetScheme.onSecondary, animSpec).value,
        secondaryContainer = animateColorAsState(targetScheme.secondaryContainer, animSpec).value,
        onSecondaryContainer = animateColorAsState(targetScheme.onSecondaryContainer, animSpec).value,
        tertiary = animateColorAsState(targetScheme.tertiary, animSpec).value,
        onTertiary = animateColorAsState(targetScheme.onTertiary, animSpec).value,
        tertiaryContainer = animateColorAsState(targetScheme.tertiaryContainer, animSpec).value,
        onTertiaryContainer = animateColorAsState(targetScheme.onTertiaryContainer, animSpec).value,
        background = animateColorAsState(targetScheme.background, animSpec).value,
        onBackground = animateColorAsState(targetScheme.onBackground, animSpec).value,
        surface = animateColorAsState(targetScheme.surface, animSpec).value,
        onSurface = animateColorAsState(targetScheme.onSurface, animSpec).value,
        surfaceVariant = animateColorAsState(targetScheme.surfaceVariant, animSpec).value,
        onSurfaceVariant = animateColorAsState(targetScheme.onSurfaceVariant, animSpec).value,
        surfaceTint = animateColorAsState(targetScheme.surfaceTint, animSpec).value,
        inverseSurface = animateColorAsState(targetScheme.inverseSurface, animSpec).value,
        inverseOnSurface = animateColorAsState(targetScheme.inverseOnSurface, animSpec).value,
        error = animateColorAsState(targetScheme.error, animSpec).value,
        onError = animateColorAsState(targetScheme.onError, animSpec).value,
        errorContainer = animateColorAsState(targetScheme.errorContainer, animSpec).value,
        onErrorContainer = animateColorAsState(targetScheme.onErrorContainer, animSpec).value,
        outline = animateColorAsState(targetScheme.outline, animSpec).value,
        outlineVariant = animateColorAsState(targetScheme.outlineVariant, animSpec).value,
        scrim = animateColorAsState(targetScheme.scrim, animSpec).value,
        surfaceBright = animateColorAsState(targetScheme.surfaceBright, animSpec).value,
        surfaceDim = animateColorAsState(targetScheme.surfaceDim, animSpec).value,
        surfaceContainer = animateColorAsState(targetScheme.surfaceContainer, animSpec).value,
        surfaceContainerHigh = animateColorAsState(targetScheme.surfaceContainerHigh, animSpec).value,
        surfaceContainerHighest = animateColorAsState(targetScheme.surfaceContainerHighest, animSpec).value,
        surfaceContainerLow = animateColorAsState(targetScheme.surfaceContainerLow, animSpec).value,
        surfaceContainerLowest = animateColorAsState(targetScheme.surfaceContainerLowest, animSpec).value,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = config.typography,
        shapes = config.shapes,
        content = content
    )
}
