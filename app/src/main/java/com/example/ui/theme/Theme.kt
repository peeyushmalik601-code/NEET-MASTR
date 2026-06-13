package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeetTealPrimaryDark,
    secondary = NeetTealSecondaryDark,
    tertiary = NeetMintAccentDark,
    background = NeetSlateDarkBg,
    surface = NeetSlateDarkSurface,
    onPrimary = Color(0xFF0F141C),
    onSecondary = Color(0xFF0F141C),
    onTertiary = Color(0xFF0F141C),
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFECEFF1),
    error = Color(0xFFEF9A9A)
)

private val LightColorScheme = lightColorScheme(
    primary = NeetTealPrimary,
    secondary = NeetTealSecondary,
    tertiary = NeetMintAccent,
    background = NeetBgLight,
    surface = NeetSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = NeetNavyText,
    onSurface = NeetNavyText,
    error = NeetWrongRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to enforce our elegant clinical theme!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
