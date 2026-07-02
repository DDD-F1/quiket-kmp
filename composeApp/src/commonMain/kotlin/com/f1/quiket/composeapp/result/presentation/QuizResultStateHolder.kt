package com.f1.quiket.composeapp.result.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.domain.model.toLaunchConfig
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.domain.model.QuizResultException
import com.f1.quiket.composeapp.result.domain.usecase.QuizResultUseCases
import com.f1.quiket.composeapp.util.generateUuid

internal class QuizResultStateHolder(
    private val quizResultUseCases: QuizResultUseCases,
    private val quizPlayUseCases: QuizPlayUseCases,
) {
    var state by mutableStateOf<QuizResultUiState>(QuizResultUiState.Loading)
        private set

    var isRetrying by mutableStateOf(false)
        private set

    var retryMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadResult(
        resultId: String,
        onSessionExpired: () -> Unit,
    ) {
        state = QuizResultUiState.Loading
        isRetrying = false
        retryMessage = null
        runCatching {
            quizResultUseCases.getQuizResult(resultId = resultId)
        }.onSuccess { result ->
            state = QuizResultUiState.Success(result)
        }.onFailure { error ->
            if (error is QuizResultException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = QuizResultUiState.Error(error.toUserFacingMessage("결과를 불러오지 못했습니다."))
            }
        }
    }

    suspend fun retry(
        wrongOnly: Boolean,
        onSessionExpired: () -> Unit,
    ): QuizPlayLaunchConfig? {
        val result = (state as? QuizResultUiState.Success)?.result ?: return null
        if (isRetrying) return null

        val targetResultId = result.resultId ?: result.playSessionId
        if (targetResultId.isBlank()) {
            retryMessage = "다시 풀 결과 정보를 찾을 수 없어요."
            return null
        }
        val wrongCount = result.retryAvailable?.wrongCount ?: result.wrongCount
        if (wrongOnly && wrongCount <= 0) {
            retryMessage = "다시 풀 오답이 없어요."
            return null
        }
        if (!wrongOnly && result.totalCount <= 0) {
            retryMessage = "전체 다시 풀기를 시작할 수 없어요."
            return null
        }

        isRetrying = true
        retryMessage = "다시 풀기를 준비 중이에요."

        return runCatching {
            val clientSessionId = generateUuid()
            if (wrongOnly) {
                quizPlayUseCases.retryWrongQuestions(
                    resultId = targetResultId,
                    clientSessionId = clientSessionId,
                )
            } else {
                quizPlayUseCases.retryAllQuestions(
                    resultId = targetResultId,
                    clientSessionId = clientSessionId,
                )
            }
        }.fold(
            onSuccess = { playSession ->
                isRetrying = false
                retryMessage = null
                playSession.toLaunchConfig()
            },
            onFailure = { error ->
                isRetrying = false
                if (error is QuizPlayException && error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    retryMessage = error.toUserFacingMessage("다시 풀기를 시작하지 못했습니다.")
                }
                null
            },
        )
    }
}

internal sealed interface QuizResultUiState {
    data object Loading : QuizResultUiState
    data class Success(val result: QuizResult) : QuizResultUiState
    data class Error(val message: String) : QuizResultUiState
}
