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
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizResult
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.QuizPlayRepository
import com.f1.quiket.feature.home.presentation.contract.QuizStartIntent
import com.f1.quiket.feature.home.presentation.viewmodel.QuizStartViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizStartViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun load_success_mapsMultipleChoiceSummary() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(quizSession())
        val viewModel = QuizStartViewModel(repository)

        viewModel.onIntent(QuizStartIntent.Load("session-1"))
        advanceUntilIdle()

        val summary = viewModel.state.value.summary!!
        assertThat(repository.loadedQuizSessionId).isEqualTo("session-1")
        assertThat(summary.title).isEqualTo("SQLD")
        assertThat(summary.quizTypeLabel).isEqualTo("객관식")
        assertThat(summary.choiceLabel).isEqualTo("4지선다")
        assertThat(summary.questionCountLabel).isEqualTo("2문제")
        assertThat(summary.difficultyLabel).isEqualTo("난이도: 보통")
        assertThat(summary.scopeLabels).containsExactly("SQLD 개요", "데이터모델링").inOrder()
        assertThat(viewModel.state.value.errorMessage).isNull()
    }

    @Test
    fun load_success_mapsOxSummaryWithoutChoiceLabel() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(
            quizSession(
                quizType = ServerQuizType.Ox,
                choiceCount = null,
                difficulty = QuizDifficulty.Hard,
            ),
        )
        val viewModel = QuizStartViewModel(repository)

        viewModel.onIntent(QuizStartIntent.Load("session-1"))
        advanceUntilIdle()

        val summary = viewModel.state.value.summary!!
        assertThat(summary.quizTypeLabel).isEqualTo("O/X 퀴즈")
        assertThat(summary.choiceLabel).isNull()
        assertThat(summary.difficultyLabel).isEqualTo("난이도: 어려움")
    }

    @Test
    fun load_blankSessionId_setsError() {
        val viewModel = QuizStartViewModel(FakeQuizPlayRepository())

        viewModel.onIntent(QuizStartIntent.Load(null))

        assertThat(viewModel.state.value.summary).isNull()
        assertThat(viewModel.state.value.errorMessage).isEqualTo("퀴즈 정보를 불러올 수 없어요.")
    }

    @Test
    fun load_failure_setsError() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Failure(
            code = "NOT_FOUND",
            message = "퀴즈가 없어요.",
        )
        val viewModel = QuizStartViewModel(repository)

        viewModel.onIntent(QuizStartIntent.Load("session-1"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.summary).isNull()
        assertThat(viewModel.state.value.errorMessage).isEqualTo("퀴즈가 없어요.")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun load_failureAfterSuccess_clearsSummaryAndSanitizesTimeout() = runTest {
        val repository = FakeQuizPlayRepository()
        repository.quizSessionResult = NetworkResult.Success(quizSession())
        val viewModel = QuizStartViewModel(repository)

        viewModel.onIntent(QuizStartIntent.Load("session-1"))
        advanceUntilIdle()

        repository.quizSessionResult = NetworkResult.Failure(
            code = "TIMEOUT",
            message = "timeout",
        )
        viewModel.onIntent(QuizStartIntent.Load("session-1"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.summary).isNull()
        assertThat(viewModel.state.value.errorMessage)
            .isEqualTo("퀴즈 정보를 불러올 수 없어요. 잠시 후 다시 시도해 주세요.")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    private class FakeQuizPlayRepository : QuizPlayRepository {
        var loadedQuizSessionId: String? = null
        var quizSessionResult: NetworkResult<QuizSession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun getQuizSession(quizSessionId: String): NetworkResult<QuizSession> {
            loadedQuizSessionId = quizSessionId
            return quizSessionResult
        }

        override suspend fun startQuizPlaySession(
            quizSessionId: String,
            request: QuizPlayStart,
        ): NetworkResult<QuizPlaySession> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun submitQuizResult(request: QuizResultSubmit): NetworkResult<QuizResult> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

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
}

private fun quizSession(
    quizType: ServerQuizType = ServerQuizType.MultipleChoice,
    choiceCount: Int? = 4,
    difficulty: QuizDifficulty = QuizDifficulty.Medium,
): QuizSession = QuizSession(
    id = "session-1",
    subjectId = "subject-1",
    subjectName = "SQLD",
    quizType = quizType,
    choiceCount = choiceCount,
    questionCount = 2,
    playMode = QuizPlayMode.AllAtOnce,
    timerEnabled = false,
    timerScope = null,
    timerSeconds = null,
    difficulty = difficulty,
    status = QuizGenerationStatus.Completed,
    questions = listOf(
        question(id = "question-1", partName = "SQLD 개요", displayOrder = 1),
        question(id = "question-2", partName = "데이터모델링", displayOrder = 2),
    ),
)

private fun question(
    id: String,
    partName: String,
    displayOrder: Int,
): Question = Question(
    id = id,
    subjectId = "subject-1",
    chapterId = "chapter-1",
    partId = "part-$displayOrder",
    partName = partName,
    questionType = ServerQuizType.MultipleChoice,
    difficulty = QuizDifficulty.Medium,
    summary = null,
    body = "문제 $displayOrder",
    correctExplanation = null,
    incorrectExplanation = null,
    displayOrder = displayOrder,
    options = listOf(
        QuestionOption(id = "option-$displayOrder-1", optionNumber = 1, content = "보기 1"),
        QuestionOption(id = "option-$displayOrder-2", optionNumber = 2, content = "보기 2"),
    ),
    answer = QuestionAnswer(answerValue = "option-$displayOrder-1"),
)
