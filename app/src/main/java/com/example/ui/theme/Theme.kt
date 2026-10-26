package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MetallicGold,
    onPrimary = ForestGreen,
    primaryContainer = DeepEmerald,
    onPrimaryContainer = SoftGold,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = CardDark,
    onSecondaryContainer = SoftGold,
    background = ForestGreen,
    onBackground = WarmWhite,
    surface = SurfaceDark,
    onSurface = WarmWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = SoftGold,
    outline = BorderDark,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = Color.White,
    primaryContainer = SoftGold.copy(alpha = 0.3f),
    onPrimaryContainer = DeepEmerald,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = Ivory,
    onSecondaryContainer = DeepEmerald,
    background = WarmWhite,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = Ivory,
    onSurfaceVariant = DeepEmerald,
    outline = BorderLight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MuslimQuranTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
