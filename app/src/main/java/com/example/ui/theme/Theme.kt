package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = LightPurple,
    tertiary = HealthyGreen,
    background = BackgroundSoft,
    surface = CardWhite,
    onPrimary = Color.White,
    onSecondary = PrimaryPurple,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryPurple,
    secondary = Color(0xFF372F4C),
    tertiary = HealthyGreen,
    background = DarkBackgroundSoft,
    surface = DarkCardWhite,
    onPrimary = Color.Black,
    onSecondary = DarkPrimaryPurple,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We disable android 12 dynamic color so our custom purple brand design works everywhere
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
