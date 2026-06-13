package com.finnvek.anchor.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AnchorSpacing(
    val tiny: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val helpHeroMinHeight: Dp = 96.dp,
    val helpNeedCardMinHeight: Dp = 72.dp,
)

val LocalAnchorSpacing = staticCompositionLocalOf { AnchorSpacing() }
