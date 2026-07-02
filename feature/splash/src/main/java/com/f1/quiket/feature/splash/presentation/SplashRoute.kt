package com.f1.quiket.feature.splash.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(
    onDecideNext: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_ANIMATION_DURATION_MILLIS)
        onDecideNext()
    }

    SplashScreen()
}

private const val SPLASH_ANIMATION_DURATION_MILLIS = 5_050L
