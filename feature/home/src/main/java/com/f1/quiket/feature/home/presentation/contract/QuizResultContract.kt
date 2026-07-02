package com.f1.quiket.feature.home.presentation.contract

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.QuizResult

data class QuizResultState(
    val isLoading: Boolean = false,
    val isRetrying: Boolean = false,
    val result: QuizResult? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface QuizResultIntent : UiIntent {
    data class Load(val resultId: String) : QuizResultIntent
    data object RetryAll : QuizResultIntent
    data object RetryWrong : QuizResultIntent
}

data class QuizRetryPlayConfig(
    val quizSessionId: String,
    val clientSessionId: String,
    val playSessionId: String,
    val playType: QuizPlayType,
    val playMode: QuizPlayMode,
    val timerEnabled: Boolean,
    val timerScope: QuizTimerScope?,
    val timerSeconds: Int?,
)

sealed interface QuizResultEffect : UiEffect {
    data class NavigateToRetry(val config: QuizRetryPlayConfig) : QuizResultEffect

    data class ShowMessage(val message: String) : QuizResultEffect
}
