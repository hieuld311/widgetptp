package com.ivi.widgetptp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WidgetColorScheme = darkColorScheme(
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun WidgetPtPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WidgetColorScheme,
        content = content,
    )
}
