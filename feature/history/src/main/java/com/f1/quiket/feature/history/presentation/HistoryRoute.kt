package com.f1.quiket.feature.history.presentation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.feature.history.domain.model.RecentActivityType

@Composable
fun HistoryRoute(
    onQuizStartClick: (String) -> Unit,
    onQuizResultClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HistoryScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onActivityClick = { activity ->
            val quizResultId = activity.resultId ?: activity.playSessionId
            when {
                quizResultId != null &&
                    activity.activityType == RecentActivityType.QuizCompleted -> {
                    onQuizResultClick(quizResultId)
                }

                activity.quizSessionId != null &&
                    activity.activityType in setOf(
                        RecentActivityType.QuizReady,
                        RecentActivityType.QuizInProgress,
                    ) -> {
                    onQuizStartClick(activity.quizSessionId)
                }
            }
        },
    )
}
