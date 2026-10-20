package com.example.setsignal.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun SetSignalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SetSignalColorScheme,
        typography = SetSignalTypography,
        shapes = SetSignalShapes,
        content = content,
    )
}
