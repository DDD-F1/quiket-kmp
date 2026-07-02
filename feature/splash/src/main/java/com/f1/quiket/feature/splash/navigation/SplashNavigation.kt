package com.f1.quiket.feature.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.f1.quiket.feature.splash.presentation.SplashRoute

fun NavGraphBuilder.splashGraph(
    onDecideNext: () -> Unit,
) {
    composable(route = SplashDestination.route) {
        SplashRoute(onDecideNext = onDecideNext)
    }
}
