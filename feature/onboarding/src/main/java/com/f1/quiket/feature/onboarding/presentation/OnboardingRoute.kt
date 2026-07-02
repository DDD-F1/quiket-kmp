package com.f1.quiket.feature.onboarding.presentation

import androidx.compose.runtime.Composable

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
) {
    OnboardingScreen(
        onComplete = onComplete,
        onSkip = onComplete,
    )
}
