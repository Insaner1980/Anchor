package com.finnvek.anchor.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class AnchorMotionScheme(
    val fadeMillis: Int,
    val containerMillis: Int,
)

object AnchorMotion {
    val Standard = AnchorMotionScheme(fadeMillis = 150, containerMillis = 300)
    val Crisis = AnchorMotionScheme(fadeMillis = 0, containerMillis = 0)
}

val LocalAnchorMotion = staticCompositionLocalOf { AnchorMotion.Standard }
