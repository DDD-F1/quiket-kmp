package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.Question
import com.f1.quiket.feature.home.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class QuizPlayAllViewModel @Inject constructor(
    private val quizPlayRepository: QuizPlayRepository,
) :
    MviViewModel<QuizPlayAllState, QuizPlayAllIntent, QuizPlayAllEffect>(
        initialState = QuizPlayAllState(
            questions = mockQuestions,
        ),
    ) {
    private var timerJob: Job? = null

    override fun handleIntent(intent: QuizPlayAllIntent) {
        when (intent) {
            is QuizPlayAllIntent.ConfigurePlay -> configurePlay(intent)
            is QuizPlayAllIntent.LoadQuizSession -> loadQuizSession(intent)
            QuizPlayAllIntent.RetryLoadQuizSession -> retryLoadQuizSession()
            is QuizPlayAllIntent.SelectOption -> selectOption(intent.optionId)
            QuizPlayAllIntent.MovePrevious -> movePrevious()
            QuizPlayAllIntent.MoveNext -> moveNext()
            QuizPlayAllIntent.CheckCurrentAnswer -> checkCurrentAnswer()
            QuizPlayAllIntent.ToggleBookmark -> toggleBookmark()
            QuizPlayAllIntent.OpenQuestionList -> setQuestionListVisible(true)
            QuizPlayAllIntent.CloseQuestionList -> setQuestionListVisible(false)
            is QuizPlayAllIntent.SelectQuestion -> selectQuestion(intent.questionIndex)
            QuizPlayAllIntent.OpenSubmitConfirm -> setSubmitConfirmVisible(true)
            QuizPlayAllIntent.CloseSubmitConfirm -> setSubmitConfirmVisible(false)
            QuizPlayAllIntent.Submit -> submitQuizResult()
            QuizPlayAllIntent.DismissTutorial -> dismissTutorial()
        }
    }

    private fun configurePlay(intent: QuizPlayAllIntent.ConfigurePlay) {
        updateState {
            val configuredQuestions = questions.withTimerText(
                timerEnabled = intent.timerEnabled,
                timerSeconds = intent.timerSeconds,
            )
            copy(
                playMode = intent.playMode,
                timerEnabled = intent.timerEnabled,
                timerScope = intent.timerScope,
                timerSeconds = intent.timerSeconds,
                hasStartConfig = true,
                questions = configuredQuestions,
                remainingTotalSeconds = intent.initialTotalRemainingSeconds(),
                remainingSecondsByQuestionId = configuredQuestions.initialRemainingSeconds(
                    timerEnabled = intent.timerEnabled,
                    timerScope = intent.timerScope,
                    timerSeconds = intent.timerSeconds,
                ),
            )
        }
        startTimerIfNeeded()
    }

    private fun loadQuizSession(intent: QuizPlayAllIntent.LoadQuizSession) {
        val quizSessionId = intent.quizSessionId
        if (
            currentState.quizSessionId == quizSessionId &&
            currentState.clientSessionId == intent.clientSessionId &&
            currentState.playSessionId == intent.playSessionId &&
            currentState.questions.isNotEmpty() &&
            currentState.errorMessage == null
        ) {
            return
        }

        stopTimer()
        launch {
            updateState {
                copy(
                    quizSessionId = quizSessionId,
                    clientSessionId = intent.clientSessionId,
                    playSessionId = intent.playSessionId,
                    playType = intent.playType,
                    isLoading = true,
                    isSubmitting = false,
                    errorMessage = null,
                    questions = emptyList(),
                    currentQuestionIndex = 0,
                    selectedOptionIds = emptyMap(),
                    checkedQuestionIds = emptySet(),
                    remainingTotalSeconds = null,
                    remainingSecondsByQuestionId = emptyMap(),
                    bookmarkedQuestionIds = emptySet(),
                    isQuestionListVisible = false,
                    isSubmitConfirmVisible = false,
                )
            }

            when (val sessionResult = quizPlayRepository.getQuizSession(quizSessionId)) {
                is NetworkResult.Success -> {
                    val quizSession = sessionResult.data
                    val timerEnabled = if (currentState.hasStartConfig) {
                        currentState.timerEnabled
                    } else {
                        quizSession.timerEnabled
                    }
                    val timerSeconds = if (currentState.hasStartConfig) {
                        currentState.timerSeconds
                    } else {
                        quizSession.timerSeconds
                    }
                    val timerScope = if (currentState.hasStartConfig) {
                        currentState.timerScope
                    } else {
                        quizSession.timerScope.toQuizTimerScope()
                    }
                    val questions = quizSession.toQuizPlayAllQuestions(
                        timerEnabled = timerEnabled,
                        timerSeconds = timerSeconds,
                    )
                    updateState {
                        copy(
                            questions = questions,
                            playMode = if (hasStartConfig) playMode else quizSession.playMode,
                            timerEnabled = timerEnabled,
                            timerScope = timerScope,
                            timerSeconds = timerSeconds,
                            checkedQuestionIds = emptySet(),
                            remainingTotalSeconds = initialTotalRemainingSeconds(
                                timerEnabled = timerEnabled,
                                timerScope = timerScope,
                                timerSeconds = timerSeconds,
                            ),
                            remainingSecondsByQuestionId = questions.initialRemainingSeconds(
                                timerEnabled = timerEnabled,
                                timerScope = timerScope,
                                timerSeconds = timerSeconds,
                            ),
                            isLoading = false,
                        )
                    }
                    startTimerIfNeeded()
                    if (intent.clientSessionId.isNullOrBlank() || intent.playSessionId.isNullOrBlank()) {
                        startPlaySession(quizSession)
                    }
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            errorMessage = sessionResult.message.toQuizLoadErrorMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun retryLoadQuizSession() {
        val quizSessionId = currentState.quizSessionId ?: return
        loadQuizSession(QuizPlayAllIntent.LoadQuizSession(quizSessionId))
    }

    private fun String.toQuizLoadErrorMessage(): String =
        if (equals("timeout", ignoreCase = true) || contains("timeout", ignoreCase = true)) {
            "퀴즈를 불러오지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요."
        } else {
            this
        }

    private suspend fun startPlaySession(quizSession: QuizSession) {
        val clientSessionId = UUID.randomUUID().toString()
        val request = QuizPlayStart(
            clientSessionId = clientSessionId,
            playType = QuizPlayType.First,
            questionShuffled = false,
            optionShuffled = false,
        )

        when (val result = quizPlayRepository.startQuizPlaySession(quizSession.id, request)) {
            is NetworkResult.Success -> {
                updateState {
                    copy(
                        clientSessionId = result.data.clientSessionId,
                        playSessionId = result.data.playSessionId,
                        playType = QuizPlayType.First,
                        errorMessage = null,
                    )
                }
            }

            is NetworkResult.Failure -> {
                updateState {
                    copy(errorMessage = result.message)
                }
            }
        }
    }

    private fun selectOption(optionId: String) {
        updateState {
            val questionId = currentQuestion?.id ?: return@updateState this
            if (isOneByOneMode && checkedQuestionIds.contains(questionId)) {
                return@updateState this
            }
            copy(
                selectedOptionIds = selectedOptionIds + (questionId to optionId),
            )
        }
    }

    private fun movePrevious() {
        updateState {
            copy(
                currentQuestionIndex = (currentQuestionIndex - 1).coerceAtLeast(0),
            )
        }
        startTimerIfNeeded()
    }

    private fun moveNext() {
        updateState {
            copy(
                currentQuestionIndex = (currentQuestionIndex + 1).coerceAtMost(
                    (totalQuestionCount - 1).coerceAtLeast(0),
                ),
            )
        }
        startTimerIfNeeded()
    }

    private fun checkCurrentAnswer(force: Boolean = false) {
        val state = currentState
        val questionId = state.currentQuestion?.id ?: return
        if (!force && state.selectedOptionIds[questionId] == null) return

        updateState {
            copy(checkedQuestionIds = checkedQuestionIds + questionId)
        }
        stopTimer()
    }

    private fun toggleBookmark() {
        updateState {
            val questionId = currentQuestion?.id ?: return@updateState this
            val nextBookmarks = if (bookmarkedQuestionIds.contains(questionId)) {
                bookmarkedQuestionIds - questionId
            } else {
                bookmarkedQuestionIds + questionId
            }
            copy(bookmarkedQuestionIds = nextBookmarks)
        }
    }

    private fun setQuestionListVisible(visible: Boolean) {
        updateState {
            copy(isQuestionListVisible = visible)
        }
    }

    private fun selectQuestion(questionIndex: Int) {
        updateState {
            copy(
                currentQuestionIndex = questionIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue = (totalQuestionCount - 1).coerceAtLeast(0),
                ),
                isQuestionListVisible = false,
            )
        }
        startTimerIfNeeded()
    }

    private fun setSubmitConfirmVisible(visible: Boolean) {
        updateState {
            copy(isSubmitConfirmVisible = visible)
        }
    }

    private fun submitQuizResult() {
        stopTimer()
        val state = currentState
        val quizSessionId = state.quizSessionId
        val clientSessionId = state.clientSessionId

        if (quizSessionId.isNullOrBlank() || clientSessionId.isNullOrBlank()) {
            updateState {
                copy(
                    isSubmitConfirmVisible = false,
                    errorMessage = "풀이 세션을 먼저 시작할 수 없어요.",
                )
            }
            return
        }

        launch {
            updateState {
                copy(
                    isSubmitting = true,
                    errorMessage = null,
                )
            }

            val request = QuizResultSubmit(
                clientSessionId = clientSessionId,
                quizSessionId = quizSessionId,
                playType = state.playType,
                elapsedMs = 0,
                questionShuffled = false,
                optionShuffled = false,
                answers = state.questions.map { question ->
                    question.toAnswerSubmitItem(
                        selectedOptionId = state.selectedOptionIds[question.id],
                        marked = state.bookmarkedQuestionIds.contains(question.id),
                    )
                },
            )

            when (val result = quizPlayRepository.submitQuizResult(request)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            isSubmitting = false,
                            isSubmitConfirmVisible = false,
                            errorMessage = null,
                        )
                    }
                    sendEffect(
                        QuizPlayAllEffect.NavigateToResult(
                            result.data.resultId ?: result.data.playSessionId,
                        ),
                    )
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            isSubmitting = false,
                            isSubmitConfirmVisible = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun dismissTutorial() {
        updateState {
            copy(showTutorial = false)
        }
    }

    private fun startTimerIfNeeded() {
        stopTimer()

        val state = currentState
        if (!state.timerEnabled) {
            return
        }

        if (state.timerScope == QuizTimerScope.Total) {
            startTotalTimer()
            return
        }

        if (!state.isOneByOneMode || state.timerScope != QuizTimerScope.PerQuestion) {
            return
        }

        val question = state.currentQuestion ?: return
        if (state.checkedQuestionIds.contains(question.id)) return

        val initialSeconds = state.timerSeconds ?: return
        val remainingSeconds = state.remainingSecondsByQuestionId[question.id] ?: initialSeconds
        if (remainingSeconds <= 0) {
            checkCurrentAnswer(force = true)
            return
        }

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)

                val latestState = currentState
                val latestQuestion = latestState.currentQuestion ?: break
                if (
                    latestQuestion.id != question.id ||
                    latestState.checkedQuestionIds.contains(latestQuestion.id) ||
                    !latestState.isOneByOneMode ||
                    !latestState.timerEnabled
                ) {
                    break
                }

                val currentRemainingSeconds = latestState.remainingSecondsByQuestionId[latestQuestion.id]
                    ?: latestState.timerSeconds
                    ?: break
                val nextRemainingSeconds = (currentRemainingSeconds - 1).coerceAtLeast(0)

                updateState {
                    copy(
                        remainingSecondsByQuestionId = remainingSecondsByQuestionId +
                            (latestQuestion.id to nextRemainingSeconds),
                    )
                }

                if (nextRemainingSeconds == 0) {
                    checkCurrentAnswer(force = true)
                    break
                }
            }
        }
    }

    private fun startTotalTimer() {
        val state = currentState
        val initialSeconds = state.timerSeconds?.coerceAtLeast(0) ?: return
        val remainingSeconds = state.remainingTotalSeconds ?: initialSeconds
        if (remainingSeconds <= 0) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_TICK_MS)

                val latestState = currentState
                if (!latestState.timerEnabled || latestState.timerScope != QuizTimerScope.Total) {
                    break
                }

                val currentRemainingSeconds = latestState.remainingTotalSeconds
                    ?: latestState.timerSeconds
                    ?: break
                val nextRemainingSeconds = (currentRemainingSeconds - 1).coerceAtLeast(0)

                updateState {
                    copy(remainingTotalSeconds = nextRemainingSeconds)
                }

                if (nextRemainingSeconds == 0) {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }

    private companion object {
        const val TIMER_TICK_MS = 1_000L

        val mockQuestions = listOf(
            QuizPlayAllQuestion(
                id = "question-1",
                number = 1,
                body = "SQL에서 두 테이블의 공통 컬럼을 기준으로 행을 결합하는 연산은 무엇인가요?",
                timerText = "00:30",
                answerValue = "question-1-option-2",
                options = listOf(
                    option("question-1", 1, "SELECT"),
                    option("question-1", 2, "JOIN"),
                    option("question-1", 3, "WHERE"),
                    option("question-1", 4, "GROUP BY"),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-2",
                number = 2,
                body = "다음 중 '좋은 데이터 모델의 요소'로 가장 거리가 먼 것은?",
                timerText = "00:30",
                answerValue = "question-2-option-2",
                options = listOf(
                    option("question-2", 1, "완전성"),
                    option("question-2", 2, "중복성"),
                    option("question-2", 3, "업무 규칙 반영"),
                    option("question-2", 4, "확장성"),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-3",
                number = 3,
                body = "다음 중 스키마(Schema)에 대한 설명으로 가장 적절한 것은?",
                timerText = "00:29",
                answerValue = "question-3-option-1",
                options = listOf(
                    option("question-3", 1, "데이터베이스의 논리적 구조와 제약조건을 정의한 것이다."),
                    option("question-3", 2, "데이터를 일시적으로 저장하는 메모리 공간이다."),
                    option("question-3", 3, "SQL 실행 결과를 화면에 출력하는 별도 프로그램이다."),
                    option("question-3", 4, "테이블 간 물리적 저장 순서를 의미한다."),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-4",
                number = 4,
                body = "정규화의 주된 목적으로 가장 알맞은 것은?",
                timerText = "00:28",
                answerValue = "question-4-option-1",
                options = listOf(
                    option("question-4", 1, "데이터 중복과 이상 현상을 줄인다."),
                    option("question-4", 2, "항상 조회 속도를 최대로 높인다."),
                    option("question-4", 3, "모든 테이블을 하나로 합친다."),
                    option("question-4", 4, "인덱스를 자동 생성한다."),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-5",
                number = 5,
                body = "기본키에 대한 설명으로 옳은 것은?",
                timerText = "00:27",
                answerValue = "question-5-option-2",
                options = listOf(
                    option("question-5", 1, "NULL 값을 여러 개 가질 수 있다."),
                    option("question-5", 2, "각 행을 고유하게 식별한다."),
                    option("question-5", 3, "반드시 외래키와 같은 이름이어야 한다."),
                    option("question-5", 4, "한 테이블에 무제한으로 지정할 수 있다."),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-6",
                number = 6,
                body = "외래키가 주로 표현하는 관계는 무엇인가요?",
                timerText = "00:27",
                answerValue = "question-6-option-1",
                options = listOf(
                    option("question-6", 1, "테이블 간 참조 관계"),
                    option("question-6", 2, "쿼리 실행 순서"),
                    option("question-6", 3, "파일 저장 위치"),
                    option("question-6", 4, "인덱스 정렬 방식"),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-7",
                number = 7,
                body = "다음 중 SQL 집계 함수가 아닌 것은?",
                timerText = "00:26",
                answerValue = "question-7-option-4",
                options = listOf(
                    option("question-7", 1, "COUNT"),
                    option("question-7", 2, "SUM"),
                    option("question-7", 3, "AVG"),
                    option("question-7", 4, "ORDER"),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-8",
                number = 8,
                body = "WHERE 절의 역할로 가장 적절한 것은?",
                timerText = "00:25",
                answerValue = "question-8-option-1",
                options = listOf(
                    option("question-8", 1, "조회할 행의 조건을 지정한다."),
                    option("question-8", 2, "출력 컬럼의 별칭만 지정한다."),
                    option("question-8", 3, "테이블을 생성한다."),
                    option("question-8", 4, "트랜잭션을 종료한다."),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-9",
                number = 9,
                body = "GROUP BY와 함께 자주 사용되는 절은 무엇인가요?",
                timerText = "00:24",
                answerValue = "question-9-option-1",
                options = listOf(
                    option("question-9", 1, "HAVING"),
                    option("question-9", 2, "VALUES"),
                    option("question-9", 3, "COMMIT"),
                    option("question-9", 4, "ROLLBACK"),
                ),
            ),
            QuizPlayAllQuestion(
                id = "question-10",
                number = 10,
                body = "다음 중 관계의 분류 기준으로 가장 적절한 것은?",
                timerText = "00:23",
                answerValue = "question-10-option-2",
                options = listOf(
                    option("question-10", 1, "관계명과 관계 차수"),
                    option("question-10", 2, "존재에 의한 관계와 행위에 의한 관계"),
                    option("question-10", 3, "컬럼 길이와 데이터 타입"),
                    option("question-10", 4, "저장 위치와 파일 크기"),
                ),
            ),
        )

        fun option(questionId: String, number: Int, text: String): QuizPlayAllOption =
            QuizPlayAllOption(
                id = "$questionId-option-$number",
                number = number,
                text = text,
            )
    }
}

private fun QuizSession.toQuizPlayAllQuestions(
    timerEnabled: Boolean,
    timerSeconds: Int?,
): List<QuizPlayAllQuestion> {
    val timerText = if (timerEnabled) {
        timerSeconds?.formatTimerText().orEmpty().ifBlank { "00:00" }
    } else {
        "00:00"
    }

    return questions
        .sortedBy { question -> question.displayOrder }
        .mapIndexed { index, question ->
            question.toQuizPlayAllQuestion(
                number = index + 1,
                timerText = timerText,
            )
        }
}

private fun Question.toQuizPlayAllQuestion(
    number: Int,
    timerText: String,
): QuizPlayAllQuestion = QuizPlayAllQuestion(
    id = id,
    number = number,
    body = body,
    timerText = timerText,
    questionType = questionType,
    answerValue = answer.answerValue,
    correctExplanation = correctExplanation,
    incorrectExplanation = incorrectExplanation,
    options = options
        .sortedBy { option -> option.optionNumber }
        .map { option ->
            QuizPlayAllOption(
                id = option.id,
                number = option.optionNumber,
                text = option.content,
            )
        }
        .ifEmpty { questionType.defaultOptions(id) },
)

private fun Int.formatTimerText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun String?.toQuizTimerScope(): QuizTimerScope? =
    QuizTimerScope.entries.firstOrNull { scope -> scope.wireValue == this }

private fun QuizPlayAllIntent.ConfigurePlay.initialTotalRemainingSeconds(): Int? =
    initialTotalRemainingSeconds(
        timerEnabled = timerEnabled,
        timerScope = timerScope,
        timerSeconds = timerSeconds,
    )

private fun initialTotalRemainingSeconds(
    timerEnabled: Boolean,
    timerScope: QuizTimerScope?,
    timerSeconds: Int?,
): Int? =
    if (timerEnabled && timerScope == QuizTimerScope.Total) {
        timerSeconds?.coerceAtLeast(0)
    } else {
        null
    }

private fun List<QuizPlayAllQuestion>.withTimerText(
    timerEnabled: Boolean,
    timerSeconds: Int?,
): List<QuizPlayAllQuestion> {
    val timerText = if (timerEnabled) {
        timerSeconds?.formatTimerText().orEmpty().ifBlank { "00:00" }
    } else {
        "00:00"
    }
    return map { question -> question.copy(timerText = timerText) }
}

private fun List<QuizPlayAllQuestion>.initialRemainingSeconds(
    timerEnabled: Boolean,
    timerScope: QuizTimerScope?,
    timerSeconds: Int?,
): Map<String, Int> {
    val seconds = timerSeconds?.coerceAtLeast(0)
    if (!timerEnabled || timerScope != QuizTimerScope.PerQuestion || seconds == null) {
        return emptyMap()
    }
    return associate { question -> question.id to seconds }
}

private fun ServerQuizType.defaultOptions(questionId: String): List<QuizPlayAllOption> =
    when (this) {
        ServerQuizType.Ox -> listOf(
            QuizPlayAllOption(
                id = "$questionId-ox-o",
                number = 1,
                text = "O",
                value = "O",
            ),
            QuizPlayAllOption(
                id = "$questionId-ox-x",
                number = 2,
                text = "X",
                value = "X",
            ),
        )

        ServerQuizType.MultipleChoice -> emptyList()
    }

private fun QuizPlayAllQuestion.toAnswerSubmitItem(
    selectedOptionId: String?,
    marked: Boolean,
): QuizAnswerSubmitItem {
    val selectedOption = options.firstOrNull { option -> option.id == selectedOptionId }
    val selectedValue = if (questionType == ServerQuizType.Ox) {
        selectedOption?.value ?: selectedOption?.text
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
