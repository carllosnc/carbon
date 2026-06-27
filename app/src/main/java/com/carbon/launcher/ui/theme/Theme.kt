package com.carbon.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CarbonColors = darkColorScheme(
    primary = Grayscale.g19,
    onPrimary = Grayscale.g02,
    primaryContainer = Grayscale.g05,
    onPrimaryContainer = Grayscale.g18,
    secondary = Grayscale.g13,
    onSecondary = Grayscale.g02,
    secondaryContainer = Grayscale.g04,
    onSecondaryContainer = Grayscale.g17,
    tertiary = Grayscale.g11,
    onTertiary = Grayscale.g01,
    tertiaryContainer = Grayscale.g03,
    onTertiaryContainer = Grayscale.g16,
    error = Grayscale.g20,
    onError = Grayscale.g00,
    errorContainer = Grayscale.g06,
    onErrorContainer = Grayscale.g18,
    background = Grayscale.g00,
    onBackground = Grayscale.g19,
    surface = Grayscale.g01,
    onSurface = Grayscale.g19,
    surfaceVariant = Grayscale.g03,
    onSurfaceVariant = Grayscale.g15,
    surfaceTint = Grayscale.g18,
    outline = Grayscale.g07,
    outlineVariant = Grayscale.g05,
    scrim = Grayscale.g00,
    inverseSurface = Grayscale.g18,
    inverseOnSurface = Grayscale.g02,
    inversePrimary = Grayscale.g06,
)

@Composable
fun CarbonTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CarbonColors,
        content = content,
    )
}
