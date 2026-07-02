package com.f1.quiket.feature.home.domain.usecase

import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.ServerQuizType
import javax.inject.Inject

class BuildQuizCreateUseCase @Inject constructor() {
    operator fun invoke(draft: QuizCreateDraft): QuizCreate? {
        val subjectId = draft.subjectId?.takeIf { id -> id.isNotBlank() } ?: return null
        val quizType = draft.quizType ?: return null
        val questionCount = draft.questionCount ?: return null
        val difficulty = draft.difficulty ?: return null
        if (draft.partIds.isEmpty()) return null

        val timerSeconds = draft.timerSeconds
            ?.takeIf { draft.timerEnabled }
            ?.coerceAtLeast(MIN_TIMER_SECONDS)

        return QuizCreate(
            subjectId = subjectId,
            partIds = draft.partIds,
            quizType = quizType,
            choiceCount = if (quizType == ServerQuizType.MultipleChoice) {
                draft.choiceCount ?: DEFAULT_CHOICE_COUNT
            } else {
                null
            },
            questionCount = questionCount.coerceIn(MIN_QUESTION_COUNT, MAX_QUESTION_COUNT),
            playMode = draft.playMode,
            timerEnabled = timerSeconds != null,
            timerScope = timerSeconds?.let { draft.timerScope },
            timerSeconds = timerSeconds,
            difficulty = difficulty,
        )
    }

    private companion object {
        const val DEFAULT_CHOICE_COUNT = 4
        const val MIN_QUESTION_COUNT = 1
        const val MAX_QUESTION_COUNT = 100
        const val MIN_TIMER_SECONDS = 1
    }
}

data class QuizCreateDraft(
    val subjectId: String?,
    val partIds: List<String>,
    val quizType: ServerQuizType?,
    val choiceCount: Int?,
    val questionCount: Int?,
    val difficulty: QuizDifficulty?,
    val playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    val timerEnabled: Boolean = false,
    val timerScope: QuizTimerScope? = null,
    val timerSeconds: Int? = null,
)
