package com.f1.quiket.composeapp.quiz.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.quiz.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.composeapp.quiz.domain.model.QuizOption
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizQuestion
import com.f1.quiket.composeapp.quiz.domain.model.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.domain.model.QuizSession
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import com.f1.quiket.composeapp.quiz.domain.model.defaultOptions
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.domain.model.matchesAnswerValue
import com.f1.quiket.composeapp.quiz.toQuizNetworkAwareMessage
import com.f1.quiket.composeapp.quiz.withQuizNetworkRetryGuide
import com.f1.quiket.composeapp.util.generateUuid

internal class QuizPlayStateHolder(
    private val quizPlayUseCases: QuizPlayUseCases,
) {
    var state by mutableStateOf<QuizPlayUiState>(QuizPlayUiState.Loading)
        private set

    suspend fun loadQuiz(
        launchConfig: QuizPlayLaunchConfig,
        onSessionExpired: () -> Unit,
    ) {
        state = QuizPlayUiState.Loading
        runCatching {
            val quizSession = quizPlayUseCases.getQuizSession(
                quizSessionId = launchConfig.quizSessionId,
            )
            val clientSessionId = launchConfig.clientSessionId?.takeIf { it.isNotBlank() }
                ?: launchConfig.playSessionId?.takeIf { it.isNotBlank() }
                ?: generateUuid()
            val playSession = if (launchConfig.playSessionId.isNullOrBlank()) {
                quizPlayUseCases.startQuizPlaySession(
                    quizSessionId = quizSession.id,
                    clientSessionId = clientSessionId,
                    playType = launchConfig.playType,
                )
            } else {
                null
            }
            val resolvedQuizSession = playSession?.quizSession ?: quizSession
            val sortedQuestions = resolvedQuizSession.questions
                .sortedBy { question -> question.displayOrder }
            QuizPlayUiState.Ready(
                quizSession = resolvedQuizSession,
                clientSessionId = playSession?.clientSessionId ?: clientSessionId,
                playSessionId = playSession?.playSessionId ?: launchConfig.playSessionId.orEmpty(),
                playType = playSession?.playType?.takeIf { it != QuizPlayType.Unknown }
                    ?: launchConfig.playType,
                playMode = launchConfig.playMode,
                timerEnabled = launchConfig.timerEnabled,
                timerScope = launchConfig.timerScope,
                timerSeconds = launchConfig.timerSeconds,
                remainingTotalSeconds = launchConfig.initialTotalRemainingSeconds(),
                remainingSecondsByQuestionId = sortedQuestions.initialRemainingSeconds(
                    timerEnabled = launchConfig.timerEnabled,
                    timerScope = launchConfig.timerScope,
                    timerSeconds = launchConfig.timerSeconds,
                ),
                questions = sortedQuestions,
            )
        }.onSuccess { nextState ->
            state = nextState
        }.onFailure { error ->
            if (error is QuizPlayException && error.isUnauthorized) {
                onSessionExpired()
            } else {
                state = QuizPlayUiState.Error(
                    message = error.toQuizNetworkAwareMessage(
                        fallback = QuizPlayLoadFailureMessage,
                    ),
                )
            }
        }
    }

    fun updateReady(transform: (QuizPlayUiState.Ready) -> QuizPlayUiState.Ready) {
        val ready = state as? QuizPlayUiState.Ready ?: return
        state = transform(ready)
    }

    fun tickTimer() {
        updateReady { ready -> ready.tickTimer() }
    }

    suspend fun submit(onSessionExpired: () -> Unit): String? {
        val ready = state as? QuizPlayUiState.Ready ?: return null
        if (ready.isSubmitting) return null

        state = ready.copy(isSubmitting = true, isSubmitConfirmVisible = false, errorMessage = null)

        return runCatching {
            quizPlayUseCases.submitQuizResult(
                request = ready.toSubmitRequest(),
            )
        }.fold(
            onSuccess = { result -> result.resultId ?: result.playSessionId },
            onFailure = { error ->
                if (error is QuizPlayException && error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    updateReady {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.toQuizNetworkAwareMessage(
                                fallback = "결과를 제출하지 못했습니다.",
                                timeoutMessage = "결과를 제출하지 못했습니다.".withQuizNetworkRetryGuide(),
                            ),
                        )
                    }
                }
                null
            },
        )
    }
}

internal sealed interface QuizPlayUiState {
    data object Loading : QuizPlayUiState

    data class Error(
        val message: String,
    ) : QuizPlayUiState

    data class Ready(
        val quizSession: QuizSession,
        val clientSessionId: String,
        val playSessionId: String,
        val playType: QuizPlayType,
        val playMode: QuizPlayMode,
        val timerEnabled: Boolean,
        val timerScope: QuizTimerScope?,
        val timerSeconds: Int?,
        val remainingTotalSeconds: Int?,
        val remainingSecondsByQuestionId: Map<String, Int>,
        val questions: List<QuizQuestion>,
        val currentQuestionIndex: Int = 0,
        val selectedOptionIds: Map<String, String> = emptyMap(),
        val checkedQuestionIds: Set<String> = emptySet(),
        val bookmarkedQuestionIds: Set<String> = emptySet(),
        val isQuestionListVisible: Boolean = false,
        val isSubmitConfirmVisible: Boolean = false,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
    ) : QuizPlayUiState {
        val currentQuestion: QuizQuestion?
            get() = questions.getOrNull(currentQuestionIndex)

        val selectedOptionId: String?
            get() = currentQuestion?.let { question -> selectedOptionIds[question.id] }

        val isOneByOneMode: Boolean
            get() = playMode == QuizPlayMode.OneByOne

        val isLastQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex == questions.lastIndex

        val isCurrentQuestionChecked: Boolean
            get() = currentQuestion?.id?.let(checkedQuestionIds::contains) == true

        val isCurrentQuestionBookmarked: Boolean
            get() = currentQuestion?.id?.let(bookmarkedQuestionIds::contains) == true

        val solvedQuestionCount: Int
            get() = questions.count { question -> question.id in selectedOptionIds }

        val unsolvedQuestionCount: Int
            get() = questions.size - solvedQuestionCount

        val unsolvedQuestions: List<QuizQuestion>
            get() = questions.filter { question -> question.id !in selectedOptionIds }

        val bookmarkedQuestionCount: Int
            get() = questions.count { question -> question.id in bookmarkedQuestionIds }

        val bookmarkedQuestions: List<QuizQuestion>
            get() = questions.filter { question -> question.id in bookmarkedQuestionIds }

        fun questionNumber(question: QuizQuestion): Int {
            val index = questions.indexOfFirst { it.id == question.id }
            return question.displayOrder.takeIf { it > 0 } ?: (index + 1).coerceAtLeast(1)
        }

        val currentRemainingSeconds: Int?
            get() {
                if (!timerEnabled || timerScope != QuizTimerScope.PerQuestion) return null
                val questionId = currentQuestion?.id ?: return null
                return remainingSecondsByQuestionId[questionId] ?: timerSeconds
            }

        val timerRemainingSeconds: Int?
            get() {
                if (!timerEnabled) return null
                return when (timerScope) {
                    QuizTimerScope.PerQuestion -> currentRemainingSeconds
                    QuizTimerScope.Total -> remainingTotalSeconds ?: timerSeconds
                    null -> timerSeconds
                }
            }

        val timerText: String?
            get() = timerRemainingSeconds?.formatQuizTimer()

        val shouldTickTimer: Boolean
            get() {
                val remainingSeconds = timerRemainingSeconds
                if (!timerEnabled || timerSeconds == null || remainingSeconds == null) return false
                if (remainingSeconds <= 0) return false
                return when (timerScope) {
                    QuizTimerScope.PerQuestion -> !isCurrentQuestionChecked
                    QuizTimerScope.Total -> true
                    null -> false
                }
            }
    }
}

private fun QuizPlayLaunchConfig.initialTotalRemainingSeconds(): Int? =
    timerSeconds.takeIf {
        timerEnabled && timerScope == QuizTimerScope.Total && timerSeconds != null
    }

private fun List<QuizQuestion>.initialRemainingSeconds(
    timerEnabled: Boolean,
    timerScope: QuizTimerScope?,
    timerSeconds: Int?,
): Map<String, Int> {
    if (!timerEnabled || timerScope != QuizTimerScope.PerQuestion || timerSeconds == null) {
        return emptyMap()
    }
    return associate { question -> question.id to timerSeconds }
}

private fun QuizPlayUiState.Ready.tickTimer(): QuizPlayUiState.Ready {
    if (!timerEnabled || timerSeconds == null) return this
    return when (timerScope) {
        QuizTimerScope.Total -> {
            val nextSeconds = ((remainingTotalSeconds ?: timerSeconds) - 1).coerceAtLeast(0)
            copy(remainingTotalSeconds = nextSeconds)
        }

        QuizTimerScope.PerQuestion -> {
            val questionId = currentQuestion?.id ?: return this
            if (isCurrentQuestionChecked) return this
            val nextSeconds = ((remainingSecondsByQuestionId[questionId] ?: timerSeconds) - 1)
                .coerceAtLeast(0)
            copy(
                remainingSecondsByQuestionId = remainingSecondsByQuestionId + (questionId to nextSeconds),
                checkedQuestionIds = if (nextSeconds == 0 && isOneByOneMode) {
                    checkedQuestionIds + questionId
                } else {
                    checkedQuestionIds
                },
            )
        }

        null -> this
    }
}

private fun QuizPlayUiState.Ready.toSubmitRequest(): QuizResultSubmit =
    QuizResultSubmit(
        clientSessionId = clientSessionId,
        quizSessionId = quizSession.id,
        playType = playType,
        elapsedMs = 0,
        answers = questions.map { question ->
            question.toSubmitItem(
                selectedOptionId = selectedOptionIds[question.id],
                marked = question.id in bookmarkedQuestionIds,
            )
        },
    )

private fun QuizQuestion.toSubmitItem(
    selectedOptionId: String?,
    marked: Boolean,
): QuizAnswerSubmitItem {
    val selectedOption = submitOptions().firstOrNull { option -> option.id == selectedOptionId }
    val selectedValue = if (questionType == ServerQuizType.Ox) {
        selectedOption?.value ?: selectedOption?.content
    } else {
        null
    }
    val submittedOptionId = if (questionType == ServerQuizType.MultipleChoice) {
        selectedOptionId
    } else {
        null
    }

    return QuizAnswerSubmitItem(
        questionId = id,
        selectedOptionId = submittedOptionId,
        selectedValue = selectedValue,
        correctClient = selectedOption?.matchesAnswerValue(answerValue),
        skipped = selectedOption == null,
        marked = marked,
    )
}

private fun QuizQuestion.submitOptions(): List<QuizOption> =
    options
        .sortedBy { option -> option.optionNumber }
        .ifEmpty { questionType.defaultOptions(id) }

private fun Int.formatQuizTimer(): String {
    val normalizedSeconds = coerceAtLeast(0)
    val minutes = normalizedSeconds / 60
    val seconds = normalizedSeconds % 60
    return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
}

private const val QuizPlayLoadFailureMessage = "퀴즈를 불러오지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
