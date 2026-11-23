package com.example.smartcafecustomer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GreenWhiteColorScheme = lightColorScheme(
    primary = CafeGreenPrimary,
    onPrimary = CafeWhite,
    primaryContainer = CafeGreenContainer,
    onPrimaryContainer = CafeGreenPrimary,
    secondary = CafeGreenSecondary,
    onSecondary = CafeWhite,
    secondaryContainer = CafeGreenContainer,
    onSecondaryContainer = CafeDarkText,
    tertiary = CafeGreenTertiary,
    onTertiary = CafeWhite,
    background = CafeOffWhite,
    onBackground = CafeDarkText,
    surface = CafeWhite,
    onSurface = CafeDarkText,
    surfaceVariant = CafeGreenContainer,
    onSurfaceVariant = CafeDarkText,
    outline = CafeGreenSecondary
)

@Composable
fun SmartCafeCustomerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GreenWhiteColorScheme,
        typography = Typography,
        content = content
    )
}