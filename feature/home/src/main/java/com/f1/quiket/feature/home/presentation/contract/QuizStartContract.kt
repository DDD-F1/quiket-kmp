package com.f1.quiket.feature.home.presentation.contract

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class QuizStartState(
    val isLoading: Boolean = false,
    val summary: QuizStartSummaryUiModel? = null,
    val errorMessage: String? = null,
) : UiState

data class QuizStartSummaryUiModel(
    val title: String,
    val quizTypeLabel: String,
    val choiceLabel: String?,
    val questionCountLabel: String,
    val difficultyLabel: String,
    val scopeLabels: List<String>,
)

sealed interface QuizStartIntent : UiIntent {
    data class Load(val quizSessionId: String?) : QuizStartIntent
}

sealed interface QuizStartEffect : UiEffect
