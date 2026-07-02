package com.f1.quiket.feature.review.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.f1.quiket.feature.review.presentation.ReviewRoute

fun NavGraphBuilder.reviewGraph() {
    composable(route = ReviewDestination.route) {
        ReviewRoute()
    }
}
