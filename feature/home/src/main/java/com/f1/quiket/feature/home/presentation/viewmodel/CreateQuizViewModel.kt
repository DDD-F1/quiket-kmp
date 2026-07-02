package com.f1.quiket.feature.home.presentation.viewmodel

import com.f1.quiket.feature.home.presentation.contract.*

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizScope
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import com.f1.quiket.feature.home.domain.repository.HomeRepository
import com.f1.quiket.feature.home.domain.repository.QuizGenerationRepository
import com.f1.quiket.feature.home.domain.usecase.BuildQuizCreateUseCase
import com.f1.quiket.feature.home.domain.usecase.QuizCreateDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay

@HiltViewModel
class CreateQuizViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val quizGenerationRepository: QuizGenerationRepository,
    private val buildQuizCreate: BuildQuizCreateUseCase,
) : MviViewModel<CreateQuizState, CreateQuizIntent, CreateQuizEffect>(
    initialState = CreateQuizState(),
) {
    init {
        loadSubjects()
    }

    override fun handleIntent(intent: CreateQuizIntent) {
        when (intent) {
            CreateQuizIntent.LoadSubjects -> loadSubjects()
            is CreateQuizIntent.LoadQuizScope -> loadQuizScope(intent.subjectId)
            is CreateQuizIntent.SelectSubject -> selectSubject(intent.subjectId)
            CreateQuizIntent.SubjectNextClick -> moveToScope()
            CreateQuizIntent.ScopeBackClick -> updateState { copy(currentStep = CreateQuizStep.Subject) }
            is CreateQuizIntent.ToggleChapterExpansion -> toggleChapterExpansion(intent.chapterId)
            is CreateQuizIntent.ToggleChapterSelection -> toggleChapterSelection(intent.chapterId)
            is CreateQuizIntent.TogglePartSelection -> togglePartSelection(intent.partId)
            CreateQuizIntent.ClearAllParts -> updateState { copy(selectedPartIds = emptyList()) }
            CreateQuizIntent.ScopeNextClick -> moveToOptions()
            CreateQuizIntent.OptionsBackClick -> updateState { copy(currentStep = CreateQuizStep.Scope) }
            is CreateQuizIntent.SelectQuizType -> selectQuizType(intent.quizType)
            is CreateQuizIntent.SelectChoiceCount -> updateState { copy(selectedChoiceCount = intent.count) }
            is CreateQuizIntent.SelectQuestionCount -> updateState { copy(selectedQuestionCountOption = intent.option) }
            CreateQuizIntent.OpenCustomQuestionCountDialog -> openCustomQuestionCountDialog()
            is CreateQuizIntent.ChangeCustomQuestionCountText -> updateState {
                copy(customQuestionCountText = intent.text.filter { character -> character.isDigit() })
            }
            CreateQuizIntent.DismissCustomQuestionCountDialog -> updateState {
                copy(customQuestionCountDialogVisible = false)
            }
            CreateQuizIntent.ApplyCustomQuestionCount -> applyCustomQuestionCount()
            is CreateQuizIntent.SelectDifficulty -> updateState { copy(selectedDifficulty = intent.difficulty) }
            CreateQuizIntent.CreateQuiz -> createQuiz()
            CreateQuizIntent.DismissGenerationFailure -> updateState {
                copy(generationFailureMessage = null)
            }
            CreateQuizIntent.BackClick -> handleBackClick()
        }
    }

    private fun loadSubjects() {
        if (currentState.isLoadingSubjects) return

        launch {
            updateState { copy(isLoadingSubjects = true) }

            when (val result = homeRepository.getHome()) {
                is NetworkResult.Success -> {
                    updateState {
                        val nextSubjects = result.data.subjects.map { subject -> subject.toUiModel() }
                        val selectedSubjectStillExists = nextSubjects.any { subject ->
                            subject.id == selectedSubjectId
                        }
                        copy(
                            subjects = nextSubjects,
                            isLoadingSubjects = false,
                            loadedScopeSubjectIds = emptySet(),
                            selectedSubjectId = selectedSubjectId.takeIf { selectedSubjectStillExists },
                            selectedPartIds = selectedPartIds.takeIf { selectedSubjectStillExists }.orEmpty(),
                            expandedChapterId = expandedChapterId.takeIf { selectedSubjectStillExists },
                            currentStep = if (selectedSubjectStillExists || currentStep == CreateQuizStep.Subject) {
                                currentStep
                            } else {
                                CreateQuizStep.Subject
                            },
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    updateState { copy(isLoadingSubjects = false) }
                    sendEffect(CreateQuizEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun loadQuizScope(subjectId: String) {
        val subject = currentState.subjects.firstOrNull { it.id == subjectId } ?: return
        if (subjectId in currentState.loadedScopeSubjectIds || currentState.loadingScopeSubjectId == subjectId) {
            return
        }

        launch {
            updateState { copy(loadingScopeSubjectId = subjectId) }

            when (val result = quizGenerationRepository.getQuizScope(subjectId)) {
                is NetworkResult.Success -> {
                    val scopedSubject = result.data.toUiModel(summary = subject)
                    updateState {
                        val shouldAutoSelect = selectedSubjectId == subjectId &&
                            shouldAutoSelectParts &&
                            selectedPartIds.isEmpty()
                        val scopedPartIds = scopedSubject.allPartIds()
                        copy(
                            subjects = subjects.map { item ->
                                if (item.id == subjectId) scopedSubject else item
                            },
                            loadedScopeSubjectIds = loadedScopeSubjectIds + subjectId,
                            loadingScopeSubjectId = null,
                            selectedPartIds = if (shouldAutoSelect && scopedPartIds.isNotEmpty()) {
                                scopedPartIds
                            } else {
                                selectedPartIds
                            },
                            shouldAutoSelectParts = if (shouldAutoSelect && scopedPartIds.isNotEmpty()) {
                                false
                            } else {
                                shouldAutoSelectParts
                            },
                        )
                    }
                }

                is NetworkResult.Failure -> {
                    updateState { copy(loadingScopeSubjectId = null) }
                    sendEffect(CreateQuizEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun selectSubject(subjectId: String) {
        val subject = currentState.subjects.firstOrNull { subject -> subject.id == subjectId } ?: return
        val isDifferentSubject = currentState.selectedSubjectId != subjectId
        if (!isDifferentSubject) return

        val partIds = subject.allPartIds()
        updateState {
            copy(
                selectedSubjectId = subjectId,
                expandedChapterId = null,
                selectedPartIds = partIds,
                shouldAutoSelectParts = partIds.isEmpty(),
                selectedQuizType = null,
                selectedChoiceCount = null,
                selectedQuestionCountOption = null,
                customQuestionCount = null,
                customQuestionCountDialogVisible = false,
                customQuestionCountText = "",
                selectedDifficulty = null,
            )
        }
    }

    private fun moveToScope() {
        val subject = currentState.selectedSubject ?: return
        val partIds = currentState.selectedPartIds.ifEmpty { subject.allPartIds() }

        updateState {
            copy(
                currentStep = CreateQuizStep.Scope,
                selectedPartIds = partIds,
                shouldAutoSelectParts = partIds.isEmpty(),
            )
        }
        loadQuizScope(subject.id)
    }

    private fun moveToOptions() {
        if (currentState.selectedSubject == null) return
        if (currentState.selectedPartIds.isEmpty()) {
            launch { sendEffect(CreateQuizEffect.ShowMessage("출제 범위를 선택해주세요.")) }
            return
        }
        updateState { copy(currentStep = CreateQuizStep.Options) }
    }

    private fun toggleChapterExpansion(chapterId: String) {
        updateState {
            copy(expandedChapterId = if (expandedChapterId == chapterId) null else chapterId)
        }
    }

    private fun toggleChapterSelection(chapterId: String) {
        val chapter = currentState.selectedSubject
            ?.chapters
            ?.firstOrNull { item -> item.id == chapterId }
            ?: return

        val currentSelectedPartIds = currentState.selectedPartIds.toSet()
        val chapterPartIds = chapter.parts.map { part -> part.id }.toSet()
        val nextSelectedPartIds = if (chapterPartIds.all { partId -> partId in currentSelectedPartIds }) {
            currentSelectedPartIds - chapterPartIds
        } else {
            currentSelectedPartIds + chapterPartIds
        }

        updateState { copy(selectedPartIds = nextSelectedPartIds.toList()) }
    }

    private fun togglePartSelection(partId: String) {
        updateState {
            copy(selectedPartIds = selectedPartIds.toggle(partId))
        }
    }

    private fun selectQuizType(quizType: QuizTypeOption) {
        updateState {
            copy(
                selectedQuizType = quizType,
                selectedChoiceCount = if (quizType.requiresChoiceCount) {
                    selectedChoiceCount ?: 4
                } else {
                    null
                },
            )
        }
    }

    private fun openCustomQuestionCountDialog() {
        updateState {
            copy(
                customQuestionCountDialogVisible = true,
                customQuestionCountText = customQuestionCount?.toString().orEmpty(),
            )
        }
    }

    private fun applyCustomQuestionCount() {
        val count = currentState.customQuestionCountText.toIntOrNull()
        if (count == null || count <= 0) return

        updateState {
            copy(
                customQuestionCount = count.coerceIn(1, 100),
                selectedQuestionCountOption = QuizQuestionCountOption.Custom,
                customQuestionCountDialogVisible = false,
            )
        }
    }

    private fun createQuiz() {
        if (currentState.isCreatingQuiz) return
        val request = buildQuizCreate(currentState.toQuizCreateDraft())

        if (request == null) {
            launch { sendEffect(CreateQuizEffect.ShowMessage(currentState.createValidationMessage())) }
            return
        }

        launch {
            updateState {
                copy(
                        currentStep = CreateQuizStep.Loading,
                        isCreatingQuiz = true,
                        canBrowseDuringGeneration = false,
                        generationProgress = 0.05f,
                        generationFailureMessage = null,
                )
            }
            sendEffect(CreateQuizEffect.QuizGenerationStarted)

            when (val result = quizGenerationRepository.createQuizSession(request)) {
                is NetworkResult.Success -> {
                    updateState {
                        copy(
                            generationProgress = 0.1f,
                            canBrowseDuringGeneration = true,
                        )
                    }
                    if (result.data.status == QuizGenerationStatus.Completed) {
                        updateState {
                            copy(
                                isCreatingQuiz = false,
                                canBrowseDuringGeneration = false,
                                generationProgress = 1f,
                            )
                        }
                        sendEffect(CreateQuizEffect.QuizCreated(result.data.quizSessionId))
                    } else {
                        pollGenerationStatus(result.data.quizSessionId)
                    }
                }

                is NetworkResult.Failure -> {
                    updateState {
                        copy(
                            currentStep = CreateQuizStep.Options,
                            isCreatingQuiz = false,
                            canBrowseDuringGeneration = false,
                            generationProgress = 0f,
                        )
                    }
                    sendEffect(CreateQuizEffect.QuizGenerationFinished)
                    sendEffect(CreateQuizEffect.ShowMessage(result.message))
                }
            }
        }
    }

    private fun handleBackClick() {
        updateState {
            copy(
                currentStep = when (currentStep) {
                    CreateQuizStep.Subject -> CreateQuizStep.Subject
                    CreateQuizStep.Scope -> CreateQuizStep.Subject
                    CreateQuizStep.Options -> CreateQuizStep.Scope
                    CreateQuizStep.Loading -> CreateQuizStep.Options
                },
            )
        }
    }

    private suspend fun pollGenerationStatus(quizSessionId: String) {
        repeat(GENERATION_POLL_MAX_ATTEMPTS) { attempt ->
            if (attempt > 0) {
                delay(GENERATION_POLL_INTERVAL_MILLIS)
            }

            when (val status = quizGenerationRepository.getGenerationStatus(quizSessionId)) {
                is NetworkResult.Success -> {
                    val serverProgress = status.data.progressPct
                        ?.coerceIn(0, 100)
                        ?.div(100f)

                    updateState {
                        copy(
                            generationProgress = generationProgress.nextVisibleGenerationProgress(
                                serverProgress = serverProgress,
                                status = status.data.status,
                            ),
                        )
                    }

                    when (status.data.status) {
                        QuizGenerationStatus.Completed -> {
                            updateState {
                                copy(
                                    isCreatingQuiz = false,
                                    canBrowseDuringGeneration = false,
                                    generationProgress = 1f,
                                )
                            }
                            sendEffect(CreateQuizEffect.QuizCreated(quizSessionId))
                            return
                        }

                        QuizGenerationStatus.Failed -> {
                            val failMessage = status.data.failReason ?: "퀴즈 생성에 실패했어요."
                            updateState {
                                copy(
                                    currentStep = CreateQuizStep.Options,
                                    isCreatingQuiz = false,
                                    canBrowseDuringGeneration = false,
                                    generationFailureMessage = failMessage,
                                )
                            }
                            sendEffect(CreateQuizEffect.QuizGenerationFinished)
                            sendEffect(CreateQuizEffect.ShowMessage(failMessage))
                            return
                        }

                        QuizGenerationStatus.Pending,
                        QuizGenerationStatus.InProgress,
                        QuizGenerationStatus.Unknown,
                        -> Unit
                    }
                }

                is NetworkResult.Failure -> {
                    val failMessage = status.message
                    updateState {
                        copy(
                            currentStep = CreateQuizStep.Options,
                            isCreatingQuiz = false,
                            canBrowseDuringGeneration = false,
                            generationFailureMessage = failMessage,
                        )
                    }
                    sendEffect(CreateQuizEffect.QuizGenerationFinished)
                    sendEffect(CreateQuizEffect.ShowMessage(failMessage))
                    return
                }
            }
        }

        val timeoutMessage = "퀴즈 생성 상태 확인 시간이 초과됐어요."
        updateState {
            copy(
                currentStep = CreateQuizStep.Options,
                isCreatingQuiz = false,
                canBrowseDuringGeneration = false,
                generationFailureMessage = timeoutMessage,
            )
        }
        sendEffect(CreateQuizEffect.QuizGenerationFinished)
        sendEffect(CreateQuizEffect.ShowMessage(timeoutMessage))
    }

    private fun Float.nextVisibleGenerationProgress(
        serverProgress: Float?,
        status: QuizGenerationStatus,
    ): Float {
        val visibleServerProgress = serverProgress
            ?.takeIf { status == QuizGenerationStatus.Completed }
            ?: serverProgress?.coerceAtMost(GENERATION_CLIENT_PROGRESS_MAX)
        val baselineProgress = maxOf(this, visibleServerProgress ?: 0f)

        return when (status) {
            QuizGenerationStatus.Completed -> 1f
            QuizGenerationStatus.Failed -> baselineProgress
            QuizGenerationStatus.Pending,
            QuizGenerationStatus.InProgress,
            QuizGenerationStatus.Unknown,
            -> maxOf(
                baselineProgress,
                (this + GENERATION_CLIENT_PROGRESS_STEP)
                    .coerceAtMost(GENERATION_CLIENT_PROGRESS_MAX),
            )
        }
    }

    private companion object {
        const val GENERATION_POLL_INTERVAL_MILLIS = 1_000L
        const val GENERATION_POLL_MAX_ATTEMPTS = 120
        const val GENERATION_CLIENT_PROGRESS_STEP = 0.03f
        const val GENERATION_CLIENT_PROGRESS_MAX = 0.95f
    }
}

private fun CreateQuizState.toQuizCreateDraft(): QuizCreateDraft {
    return QuizCreateDraft(
        subjectId = selectedSubject?.id,
        partIds = selectedPartIds,
        quizType = selectedQuizType?.toServerQuizTypeOrNull(),
        choiceCount = selectedChoiceCount,
        questionCount = selectedQuestionCountOption.toQuestionCount(customQuestionCount),
        difficulty = selectedDifficulty?.toDomain(),
    )
}

private fun CreateQuizState.createValidationMessage(): String = when {
    selectedSubject == null -> "과목을 선택해주세요."
    selectedPartIds.isEmpty() -> "출제 범위를 선택해주세요."
    selectedQuizType?.toServerQuizTypeOrNull() == null -> "아직 지원하지 않는 퀴즈 유형이에요."
    selectedQuestionCountOption.toQuestionCount(customQuestionCount) == null -> "문제수를 선택해주세요."
    selectedDifficulty == null -> "난이도를 선택해주세요."
    else -> "퀴즈 생성 정보를 확인해주세요."
}

private fun QuizSubjectSummary.toUiModel(): QuizSubjectUiModel = QuizSubjectUiModel(
    id = id,
    name = name,
    chapters = emptyList(),
    chapterCountOverride = chapterCount,
    partCountOverride = partCount,
)

private fun QuizScope.toUiModel(summary: QuizSubjectUiModel): QuizSubjectUiModel = QuizSubjectUiModel(
    id = subjectId,
    name = subjectName.ifBlank { summary.name },
    chapters = chapters
        .sortedBy { chapter -> chapter.displayOrder }
        .map { chapter ->
            QuizScopeChapterUiModel(
                id = chapter.id,
                title = chapter.name,
                chapterNumber = chapter.displayOrder,
                parts = chapter.parts
                    .sortedBy { part -> part.partNumber }
                    .map { part ->
                        QuizScopePartUiModel(
                            id = part.id,
                            title = part.name,
                        )
                    },
            )
        },
    chapterCountOverride = null,
    partCountOverride = null,
)

private fun QuizSubjectUiModel.allPartIds(): List<String> =
    chapters.flatMap { chapter -> chapter.parts.map { part -> part.id } }

private fun List<String>.toggle(id: String): List<String> =
    if (id in this) {
        this - id
    } else {
        this + id
    }

private fun QuizTypeOption.toServerQuizTypeOrNull(): ServerQuizType? = when (this) {
    QuizTypeOption.MultipleChoice -> ServerQuizType.MultipleChoice
    QuizTypeOption.Ox -> ServerQuizType.Ox
    QuizTypeOption.Flashcard,
    QuizTypeOption.ShortAnswer,
    -> null
}

private fun QuizQuestionCountOption?.toQuestionCount(customQuestionCount: Int?): Int? = when (this) {
    QuizQuestionCountOption.Five -> 5
    QuizQuestionCountOption.Ten -> 10
    QuizQuestionCountOption.Twenty -> 20
    QuizQuestionCountOption.Custom -> customQuestionCount
    null -> null
}

private fun QuizDifficultyOption.toDomain(): QuizDifficulty = when (this) {
    QuizDifficultyOption.Easy -> QuizDifficulty.Easy
    QuizDifficultyOption.Normal -> QuizDifficulty.Medium
    QuizDifficultyOption.Hard -> QuizDifficulty.Hard
}
