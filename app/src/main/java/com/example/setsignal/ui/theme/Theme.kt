package com.example.setsignal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.wear.compose.material3.MaterialTheme

/**
 * Global UI scale. Slightly below 1 so that more content fits on compact watch screens and
 * emulators without having to shrink individual components.
 */
private const val UI_SCALE = 0.9f

@Composable
fun SetSignalTheme(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density.density * UI_SCALE,
            fontScale = density.fontScale,
        ),
    ) {
        MaterialTheme(
            colorScheme = SetSignalColorScheme,
            typography = SetSignalTypography,
            shapes = SetSignalShapes,
            content = content,
        )
    }
}
