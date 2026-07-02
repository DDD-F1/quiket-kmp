package com.f1.quiket.feature.home.presentation

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.home.domain.model.HomeData
import com.f1.quiket.feature.home.domain.model.HomeUserSummary
import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizGenerationAccepted
import com.f1.quiket.feature.home.domain.model.QuizGenerationProgress
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizScope
import com.f1.quiket.feature.home.domain.model.QuizScopeChapter
import com.f1.quiket.feature.home.domain.model.QuizScopePart
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.RecentActivity
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import com.f1.quiket.feature.home.domain.repository.QuizGenerationRepository
import com.f1.quiket.feature.home.domain.usecase.BuildQuizCreateUseCase
import com.f1.quiket.feature.home.presentation.contract.CreateQuizEffect
import com.f1.quiket.feature.home.presentation.contract.CreateQuizIntent
import com.f1.quiket.feature.home.presentation.contract.CreateQuizStep
import com.f1.quiket.feature.home.presentation.contract.QuizDifficultyOption
import com.f1.quiket.feature.home.presentation.contract.QuizQuestionCountOption
import com.f1.quiket.feature.home.presentation.contract.QuizTypeOption
import com.f1.quiket.feature.home.presentation.viewmodel.CreateQuizViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateQuizViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsHomeSubjectsWithServerCounts() = runTest {
        val homeRepository = FakeHomeRepository()
        homeRepository.homeResult = NetworkResult.Success(homeData())

        val viewModel = viewModel(homeRepository = homeRepository)
        advanceUntilIdle()

        val subjects = viewModel.state.value.subjects
        assertThat(subjects).hasSize(2)
        assertThat(subjects.first().id).isEqualTo("subject-1")
        assertThat(subjects.first().name).isEqualTo("SQLD")
        assertThat(subjects.first().chapterCount).isEqualTo(3)
        assertThat(subjects.first().partCount).isEqualTo(7)
        assertThat(viewModel.state.value.isLoadingSubjects).isFalse()
    }

    @Test
    fun loadQuizScope_success_replacesSelectedSubjectWithSortedScope() = runTest {
        val homeRepository = FakeHomeRepository()
        val quizGenerationRepository = FakeQuizGenerationRepository()
        homeRepository.homeResult = NetworkResult.Success(homeData())
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        val viewModel = viewModel(homeRepository, quizGenerationRepository)
        advanceUntilIdle()

        viewModel.onIntent(CreateQuizIntent.LoadQuizScope("subject-1"))
        advanceUntilIdle()

        val subject = viewModel.state.value.subjects.first { it.id == "subject-1" }
        assertThat(quizGenerationRepository.loadedScopeSubjectId).isEqualTo("subject-1")
        assertThat(subject.name).isEqualTo("SQLD 상세")
        assertThat(subject.chapterCount).isEqualTo(2)
        assertThat(subject.partCount).isEqualTo(3)
        assertThat(subject.chapters.map { it.id }).containsExactly("chapter-1", "chapter-2").inOrder()
        assertThat(subject.chapters.first().parts.map { it.id })
            .containsExactly("part-1", "part-2")
            .inOrder()
        assertThat(viewModel.state.value.loadingScopeSubjectId).isNull()
    }

    @Test
    fun selectMultipleChoice_defaultsChoiceCountToFour() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onIntent(CreateQuizIntent.SelectQuizType(QuizTypeOption.MultipleChoice))

        assertThat(viewModel.state.value.selectedChoiceCount).isEqualTo(4)
    }

    @Test
    fun createQuiz_passesRequestAndEmitsCreated_whenAcceptedAlreadyCompleted() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Success(
            QuizGenerationAccepted(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.Completed,
                estimatedSeconds = null,
            ),
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        val request = quizCreateRequest()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()
        val effects = mutableListOf<CreateQuizEffect>()
        val collectJob = launch { viewModel.effect.take(2).toList(effects) }

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        advanceUntilIdle()

        assertThat(quizGenerationRepository.createdRequest).isEqualTo(request)
        assertThat(viewModel.state.value.isCreatingQuiz).isFalse()
        assertThat(viewModel.state.value.generationProgress).isEqualTo(1f)
        assertThat(effects)
            .containsExactly(
                CreateQuizEffect.QuizGenerationStarted,
                CreateQuizEffect.QuizCreated("session-1"),
            )
            .inOrder()
        collectJob.cancel()
    }

    @Test
    fun createQuiz_pollsGenerationStatusAndEmitsCreated_whenPendingThenCompleted() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Success(
            QuizGenerationAccepted(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.Pending,
                estimatedSeconds = 3,
            ),
        )
        quizGenerationRepository.generationStatusResults += NetworkResult.Success(
            QuizGenerationProgress(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.Completed,
                estimatedSeconds = null,
                progressPct = 80,
                generatedCount = 5,
                failReason = null,
            ),
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()
        val effects = mutableListOf<CreateQuizEffect>()
        val collectJob = launch { viewModel.effect.take(2).toList(effects) }

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        advanceUntilIdle()

        assertThat(quizGenerationRepository.loadedStatusSessionIds).containsExactly("session-1")
        assertThat(viewModel.state.value.isCreatingQuiz).isFalse()
        assertThat(viewModel.state.value.generationProgress).isEqualTo(1f)
        assertThat(effects)
            .containsExactly(
                CreateQuizEffect.QuizGenerationStarted,
                CreateQuizEffect.QuizCreated("session-1"),
            )
            .inOrder()
        collectJob.cancel()
    }

    @Test
    fun createQuiz_incrementsVisibleProgress_whenServerProgressStaysFixed() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Success(
            QuizGenerationAccepted(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.InProgress,
                estimatedSeconds = null,
            ),
        )
        quizGenerationRepository.generationStatusResults += quizGenerationProgress(
            status = QuizGenerationStatus.InProgress,
            progressPct = 10,
        )
        quizGenerationRepository.generationStatusResults += quizGenerationProgress(
            status = QuizGenerationStatus.InProgress,
            progressPct = 10,
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        runCurrent()

        val firstPolledProgress = viewModel.state.value.generationProgress
        assertThat(firstPolledProgress).isGreaterThan(0.1f)

        advanceTimeBy(1_000L)
        runCurrent()

        val secondPolledProgress = viewModel.state.value.generationProgress
        assertThat(secondPolledProgress).isGreaterThan(firstPolledProgress)
        assertThat(secondPolledProgress).isLessThan(1f)

        quizGenerationRepository.generationStatusResults += quizGenerationProgress(
            status = QuizGenerationStatus.Completed,
            progressPct = 10,
        )
        advanceUntilIdle()

        assertThat(viewModel.state.value.generationProgress).isEqualTo(1f)
    }

    @Test
    fun createQuiz_failure_resetsProgressAndEmitsFinishedAndMessage() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Failure(
            code = "QUIZ_CONFLICT",
            message = "이미 생성 중인 퀴즈가 있어요.",
            httpCode = 409,
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()
        val effects = mutableListOf<CreateQuizEffect>()
        val collectJob = launch { viewModel.effect.take(3).toList(effects) }

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isCreatingQuiz).isFalse()
        assertThat(viewModel.state.value.generationProgress).isEqualTo(0f)
        assertThat(viewModel.state.value.currentStep).isEqualTo(CreateQuizStep.Options)
        assertThat(effects)
            .containsExactly(
                CreateQuizEffect.QuizGenerationStarted,
                CreateQuizEffect.QuizGenerationFinished,
                CreateQuizEffect.ShowMessage("이미 생성 중인 퀴즈가 있어요."),
            )
            .inOrder()
        collectJob.cancel()
    }

    @Test
    fun createQuiz_statusFailure_resetsCreatingAndEmitsFinishedAndMessage() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Success(
            QuizGenerationAccepted(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.InProgress,
                estimatedSeconds = 3,
            ),
        )
        quizGenerationRepository.generationStatusResults += NetworkResult.Failure(
            code = "SERVER_ERROR",
            message = "상태 조회에 실패했어요.",
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()
        val effects = mutableListOf<CreateQuizEffect>()
        val collectJob = launch { viewModel.effect.take(3).toList(effects) }

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isCreatingQuiz).isFalse()
        assertThat(effects)
            .containsExactly(
                CreateQuizEffect.QuizGenerationStarted,
                CreateQuizEffect.QuizGenerationFinished,
                CreateQuizEffect.ShowMessage("상태 조회에 실패했어요."),
            )
            .inOrder()
        collectJob.cancel()
    }

    @Test
    fun createQuiz_generationFailed_keepsFailReasonForDialog() = runTest {
        val quizGenerationRepository = FakeQuizGenerationRepository()
        quizGenerationRepository.quizScopeResult = NetworkResult.Success(quizScope())
        quizGenerationRepository.createResult = NetworkResult.Success(
            QuizGenerationAccepted(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.InProgress,
                estimatedSeconds = 3,
            ),
        )
        quizGenerationRepository.generationStatusResults += NetworkResult.Success(
            QuizGenerationProgress(
                quizSessionId = "session-1",
                jobId = "job-1",
                status = QuizGenerationStatus.Failed,
                estimatedSeconds = null,
                progressPct = 40,
                generatedCount = 0,
                failReason = "출제 범위 밖 partId가 포함되었습니다.",
            ),
        )
        val viewModel = viewModel(quizGenerationRepository = quizGenerationRepository)
        advanceUntilIdle()
        viewModel.selectSubjectAndScope()
        advanceUntilIdle()
        viewModel.selectDefaultOptions()

        viewModel.onIntent(CreateQuizIntent.CreateQuiz)
        advanceUntilIdle()

        assertThat(viewModel.state.value.currentStep).isEqualTo(CreateQuizStep.Options)
        assertThat(viewModel.state.value.isCreatingQuiz).isFalse()
        assertThat(viewModel.state.value.generationFailureMessage)
            .isEqualTo("출제 범위 밖 partId가 포함되었습니다.")

        viewModel.onIntent(CreateQuizIntent.DismissGenerationFailure)

        assertThat(viewModel.state.value.generationFailureMessage).isNull()
    }

    private fun viewModel(
        homeRepository: FakeHomeRepository = FakeHomeRepository().apply {
            homeResult = NetworkResult.Success(homeData())
        },
        quizGenerationRepository: FakeQuizGenerationRepository = FakeQuizGenerationRepository(),
    ): CreateQuizViewModel = CreateQuizViewModel(
        homeRepository = homeRepository,
        quizGenerationRepository = quizGenerationRepository,
        buildQuizCreate = BuildQuizCreateUseCase(),
    )

    private class FakeHomeRepository : HomeRepository {
        var homeResult: NetworkResult<HomeData> = NetworkResult.Failure(
            code = "TEST",
            message = "home not configured",
        )

        override suspend fun getHome(): NetworkResult<HomeData> = homeResult

        override suspend fun getRecentActivities(
            page: Int,
            size: Int,
        ): NetworkResult<List<RecentActivity>> = NetworkResult.Success(emptyList())

        override suspend fun getSubjects(
            page: Int,
            size: Int,
        ): NetworkResult<List<QuizSubjectSummary>> = NetworkResult.Success(emptyList())
    }

    private class FakeQuizGenerationRepository : QuizGenerationRepository {
        var loadedScopeSubjectId: String? = null
        var createdRequest: QuizCreate? = null
        val loadedStatusSessionIds = mutableListOf<String>()
        val generationStatusResults = ArrayDeque<NetworkResult<QuizGenerationProgress>>()

        var quizScopeResult: NetworkResult<QuizScope> = NetworkResult.Failure(
            code = "TEST",
            message = "scope not configured",
        )
        var createResult: NetworkResult<QuizGenerationAccepted> = NetworkResult.Failure(
            code = "TEST",
            message = "create not configured",
        )

        override suspend fun getQuizScope(subjectId: String): NetworkResult<QuizScope> {
            loadedScopeSubjectId = subjectId
            return quizScopeResult
        }

        override suspend fun createQuizSession(
            request: QuizCreate,
        ): NetworkResult<QuizGenerationAccepted> {
            createdRequest = request
            return createResult
        }

        override suspend fun getGenerationStatus(
            quizSessionId: String,
        ): NetworkResult<QuizGenerationProgress> {
            loadedStatusSessionIds += quizSessionId
            return generationStatusResults.removeFirstOrNull()
                ?: NetworkResult.Failure(code = "TEST", message = "status not configured")
        }
    }
}

private fun CreateQuizViewModel.selectSubjectAndScope() {
    onIntent(CreateQuizIntent.SelectSubject("subject-1"))
    onIntent(CreateQuizIntent.SubjectNextClick)
}

private fun CreateQuizViewModel.selectDefaultOptions() {
    onIntent(CreateQuizIntent.SelectQuizType(QuizTypeOption.MultipleChoice))
    onIntent(CreateQuizIntent.SelectChoiceCount(4))
    onIntent(CreateQuizIntent.SelectQuestionCount(QuizQuestionCountOption.Five))
    onIntent(CreateQuizIntent.SelectDifficulty(QuizDifficultyOption.Normal))
}

private fun homeData(): HomeData = HomeData(
    user = HomeUserSummary(
        nickname = "테스터",
        dotoriBalance = 100,
        xpTotal = 0,
        currentLevel = 1,
        levelName = null,
    ),
    hero = null,
    dDayCards = emptyList(),
    subjects = listOf(
        QuizSubjectSummary(
            id = "subject-1",
            name = "SQLD",
            purpose = "CERTIFICATE",
            chapterCount = 3,
            partCount = 7,
            lastActivityAt = null,
            examSchedule = null,
        ),
        QuizSubjectSummary(
            id = "subject-2",
            name = "서양철학사",
            purpose = "STUDY",
            chapterCount = 1,
            partCount = 1,
            lastActivityAt = null,
            examSchedule = null,
        ),
    ),
    recentActivities = emptyList(),
)

private fun quizScope(): QuizScope = QuizScope(
    subjectId = "subject-1",
    subjectName = "SQLD 상세",
    chapters = listOf(
        QuizScopeChapter(
            id = "chapter-2",
            subjectId = "subject-1",
            name = "데이터 모델",
            displayOrder = 2,
            parts = listOf(
                QuizScopePart(
                    id = "part-3",
                    chapterId = "chapter-2",
                    name = "정규화",
                    partNumber = 1,
                    contentPreview = null,
                ),
            ),
        ),
        QuizScopeChapter(
            id = "chapter-1",
            subjectId = "subject-1",
            name = "SQLD 기본",
            displayOrder = 1,
            parts = listOf(
                QuizScopePart(
                    id = "part-2",
                    chapterId = "chapter-1",
                    name = "데이터모델링",
                    partNumber = 2,
                    contentPreview = null,
                ),
                QuizScopePart(
                    id = "part-1",
                    chapterId = "chapter-1",
                    name = "SQLD 개요",
                    partNumber = 1,
                    contentPreview = null,
                ),
            ),
        ),
    ),
)

private fun quizGenerationProgress(
    status: QuizGenerationStatus,
    progressPct: Int?,
): NetworkResult<QuizGenerationProgress> = NetworkResult.Success(
    QuizGenerationProgress(
        quizSessionId = "session-1",
        jobId = "job-1",
        status = status,
        estimatedSeconds = null,
        progressPct = progressPct,
        generatedCount = null,
        failReason = null,
    ),
)

private fun quizCreateRequest(): QuizCreate = QuizCreate(
    subjectId = "subject-1",
    partIds = listOf("part-1", "part-2", "part-3"),
    quizType = ServerQuizType.MultipleChoice,
    choiceCount = 4,
    questionCount = 5,
    playMode = QuizPlayMode.AllAtOnce,
    timerEnabled = false,
    difficulty = QuizDifficulty.Medium,
)
