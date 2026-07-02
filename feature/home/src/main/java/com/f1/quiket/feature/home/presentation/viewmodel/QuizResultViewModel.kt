package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlaySession
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val quizPlayRepository: QuizPlayRepository,
) : MviViewModel<QuizResultState, QuizResultIntent, QuizResultEffect>(
    initialState = QuizResultState(),
) {
    override fun handleIntent(intent: QuizResultIntent) {
        when (intent) {
            is QuizResultIntent.Load -> load(intent.resultId)
            QuizResultIntent.RetryAll -> retryAll()
            QuizResultIntent.RetryWrong -> retryWrong()
        }
    }

    private fun load(resultId: String) {
        if (resultId.isBlank()) {
            updateState { copy(errorMessage = "결과를 불러올 수 없어요.") }
            return
        }

        launch {
            updateState {
                copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            when (val result = quizPlayRepository.getQuizResult(resultId)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            result = result.data,
                        )
                    }
                }



                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun retryAll() {
        val result = currentState.result ?: return
        if (result.totalCount <= 0) {
            launch { sendEffect(QuizResultEffect.ShowMessage("전체 다시 풀기를 시작할 수 없어요.")) }
            return
        }
        retry(
            resultId = result.resultId ?: result.playSessionId,
            retryCall = { resultId, request ->
                quizPlayRepository.retryAllQuestions(resultId, request)
            },
        )
    }

    private fun retryWrong() {
        val result = currentState.result ?: return
        val wrongCount = result.retryAvailable?.wrongCount ?: result.wrongCount
        if (wrongCount <= 0) {
            launch { sendEffect(QuizResultEffect.ShowMessage("다시 풀 오답이 없어요.")) }
            return
        }
        retry(
            resultId = result.resultId ?: result.playSessionId,
            retryCall = { resultId, request ->
                quizPlayRepository.retryWrongQuestions(resultId, request)
            },
        )
    }

    private fun retry(
        resultId: String,
        retryCall: suspend (String, QuizRetry) -> NetworkResult<QuizPlaySession>,
    ) {
        if (currentState.isRetrying) {
            launch { sendEffect(QuizResultEffect.ShowMessage("다시 풀기를 준비 중이에요.")) }
            return
        }
        if (resultId.isBlank()) return

        launch {
            updateState { copy(isRetrying = true, errorMessage = null) }
            val retryRequest = QuizRetry(clientSessionId = UUID.randomUUID().toString())
            try {
                when (val result = retryCall(resultId, retryRequest)) {
                    is NetworkResult.Success -> {
                        val retrySession = result.data
                        val retryQuizSessionId = retrySession.quizSession?.id
                            ?: retrySession.quizSessionId
                        updateState { copy(isRetrying = false) }
                        if (retryQuizSessionId.isBlank()) {
                            sendEffect(QuizResultEffect.ShowMessage("다시 풀 퀴즈 정보를 찾을 수 없어요."))
                        } else {
                            sendEffect(
                                QuizResultEffect.NavigateToRetry(
                                    QuizRetryPlayConfig(
                                        quizSessionId = retryQuizSessionId,
                                        clientSessionId = retrySession.clientSessionId,
                                        playSessionId = retrySession.playSessionId,
                                        playType = retrySession.playType,
                                        playMode = retrySession.quizSession?.playMode
                                            ?: QuizPlayMode.AllAtOnce,
                                        timerEnabled = retrySession.quizSession?.timerEnabled
                                            ?: false,
                                        timerScope = retrySession.quizSession.toTimerScope(),
                                        timerSeconds = retrySession.quizSession?.timerSeconds,
                                    ),
                                ),
                            )
                        }
                    }

                    is NetworkResult.Failure -> {
                        updateState { copy(isRetrying = false) }
                        sendEffect(QuizResultEffect.ShowMessage(result.message))
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                updateState { copy(isRetrying = false) }
                sendEffect(QuizResultEffect.ShowMessage("다시 풀기를 시작할 수 없어요."))
            }
        }
    }

    private fun QuizSession?.toTimerScope(): QuizTimerScope? =
        QuizTimerScope.entries.firstOrNull { scope -> scope.wireValue == this?.timerScope }
}
