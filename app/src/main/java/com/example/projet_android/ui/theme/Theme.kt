package com.example.projet_android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Mauve,
    secondary = Blue,
    tertiary = Pink,
    background = Base,
    surface = Surface0,
    onPrimary = Base,
    onSecondary = Base,
    onTertiary = Base,
    onBackground = Text,
    onSurface = Text,
    error = Red,
    onError = Base,
    outline = Overlay0,
    surfaceVariant = Surface1,
    onSurfaceVariant = Subtext0
)

// For a TUI feel, we can use the same palette or a slightly lighter variation, 
// but usually TUI is dark. Let's keep a consistent look.
private val LightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Teal,
    tertiary = Rosewater,
    background = Base, // Keeping it dark for that terminal look even in "light" mode or just use Latte palette
    surface = Surface0,
    onPrimary = Base,
    onSecondary = Base,
    onTertiary = Base,
    onBackground = Text,
    onSurface = Text,
    error = Red,
    onError = Base
)

@Composable
fun ProjetandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to maintain the specific terminal/catppuccin aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Force dark for TUI look or use LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
