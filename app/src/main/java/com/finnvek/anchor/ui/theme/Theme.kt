package com.finnvek.anchor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AnchorLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF365E56),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8EDE7),
        onPrimaryContainer = Color(0xFF0D1F1B),
        secondary = Color(0xFF5C5A72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE5DFF9),
        onSecondaryContainer = Color(0xFF1B182A),
        tertiary = Color(0xFF76546A),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8EE),
        onTertiaryContainer = Color(0xFF2D1224),
        background = Color(0xFFFBFDF8),
        onBackground = Color(0xFF191C1A),
        surface = Color(0xFFFBFDF8),
        onSurface = Color(0xFF191C1A),
        surfaceVariant = Color(0xFFDCE5DF),
        onSurfaceVariant = Color(0xFF404944),
        outline = Color(0xFF707973),
    )

private val AnchorDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFBBD1C9),
        onPrimary = Color(0xFF213A34),
        primaryContainer = Color(0xFF2E514A),
        onPrimaryContainer = Color(0xFFD8EDE7),
        secondary = Color(0xFFC9C3DD),
        onSecondary = Color(0xFF2D2A42),
        secondaryContainer = Color(0xFF444159),
        onSecondaryContainer = Color(0xFFE5DFF9),
        tertiary = Color(0xFFE4BCD4),
        onTertiary = Color(0xFF45263A),
        tertiaryContainer = Color(0xFF5C3D51),
        onTertiaryContainer = Color(0xFFFFD8EE),
        background = Color(0xFF111412),
        onBackground = Color(0xFFE2E4DF),
        surface = Color(0xFF111412),
        onSurface = Color(0xFFE2E4DF),
        surfaceVariant = Color(0xFF404944),
        onSurfaceVariant = Color(0xFFC0C9C3),
        outline = Color(0xFF8A938D),
    )

val AnchorCalmLightColorScheme =
    lightColorScheme(
        primary = Color(0xFF486071),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD2E5F2),
        onPrimaryContainer = Color(0xFF0A1D29),
        secondary = Color(0xFF705B4F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFBDCCA),
        onSecondaryContainer = Color(0xFF29180F),
        tertiary = Color(0xFF4E654B),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD0EBCB),
        onTertiaryContainer = Color(0xFF0D200C),
        background = Color(0xFFFBFCF7),
        onBackground = Color(0xFF1A1C18),
        surface = Color(0xFFFBFCF7),
        onSurface = Color(0xFF1A1C18),
        surfaceVariant = Color(0xFFE0E4DA),
        onSurfaceVariant = Color(0xFF43483F),
        outline = Color(0xFF74796F),
    )

private val AnchorCalmDarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFB6C9D7),
        onPrimary = Color(0xFF203340),
        primaryContainer = Color(0xFF304B5B),
        onPrimaryContainer = Color(0xFFD2E5F2),
        secondary = Color(0xFFDEC0AF),
        onSecondary = Color(0xFF3F2C22),
        secondaryContainer = Color(0xFF564237),
        onSecondaryContainer = Color(0xFFFBDCCA),
        tertiary = Color(0xFFB5CEB0),
        onTertiary = Color(0xFF20351E),
        tertiaryContainer = Color(0xFF374C34),
        onTertiaryContainer = Color(0xFFD0EBCB),
        background = Color(0xFF121410),
        onBackground = Color(0xFFE3E4DD),
        surface = Color(0xFF121410),
        onSurface = Color(0xFFE3E4DD),
        surfaceVariant = Color(0xFF43483F),
        onSurfaceVariant = Color(0xFFC3C8BD),
        outline = Color(0xFF8D9387),
    )

@Composable
fun AnchorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    calm: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            calm && darkTheme -> AnchorCalmDarkColorScheme
            calm -> AnchorCalmLightColorScheme
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> AnchorDarkColorScheme
            else -> AnchorLightColorScheme
        }

    CompositionLocalProvider(
        LocalAnchorSpacing provides AnchorSpacing(),
        LocalAnchorMotion provides AnchorMotion.Standard,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AnchorTypography,
            shapes = AnchorShapes.Material,
            content = content,
        )
    }
}
