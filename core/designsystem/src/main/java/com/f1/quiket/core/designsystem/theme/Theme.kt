package com.f1.quiket.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val QuiketLightColorScheme = lightColorScheme(
    primary = Brown950,
    secondary = Orange500,
    tertiary = Blue500,
    surface = Gray100,
    background = Brown50,
)

@Composable
fun QuiketTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalQuiketSpacing provides QuiketSpacing()) {
        MaterialTheme(
            colorScheme = QuiketLightColorScheme,
            typography = QuiketTypography,
            content = content
        )
    }
}
