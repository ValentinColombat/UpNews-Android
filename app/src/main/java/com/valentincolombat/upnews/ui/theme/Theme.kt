package com.valentincolombat.upnews.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UpNewsColorScheme = lightColorScheme(
    primary = UpNewsGreen,
    secondary = UpNewsOrange,
    tertiary = UpNewsBlueMid,
    background = UpNewsBackground,
    surface = UpNewsBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun UpNewsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UpNewsColorScheme,
        typography = Typography,
        content = content
    )
}
