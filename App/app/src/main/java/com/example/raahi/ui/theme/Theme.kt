package com.example.raahi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Material 3 Color Scheme based on Northeast India Palette
private val RaahiLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    primaryContainer = DarkGreen,
    onPrimaryContainer = White,
    secondary = AccentPurple,
    onSecondary = White,
    secondaryContainer = LightPurple, // Lighter variant for secondary container roles
    onSecondaryContainer = DarkPurple, // Darker variant for text/icons on secondaryContainer
    tertiary = AccentPurple, // Can be same as secondary or a different accent
    onTertiary = White,
    tertiaryContainer = LightPurple, // Similar to secondary container logic
    onTertiaryContainer = DarkPurple,
    error = ErrorRed,
    onError = White,
    background = BackgroundCream,
    onBackground = PrimaryTextDarkCharcoal,
    surface = SurfaceWhite,
    onSurface = PrimaryTextDarkCharcoal,
    surfaceVariant = LightGrey, // For slightly different surfaces like outlines or dividers
    onSurfaceVariant = SecondaryTextGrey,
    outline = SecondaryTextGrey
    // InversePrimary, InverseOnPrimary, InverseSurface, InverseOnSurface, SurfaceTint, OutlineVariant, Scrim 
    // can be left to default or customized if specific needs arise.
)

// Placeholder Dark Color Scheme - can be customized further
private val RaahiDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen, // Keep primary vibrant
    onPrimary = Black,      // Adjust for contrast on primary
    primaryContainer = DarkGreen, // Keep container distinct
    onPrimaryContainer = White,
    secondary = AccentPurple,
    onSecondary = Black,
    secondaryContainer = DarkPurple, // Darker container for dark theme
    onSecondaryContainer = LightPurple, // Lighter text/icon for contrast
    tertiary = AccentPurple,
    onTertiary = Black,
    tertiaryContainer = DarkPurple,
    onTertiaryContainer = LightPurple,
    error = ErrorRed, // Standard error color
    onError = White,
    background = Color(0xFF1C1B1F), // Standard M3 dark background
    onBackground = Color(0xFFE6E1E5), // Standard M3 dark onBackground
    surface = Color(0xFF2C2B2F), // Slightly lighter surface for dark theme cards/elements
    onSurface = White, // Ensure text on dark surfaces is light
    surfaceVariant = Color(0xFF49454F), // Standard M3 dark surfaceVariant
    onSurfaceVariant = Color(0xFFCAC4D0), // Standard M3 dark onSurfaceVariant
    outline = SecondaryTextGrey // Use SecondaryTextGrey from light theme for outline
)

@Composable
fun RaahiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic coloring is available on Android S+ but we'll use our custom theme for now.
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RaahiDarkColorScheme
        else -> RaahiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Will be defined in Typography.kt
        content = content
    )
}
