package com.cashcaddy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cashcaddy.app.data.model.AccentColor
import com.cashcaddy.app.data.model.Appearance

private fun lightScheme(accent: AccentColor): ColorScheme {
    val primary = Color(accent.seedLight)
    val container = primary.copy(alpha = 1f).let {
        Color(
            red = (it.red * 0.25f + 0.75f).coerceIn(0f, 1f),
            green = (it.green * 0.25f + 0.75f).coerceIn(0f, 1f),
            blue = (it.blue * 0.35f + 0.65f).coerceIn(0f, 1f),
        )
    }
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = container,
        onPrimaryContainer = primary,
        secondary = Color(0xFF535F70),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD7E3F8),
        onSecondaryContainer = Color(0xFF101C2B),
        tertiary = Color(0xFF1B7F4E),
        onTertiary = Color.White,
        background = LightBackground,
        onBackground = Color(0xFF1A1C1E),
        surface = LightBackground,
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFE1E3E6),
        onSurfaceVariant = Color(0xFF43474E),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF0F1F3),
        surfaceContainer = LightCard,
        surfaceContainerHigh = Color(0xFFE8EAED),
        surfaceContainerHighest = Color(0xFFE7E9ED),
        outline = Color(0xFF73777F),
        outlineVariant = Color(0xFFC3C6CF),
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF1F0F4),
        inversePrimary = Color(accent.seedDark),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
    )
}

private fun darkScheme(accent: AccentColor): ColorScheme {
    val primary = Color(accent.seedDark)
    val onPrimary = if (primary.luminance() > 0.4f) Color(0xFF002F67) else Color.White
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = Color(0xFF242B35),
        onPrimaryContainer = primary,
        secondary = Color(0xFFBBC7DB),
        onSecondary = Color(0xFF253140),
        secondaryContainer = Color(0xFF3B4858),
        onSecondaryContainer = Color(0xFFD7E3F8),
        tertiary = ComparisonGreen,
        onTertiary = Color(0xFF00391D),
        background = DarkBackground,
        onBackground = Color(0xFFE2E2E6),
        surface = DarkBackground,
        onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C6CF),
        surfaceContainerLowest = Color(0xFF0A0C0E),
        surfaceContainerLow = Color(0xFF171A1E),
        surfaceContainer = DarkCard,
        surfaceContainerHigh = Color(0xFF22262B),
        surfaceContainerHighest = Color(0xFF2C3136),
        outline = Color(0xFF8D9199),
        outlineVariant = Color(0xFF43474E),
        inverseSurface = Color(0xFFE2E2E6),
        inverseOnSurface = Color(0xFF2F3033),
        inversePrimary = Color(accent.seedLight),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
    )
}

@Composable
fun CashCaddyTheme(
    appearance: Appearance = Appearance.System,
    accent: AccentColor = AccentColor.Blue,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (appearance) {
        Appearance.System -> systemDark
        Appearance.Light -> false
        Appearance.Dark -> true
    }
    val scheme = if (dark) darkScheme(accent) else lightScheme(accent)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = CashCaddyTypography,
        content = content,
    )
}