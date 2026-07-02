package com.f1.quiket.feature.home.presentation

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.home.domain.model.Question
import com.f1.quiket.feature.home.domain.model.QuestionAnswer
import com.f1.quiket.feature.home.domain.model.QuestionOption
import com.f1.quiket.feature.home.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlaySession
import com.f1.quiket.feature.home.domain.model.QuizPlaySessionStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizResult
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.RewardSummary
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import com.f1.quiket.feature.home.presentation.contract.QuizPlayAllEffect
import com.f1.quiket.feature.home.presentation.contract.QuizPlayAllIntent
import com.f1.quiket.feature.home.presentation.viewmodel.QuizPlayAllViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizPlayAllViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectOption_updatesCurrentQuestionAnswer() {
        val viewModel = viewModel()
        val question = viewModel.state.value.currentQuestion!!
        val option = question.options[1]

        viewModel.onIntent(QuizPlayAllIntent.SelectOption(option.id))

        assertThat(viewModel.state.value.selectedOptionIds[question.id]).isEqualTo(option.id)
    }

    @Test
    fun checkCurrentAnswer_marksCurrentQuestionChecked() {
        val viewModel = viewModel()
        val question = viewModel.state.value.currentQuestion!!
        val option = question.options[1]

        viewModel.onIntent(
            QuizPlayAllIntent.ConfigurePlay(
                playMode = QuizPlayMode.OneByOne,
                timerEnabled = false,
                timerScope = null,
                timerSeconds = null,
            ),
        )
        viewModel.onIntent(QuizPlayAllIntent.SelectOption(option.id))
        viewModel.onIntent(QuizPlayAllIntent.CheckCurrentAnswer)

        assertThat(viewModel.state.value.checkedQuestionIds).contains(question.id)
    }

    @Test
    fun oneByOnePerQuestionTimer_countsDownAndChecksQuestionAtZero() = runTest {
        val viewModel = viewModel()
        val question = viewModel.state.value.currentQuestion!!

        viewModel.onIntent(
            QuizPlayAllIntent.ConfigurePlay(
                playMode = QuizPlayMode.OneByOne,
                timerEnabled = true,
                timerScope = QuizTimerScope.PerQuestion,
                timerSeconds = 2,
            ),
        )
        runCurrent()

        assertThat(viewModel.state.value.currentRemainingSeconds).isEqualTo(2)

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.currentRemainingSeconds).isEqualTo(1)
        assertThat(viewModel.state.value.checkedQuestionIds).doesNotContain(question.id)

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.currentRemainingSeconds).isEqualTo(0)
        assertThat(viewModel.state.value.checkedQuestionIds).contains(question.id)
    }

    @Test
    fun allAtOnceTotalTimer_countsDownAcrossQuestionMovement() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(
            QuizPlayAllIntent.ConfigurePlay(
                playMode = QuizPlayMode.AllAtOnce,
                timerEnabled = true,
                timerScope = QuizTimerScope.Total,
                timerSeconds = 3,
            ),
        )
        runCurrent()

        assertThat(viewModel.state.value.remainingTotalSeconds).isEqualTo(3)

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.remainingTotalSeconds).isEqualTo(2)

        viewModel.onIntent(QuizPlayAllIntent.MoveNext)
        runCurrent()

        assertThat(viewModel.state.value.currentQuestionIndex).isEqualTo(1)

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.remainingTotalSeconds).isEqualTo(1)

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.remainingTotalSeconds).isEqualTo(0)
    }

    @Test
    fun movePreviousAndNext_clampsAtBounds() {
        val viewModel = viewModel()

        viewModel.onIntent(QuizPlayAllIntent.MovePrevious)

        assertThat(viewModel.state.value.currentQuestionIndex).isEqualTo(0)

        repeat(20) {
            viewModel.onIntent(QuizPlayAllIntent.MoveNext)
        }

        assertThat(viewModel.state.value.currentQuestionIndex)
            .isEqualTo(viewModel.state.value.questions.lastIndex)

        viewModel.onIntent(QuizPlayAllIntent.MoveNext)

        assertThat(viewModel.state.value.currentQuestionIndex)
            .isEqualTo(viewModel.state.value.questions.lastIndex)
    }

    @Test
    fun toggleBookmark_addsAndRemovesCurrentQuestion() {
        val viewModel = viewModel()
        val questionId = viewModel.state.value.currentQuestion!!.id

        viewModel.onIntent(QuizPlayAllIntent.ToggleBookmark)

        assertThat(viewModel.state.value.bookmarkedQuestionIds).contains(questionId)

        viewModel.onIntent(QuizPlayAllIntent.ToggleBookmark)

        assertThat(viewModel.state.value.bookmarkedQuestionIds).doesNotContain(questionId)
    }

    @Test
    fun selectQuestion_movesCurrentIndexAndClosesQuestionList() {
        val viewModel = viewModel()

        viewModel.onIntent(QuizPlayAllIntent.OpenQuestionList)
        viewModel.onIntent(QuizPlayAllIntent.SelectQuestion(4))

        assertThat(viewModel.state.value.currentQuestionIndex).isEqualTo(4)
        assertThat(viewModel.state.value.isQuestionListVisible).isFalse()
    }

    @Test
    fun submitConfirmState_calculatesUnsolvedAndBookmarkedQuestions() {
        val viewModel = viewModel()
        val firstQuestion = viewModel.state.value.currentQuestion!!
        val firstOption = firstQuestion.options.first()

        viewModel.onIntent(QuizPlayAllIntent.SelectOption(firstOption.id))
        viewModel.onIntent(QuizPlayAllIntent.SelectQuestion(1))
        viewModel.onIntent(QuizPlayAllIntent.ToggleBookmark)
        viewModel.onIntent(QuizPlayAllIntent.SelectQuestion(3))
        viewModel.onIntent(QuizPlayAllIntent.ToggleBookmark)
        viewModel.onIntent(QuizPlayAllIntent.OpenSubmitConfirm)

        val state = viewModel.state.value

        assertThat(state.isSubmitConfirmVisible).isTrue()
        assertThat(state.unsolvedQuestions.map { it.number }).doesNotContain(1)
        assertThat(state.unsolvedQuestions).hasSize(9)
        assertThat(state.bookmarkedQuestions.map { it.number }).containsExactly(2, 4).inOrder()
    }

    @Test
    fun loadQuizSession_replacesMockQuestionsAndStartsPlaySession() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(serverQuizSession())
        repository.playSessionResult = NetworkResult.Success(
            QuizPlaySession(
                playSessionId = "play-1",
                clientSessionId = "client-1",
                quizSessionId = "session-1",
                playType = QuizPlayType.First,
                status = QuizPlaySessionStatus.InProgress,
                quizSession = null,
            ),
        )
        val viewModel = viewModel(repository)

        viewModel.onIntent(QuizPlayAllIntent.LoadQuizSession("session-1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(repository.loadedQuizSessionId).isEqualTo("session-1")
        assertThat(repository.startedQuizSessionId).isEqualTo("session-1")
        assertThat(state.quizSessionId).isEqualTo("session-1")
        assertThat(state.playSessionId).isEqualTo("play-1")
        assertThat(state.questions).hasSize(1)
        assertThat(state.questions.first().body).isEqualTo("서버 문제")
    }

    @Test
    fun loadQuizSession_withExistingRetryPlaySession_doesNotStartFirstPlaySession() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(serverQuizSession())
        repository.submitResult = NetworkResult.Success(result())
        val viewModel = viewModel(repository)

        viewModel.onIntent(
            QuizPlayAllIntent.LoadQuizSession(
                quizSessionId = "session-retry",
                clientSessionId = "client-retry",
                playSessionId = "play-retry",
                playType = QuizPlayType.RetryAll,
            ),
        )
        advanceUntilIdle()
        viewModel.onIntent(QuizPlayAllIntent.SelectOption("option-1"))

        viewModel.onIntent(QuizPlayAllIntent.Submit)
        advanceUntilIdle()

        assertThat(repository.startedQuizSessionId).isNull()
        assertThat(repository.submittedRequest?.clientSessionId).isEqualTo("client-retry")
        assertThat(repository.submittedRequest?.playType).isEqualTo(QuizPlayType.RetryAll)
    }

    @Test
    fun loadQuizSession_withExistingRetryPlaySession_usesServerPlayMode() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(
            serverQuizSession(playMode = QuizPlayMode.OneByOne),
        )
        val viewModel = viewModel(repository)

        viewModel.onIntent(
            QuizPlayAllIntent.LoadQuizSession(
                quizSessionId = "session-retry",
                clientSessionId = "client-retry",
                playSessionId = "play-retry",
                playType = QuizPlayType.RetryAll,
            ),
        )
        advanceUntilIdle()

        assertThat(viewModel.state.value.isOneByOneMode).isTrue()
    }

    @Test
    fun retryLoadQuizSession_reloadsAfterInitialFailure() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Failure(
            code = "TIMEOUT",
            message = "timeout",
        )
        val viewModel = viewModel(repository)

        viewModel.onIntent(QuizPlayAllIntent.LoadQuizSession("session-1"))
        advanceUntilIdle()

        assertThat(repository.loadCallCount).isEqualTo(1)
        assertThat(viewModel.state.value.errorMessage)
            .isEqualTo("퀴즈를 불러오지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.")

        repository.quizSessionResult = NetworkResult.Success(serverQuizSession())
        repository.playSessionResult = NetworkResult.Success(
            QuizPlaySession(
                playSessionId = "play-1",
                clientSessionId = "client-1",
                quizSessionId = "session-1",
                playType = QuizPlayType.First,
                status = QuizPlaySessionStatus.InProgress,
                quizSession = null,
            ),
        )

        viewModel.onIntent(QuizPlayAllIntent.RetryLoadQuizSession)
        advanceUntilIdle()

        assertThat(repository.loadCallCount).isEqualTo(2)
        assertThat(viewModel.state.value.errorMessage).isNull()
        assertThat(viewModel.state.value.questions).hasSize(1)
    }

    @Test
    fun submit_sendsAnswersAndNavigatesToResult() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(serverQuizSession())
        repository.playSessionResult = NetworkResult.Success(
            QuizPlaySession(
                playSessionId = "play-1",
                clientSessionId = "client-1",
                quizSessionId = "session-1",
                playType = QuizPlayType.First,
                status = QuizPlaySessionStatus.InProgress,
                quizSession = null,
            ),
        )
        repository.submitResult = NetworkResult.Success(result())
        val viewModel = viewModel(repository)

        viewModel.onIntent(QuizPlayAllIntent.LoadQuizSession("session-1"))
        advanceUntilIdle()
        viewModel.onIntent(QuizPlayAllIntent.SelectOption("option-1"))
        val effect = async { viewModel.effect.first() }

        viewModel.onIntent(QuizPlayAllIntent.Submit)
        advanceUntilIdle()

        assertThat(repository.submittedRequest?.clientSessionId).isEqualTo("client-1")
        assertThat(repository.submittedRequest?.answers)
            .containsExactly(
                QuizAnswerSubmitItem(
                    questionId = "question-1",
                    selectedOptionId = "option-1",
                    correctClient = true,
                    skipped = false,
                    marked = false,
                ),
            )
        assertThat(viewModel.state.value.isSubmitting).isFalse()
        assertThat(effect.await()).isEqualTo(QuizPlayAllEffect.NavigateToResult("result-1"))
    }

    private fun viewModel(
        repository: QuizPlayRepository = FakeQuizPlayRepository(),
    ): QuizPlayAllViewModel = QuizPlayAllViewModel(repository)

    private class FakeQuizPlayRepository : QuizPlayRepository {
        var loadedQuizSessionId: String? = null
        var startedQuizSessionId: String? = null
        var loadCallCount: Int = 0
        var quizSessionResult: NetworkResult<QuizSession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
        var playSessionResult: NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
        var submitResult: NetworkResult<QuizResult> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
        var submittedRequest: QuizResultSubmit? = null

        override suspend fun getQuizSession(quizSessionId: String): NetworkResult<QuizSession> {
            loadCallCount += 1
            loadedQuizSessionId = quizSessionId
            return quizSessionResult
        }

        override suspend fun startQuizPlaySession(
            quizSessionId: String,
            request: QuizPlayStart,
        ): NetworkResult<QuizPlaySession> {
            startedQuizSessionId = quizSessionId
            return playSessionResult
        }

        override suspend fun submitQuizResult(
            request: QuizResultSubmit,
        ): NetworkResult<QuizResult> {
            submittedRequest = request
            return submitResult
        }

        override suspend fun getQuizResult(resultId: String): NetworkResult<QuizResult> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun retryAllQuestions(
            resultId: String,
            request: QuizRetry,
        ): NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun retryWrongQuestions(
            resultId: String,
            request: QuizRetry,
        ): NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
    }

    private fun result(): QuizResult = QuizResult(
        playSessionId = "play-1",
        resultId = "result-1",
        quizSessionId = "session-1",
        subjectId = "subject-1",
        subjectName = "SQLD",
        totalCount = 1,
        correctCount = 1,
        wrongCount = 0,
        skipCount = 0,
        accuracyPct = 100,
        elapsedMs = 0,
        scoreMatched = true,
        abuseFlagged = false,
        rewards = RewardSummary(
            dotoriEarned = 10,
            xpEarned = 5,
            leveledUp = false,
            newLevel = null,
            currentDotoriBalance = 110,
            currentXpTotal = 5,
        ),
        reviewItems = emptyList(),
        retryAvailable = null,
        createdAt = null,
    )

    private fun serverQuizSession(
        playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    ): QuizSession = QuizSession(
        id = "session-1",
        subjectId = "subject-1",
        subjectName = "SQLD",
        quizType = ServerQuizType.MultipleChoice,
        choiceCount = 4,
        questionCount = 1,
        playMode = playMode,
        timerEnabled = true,
        timerScope = "per_question",
        timerSeconds = 30,
        difficulty = QuizDifficulty.Medium,
        status = QuizGenerationStatus.Completed,
        questions = listOf(
            Question(
                id = "question-1",
                subjectId = "subject-1",
                chapterId = "chapter-1",
                partId = "part-1",
                partName = "파트",
                questionType = ServerQuizType.MultipleChoice,
                difficulty = QuizDifficulty.Medium,
                summary = null,
                body = "서버 문제",
                correctExplanation = null,
                incorrectExplanation = null,
                displayOrder = 1,
                options = listOf(
                    QuestionOption(id = "option-1", optionNumber = 1, content = "보기 1"),
                    QuestionOption(id = "option-2", optionNumber = 2, content = "보기 2"),
                ),
                answer = QuestionAnswer(answerValue = "option-1"),
            ),
        ),
    )
}
