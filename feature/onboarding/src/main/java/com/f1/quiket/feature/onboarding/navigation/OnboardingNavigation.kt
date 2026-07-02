package com.f1.quiket.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.f1.quiket.feature.onboarding.presentation.OnboardingRoute

fun NavGraphBuilder.onboardingGraph(
    onComplete: () -> Unit,
) {
    composable(route = OnboardingDestination.route) {
        OnboardingRoute(onComplete = onComplete)
    }
}
