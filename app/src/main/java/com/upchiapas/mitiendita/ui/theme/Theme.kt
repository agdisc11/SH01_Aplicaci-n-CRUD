package com.upchiapas.mitiendita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = Amber40,
    onSecondary = Color.White,
    secondaryContainer = Amber90,
    onSecondaryContainer = Amber10,
    tertiary = Teal40,
    background = Neutral99,
    surface = Neutral99
)

private val DarkColors = darkColorScheme(
    primary = Green80,
    onPrimary = Green10,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = Amber80,
    onSecondary = Amber10,
    secondaryContainer = Amber40,
    onSecondaryContainer = Amber90,
    tertiary = Teal80,
    background = Neutral10,
    surface = Neutral10
)

@Composable
fun MiTienditaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
