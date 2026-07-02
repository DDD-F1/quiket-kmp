package com.f1.quiket.composeapp.quiz.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.quiz.QuizCreateStep
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationStatus
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.QuizTypeOption
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import com.f1.quiket.composeapp.quiz.domain.usecase.BuildQuizCreateRequestUseCase
import com.f1.quiket.composeapp.quiz.domain.usecase.QuizPlayUseCases
import com.f1.quiket.composeapp.quiz.toQuizNetworkAwareMessage
import com.f1.quiket.composeapp.quiz.withQuizNetworkRetryGuide
import com.f1.quiket.composeapp.subject.PartSummary
import com.f1.quiket.composeapp.subject.SubjectException
import com.f1.quiket.composeapp.subject.SubjectListItem
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.TimeSource

internal class QuizCreateStateHolder(
    private val subjectUseCases: SubjectUseCases,
    private val quizPlayUseCases: QuizPlayUseCases,
    private val buildQuizCreateRequest: BuildQuizCreateRequestUseCase,
) {
    var subjects by mutableStateOf<List<SubjectListItem>>(emptyList())
        private set

    var subjectDetail by mutableStateOf<QuizScope?>(null)
        private set

    var selectedSubjectId by mutableStateOf<String?>(null)
        private set

    var selectedPartIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var currentStep by mutableStateOf(QuizCreateStep.Subject)
        private set

    var expandedChapterId by mutableStateOf<String?>(null)
        private set

    var quizType by mutableStateOf<QuizTypeOption?>(null)
        private set

    var choiceCount by mutableStateOf<Int?>(null)
        private set

    var questionCountPreset by mutableStateOf<Int?>(null)
        private set

    var questionCountText by mutableStateOf("")
        private set

    var difficulty by mutableStateOf<QuizDifficulty?>(null)
        private set

    var isLoadingSubjects by mutableStateOf(true)
        private set

    var isLoadingDetail by mutableStateOf(false)
        private set

    var isGenerating by mutableStateOf(false)
        private set

    var canBrowseDuringGeneration by mutableStateOf(false)
        private set

    var generationProgress by mutableStateOf(0f)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    val selectedSubjectDetail: QuizScope?
        get() = subjectDetail?.takeIf { detail -> detail.id == selectedSubjectId }

    suspend fun loadSubjects(onSessionExpired: () -> Unit) {
        isLoadingSubjects = true
        message = null
        runCatching {
            subjectUseCases.getSubjects()
        }.onSuccess { items ->
            subjects = items
            val selectedSubjectStillExists = selectedSubjectId?.let { subjectId ->
                items.any { subject -> subject.id == subjectId }
            } ?: true
            if (!selectedSubjectStillExists) {
                selectedSubjectId = null
                subjectDetail = null
                selectedPartIds = emptySet()
                expandedChapterId = null
                currentStep = QuizCreateStep.Subject
            }
            isLoadingSubjects = false
        }.onFailure { error ->
            isLoadingSubjects = false
            handleError(
                error = error,
                fallback = "과목을 불러오지 못했습니다.",
                onSessionExpired = onSessionExpired,
            )
        }
    }

    suspend fun loadSelectedSubjectDetail(onSessionExpired: () -> Unit) {
        val subjectId = selectedSubjectId ?: return
        isLoadingDetail = true
        message = null
        runCatching {
            quizPlayUseCases.getQuizScope(subjectId)
        }.onSuccess { detail ->
            subjectDetail = detail
            selectedPartIds = detail.allParts().map { part -> part.id }.toSet()
            expandedChapterId = null
            isLoadingDetail = false
        }.onFailure { error ->
            isLoadingDetail = false
            handleError(
                error = error,
                fallback = "과목 상세를 불러오지 못했습니다.",
                onSessionExpired = onSessionExpired,
            )
        }
    }

    fun moveBack(): Boolean =
        when (currentStep) {
            QuizCreateStep.Subject -> true
            QuizCreateStep.Scope -> {
                currentStep = QuizCreateStep.Subject
                false
            }

            QuizCreateStep.Options -> {
                currentStep = QuizCreateStep.Scope
                false
            }

            QuizCreateStep.Loading -> canBrowseDuringGeneration
        }

    fun moveToScope() {
        if (selectedSubjectId == null) {
            message = "과목을 선택해주세요."
            return
        }
        message = null
        currentStep = QuizCreateStep.Scope
    }

    fun moveToOptions() {
        if (selectedPartIds.isEmpty()) {
            message = "출제 범위를 선택해주세요."
            return
        }
        message = null
        currentStep = QuizCreateStep.Options
    }

    fun selectSubject(subject: SubjectListItem) {
        selectedSubjectId = subject.id
        message = null
        if (selectedSubjectDetail?.id != subject.id) {
            selectedPartIds = emptySet()
        }
    }

    fun toggleChapterExpanded(chapterId: String) {
        expandedChapterId = if (expandedChapterId == chapterId) null else chapterId
    }

    fun toggleChapterParts(chapterId: String) {
        val chapter = selectedSubjectDetail?.chapters?.firstOrNull { it.id == chapterId } ?: return
        val partIds = chapter.parts.map { part -> part.id }.toSet()
        selectedPartIds = if (partIds.all { partId -> partId in selectedPartIds }) {
            selectedPartIds - partIds
        } else {
            selectedPartIds + partIds
        }
    }

    fun togglePart(part: PartSummary) {
        selectedPartIds = if (part.id in selectedPartIds) {
            selectedPartIds - part.id
        } else {
            selectedPartIds + part.id
        }
    }

    fun clearParts() {
        selectedPartIds = emptySet()
    }

    fun selectQuizType(nextType: QuizTypeOption) {
        quizType = nextType
        choiceCount = if (nextType.requiresChoiceCount) {
            choiceCount ?: 4
        } else {
            null
        }
    }

    fun selectChoiceCount(count: Int) {
        choiceCount = count
    }

    fun selectQuestionCount(count: Int) {
        questionCountPreset = count
        questionCountText = ""
    }

    fun updateQuestionCountText(input: String) {
        questionCountPreset = null
        questionCountText = input.filter(Char::isDigit).take(3)
    }

    fun selectDifficulty(nextDifficulty: QuizDifficulty) {
        difficulty = nextDifficulty
    }

    suspend fun createQuiz(
        onSessionExpired: () -> Unit,
        onQuizGenerationStarted: () -> Unit,
        onQuizGenerationFinished: () -> Unit,
    ): QuizPlayLaunchConfig? {
        if (isGenerating) return null

        val detail = selectedSubjectDetail
        val selectedParts = selectedPartIds.toList()
        val selectedQuizOption = quizType
        val selectedQuizType = selectedQuizOption?.serverType
        val selectedDifficulty = difficulty
        val questionCount = questionCountPreset ?: questionCountText.toIntOrNull()?.coerceIn(1, 100)

        when {
            detail == null -> {
                message = "과목 정보를 먼저 불러와야 해요."
                return null
            }

            selectedParts.isEmpty() -> {
                message = "출제할 파트를 선택해주세요."
                return null
            }

            selectedQuizOption == null -> {
                message = "퀴즈 유형을 선택해주세요."
                return null
            }

            selectedQuizType == null -> {
                message = "아직 지원하지 않는 퀴즈 유형이에요."
                return null
            }

            selectedQuizType == ServerQuizType.MultipleChoice && choiceCount == null -> {
                message = "보기수를 선택해주세요."
                return null
            }

            questionCount == null -> {
                message = "문제수를 선택해주세요."
                return null
            }

            selectedDifficulty == null -> {
                message = "난이도를 선택해주세요."
                return null
            }
        }

        isGenerating = true
        canBrowseDuringGeneration = false
        generationProgress = 0.05f
        message = "퀴즈를 생성하고 있어요."
        currentStep = QuizCreateStep.Loading
        onQuizGenerationStarted()
        val generationStartedAt = TimeSource.Monotonic.markNow()

        return runCatching {
            val request = buildQuizCreateRequest(
                subjectId = detail.id,
                partIds = selectedParts,
                quizType = selectedQuizType,
                choiceCount = choiceCount,
                questionCount = questionCount,
                difficulty = selectedDifficulty,
            )
            val accepted = quizPlayUseCases.createQuizSession(request)
            canBrowseDuringGeneration = true
            generationProgress = maxOf(generationProgress, 0.1f)
            val quizSessionId = if (accepted.status == QuizGenerationStatus.Completed) {
                accepted.quizSessionId
            } else {
                pollQuizGeneration(
                    quizSessionId = accepted.quizSessionId,
                    onProgress = { progress -> generationProgress = progress },
                )
            }
            val remainingVisibleMillis =
                MinimumQuizLoadingVisibleMillis - generationStartedAt.elapsedNow().inWholeMilliseconds
            if (remainingVisibleMillis > 0) {
                delay(remainingVisibleMillis)
            }
            QuizPlayLaunchConfig(quizSessionId = quizSessionId)
        }.fold(
            onSuccess = { launchConfig ->
                isGenerating = false
                canBrowseDuringGeneration = false
                generationProgress = 1f
                message = null
                onQuizGenerationFinished()
                launchConfig
            },
            onFailure = { error ->
                if (error is CancellationException) throw error
                isGenerating = false
                canBrowseDuringGeneration = false
                generationProgress = 0f
                currentStep = QuizCreateStep.Options
                onQuizGenerationFinished()
                handleError(
                    error = error,
                    fallback = "퀴즈 생성에 실패했습니다.",
                    onSessionExpired = onSessionExpired,
                )
                null
            },
        )
    }

    private suspend fun pollQuizGeneration(
        quizSessionId: String,
        onProgress: (Float) -> Unit,
    ): String {
        repeat(QuizGenerationMaxAttempts) { attempt ->
            if (attempt > 0) {
                delay(QuizGenerationPollIntervalMillis)
            }
            val progress = quizPlayUseCases.getQuizGenerationStatus(quizSessionId)
            val visibleProgress = progress.progressPct
                ?.coerceIn(0, 100)
                ?.div(100f)
                ?: ((attempt + 1).toFloat() / QuizGenerationMaxAttempts)
                    .coerceAtMost(0.95f)
            onProgress(visibleProgress)

            when (progress.status) {
                QuizGenerationStatus.Completed -> return quizSessionId
                QuizGenerationStatus.Failed -> throw QuizPlayException(
                    progress.failReason ?: "퀴즈 생성에 실패했습니다.",
                )

                QuizGenerationStatus.Pending,
                QuizGenerationStatus.InProgress,
                QuizGenerationStatus.Unknown,
                -> Unit
            }
        }

        throw QuizPlayException("퀴즈 생성 상태 확인 시간이 초과됐어요.")
    }

    private fun handleError(
        error: Throwable,
        fallback: String,
        onSessionExpired: () -> Unit,
    ) {
        val timeoutMessage = fallback.withQuizNetworkRetryGuide()
        when (error) {
            is SubjectException -> {
                if (error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    message = error.toQuizNetworkAwareMessage(
                        fallback = fallback,
                        timeoutMessage = timeoutMessage,
                    )
                }
            }

            is QuizPlayException -> {
                if (error.isUnauthorized) {
                    onSessionExpired()
                } else {
                    message = error.toQuizNetworkAwareMessage(
                        fallback = fallback,
                        timeoutMessage = timeoutMessage,
                    )
                }
            }

            else -> message = error.toQuizNetworkAwareMessage(
                fallback = fallback,
                timeoutMessage = timeoutMessage,
            )
        }
    }
}

private fun QuizScope.allParts(): List<PartSummary> =
    chapters
        .sortedBy { chapter -> chapter.displayOrder }
        .flatMap { chapter -> chapter.parts.sortedBy { part -> part.partNumber } }

private const val MinimumQuizLoadingVisibleMillis = 1_800L
private const val QuizGenerationPollIntervalMillis = 1_000L
private const val QuizGenerationMaxAttempts = 120
