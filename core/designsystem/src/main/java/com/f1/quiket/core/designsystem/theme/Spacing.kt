package com.f1.quiket.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class QuiketSpacing(
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
)

internal val LocalQuiketSpacing = staticCompositionLocalOf { QuiketSpacing() }

val quiketSpacing: QuiketSpacing
    @Composable
    get() = LocalQuiketSpacing.current
