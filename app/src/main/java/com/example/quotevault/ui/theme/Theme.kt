package com.example.quotevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    error = Error,
    errorContainer = ErrorContainer
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = Secondary,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    error = Error,
    errorContainer = ErrorContainer,
    outline = DividerDark
)

// Shape definitions with rounded corners
private val QuotesShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun QuoteVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeOption: ThemeOption = ThemeOption.INDIGO,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = themeOption.primaryDark,
            secondary = themeOption.secondaryDark,
        )
    } else {
        LightColorScheme.copy(
            primary = themeOption.primaryLight,
            secondary = themeOption.secondaryLight,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuotesTypography,
        shapes = QuotesShapes,
        content = content
    )
}