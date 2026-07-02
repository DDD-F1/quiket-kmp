package com.f1.quiket.feature.history.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.f1.quiket.feature.history.presentation.HistoryRoute

fun NavGraphBuilder.historyGraph(
    onQuizStartClick: (String) -> Unit,
    onQuizResultClick: (String) -> Unit,
) {
    composable(route = HistoryDestination.route) {
        HistoryRoute(
            onQuizStartClick = onQuizStartClick,
            onQuizResultClick = onQuizResultClick,
        )
    }
}
