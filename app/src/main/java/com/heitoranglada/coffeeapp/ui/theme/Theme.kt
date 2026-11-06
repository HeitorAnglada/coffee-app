package com.heitoranglada.coffeeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Coffee = Color(0xFF6F4E37)
private val CoffeeLight = Color(0xFFD7B899)
private val Crema = Color(0xFFFFF8F0)
private val Roast = Color(0xFF3B2A20)

private val LightColors = lightColorScheme(
    primary = Coffee,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1DFCF),
    onPrimaryContainer = Roast,
    secondary = Color(0xFF8A6A4F),
    secondaryContainer = Color(0xFFF5E6D8),
    onSecondaryContainer = Roast,
    tertiary = Color(0xFF4F7A6A),
    background = Crema,
    surface = Crema,
    surfaceContainer = Color(0xFFF7ECE2),
    surfaceContainerHigh = Color(0xFFF3E6DA),
)

private val DarkColors = darkColorScheme(
    primary = CoffeeLight,
    onPrimary = Roast,
    primaryContainer = Color(0xFF5A3F2C),
    onPrimaryContainer = Color(0xFFF1DFCF),
    secondary = Color(0xFFD9C0A8),
    secondaryContainer = Color(0xFF4A3a2E),
    tertiary = Color(0xFF9CC7B5),
    background = Color(0xFF1C1612),
    surface = Color(0xFF1C1612),
    surfaceContainer = Color(0xFF261E19),
    surfaceContainerHigh = Color(0xFF30271F),
)

@Composable
fun CoffeeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
