package com.f1.quiket.feature.home.presentation.contract

import com.f1.quiket.core.common.mvi.UiEffect
import com.f1.quiket.core.common.mvi.UiIntent
import com.f1.quiket.core.common.mvi.UiState

data class CreateQuizState(
    val subjects: List<QuizSubjectUiModel> = emptyList(),
    val isLoadingSubjects: Boolean = false,
    val loadingScopeSubjectId: String? = null,
    val loadedScopeSubjectIds: Set<String> = emptySet(),
    val currentStep: CreateQuizStep = CreateQuizStep.Subject,
    val selectedSubjectId: String? = null,
    val expandedChapterId: String? = null,
    val selectedPartIds: List<String> = emptyList(),
    val shouldAutoSelectParts: Boolean = false,
    val selectedQuizType: QuizTypeOption? = null,
    val selectedChoiceCount: Int? = null,
    val selectedQuestionCountOption: QuizQuestionCountOption? = null,
    val customQuestionCount: Int? = null,
    val customQuestionCountDialogVisible: Boolean = false,
    val customQuestionCountText: String = "",
    val selectedDifficulty: QuizDifficultyOption? = null,
    val isCreatingQuiz: Boolean = false,
    val canBrowseDuringGeneration: Boolean = false,
    val generationProgress: Float = 0f,
    val generationFailureMessage: String? = null,
    val rewardCount: Int = 10,
) : UiState {
    val selectedSubject: QuizSubjectUiModel?
        get() = subjects.firstOrNull { subject -> subject.id == selectedSubjectId }

    val selectedChapterCount: Int
        get() {
            val selectedPartIdSet = selectedPartIds.toSet()
            return selectedSubject?.chapters?.count { chapter ->
                chapter.parts.any { part -> part.id in selectedPartIdSet }
            } ?: 0
        }

    val selectedPartCount: Int
        get() = selectedPartIds.size
}

sealed interface CreateQuizIntent : UiIntent {
    data object LoadSubjects : CreateQuizIntent
    data class LoadQuizScope(val subjectId: String) : CreateQuizIntent
    data class SelectSubject(val subjectId: String) : CreateQuizIntent
    data object SubjectNextClick : CreateQuizIntent
    data object ScopeBackClick : CreateQuizIntent
    data class ToggleChapterExpansion(val chapterId: String) : CreateQuizIntent
    data class ToggleChapterSelection(val chapterId: String) : CreateQuizIntent
    data class TogglePartSelection(val partId: String) : CreateQuizIntent
    data object ClearAllParts : CreateQuizIntent
    data object ScopeNextClick : CreateQuizIntent
    data object OptionsBackClick : CreateQuizIntent
    data class SelectQuizType(val quizType: QuizTypeOption) : CreateQuizIntent
    data class SelectChoiceCount(val count: Int) : CreateQuizIntent
    data class SelectQuestionCount(val option: QuizQuestionCountOption) : CreateQuizIntent
    data object OpenCustomQuestionCountDialog : CreateQuizIntent
    data class ChangeCustomQuestionCountText(val text: String) : CreateQuizIntent
    data object DismissCustomQuestionCountDialog : CreateQuizIntent
    data object ApplyCustomQuestionCount : CreateQuizIntent
    data class SelectDifficulty(val difficulty: QuizDifficultyOption) : CreateQuizIntent
    data object CreateQuiz : CreateQuizIntent
    data object DismissGenerationFailure : CreateQuizIntent
    data object BackClick : CreateQuizIntent
}

sealed interface CreateQuizEffect : UiEffect {
    data class ShowMessage(val message: String) : CreateQuizEffect
    data object QuizGenerationStarted : CreateQuizEffect
    data object QuizGenerationFinished : CreateQuizEffect
    data class QuizCreated(val quizSessionId: String) : CreateQuizEffect
}

enum class CreateQuizStep {
    Subject,
    Scope,
    Options,
    Loading,
}

data class QuizSubjectUiModel(
    val id: String,
    val name: String,
    val chapters: List<QuizScopeChapterUiModel>,
    val chapterCountOverride: Int? = null,
    val partCountOverride: Int? = null,
) {
    val chapterCount: Int
        get() = chapterCountOverride ?: chapters.size

    val partCount: Int
        get() = partCountOverride ?: chapters.sumOf { chapter -> chapter.parts.size }
}

data class QuizScopeChapterUiModel(
    val id: String,
    val title: String,
    val chapterNumber: Int,
    val parts: List<QuizScopePartUiModel>,
)

data class QuizScopePartUiModel(
    val id: String,
    val title: String,
)

enum class QuizTypeOption(
    val title: String,
    val requiresChoiceCount: Boolean,
) {
    MultipleChoice("객관식", true),
    Ox("O/X 퀴즈", false),
    Flashcard("플래시카드", false),
    ShortAnswer("쪽지시험", false),
}

enum class QuizQuestionCountOption(
    val title: String,
) {
    Five("5"),
    Ten("10"),
    Twenty("20"),
    Custom("직접 입력");

    companion object {
        val presets: List<QuizQuestionCountOption> = listOf(Five, Ten, Twenty)
    }
}

enum class QuizDifficultyOption(
    val title: String,
) {
    Easy("쉬움"),
    Normal("보통"),
    Hard("어려움"),
}
