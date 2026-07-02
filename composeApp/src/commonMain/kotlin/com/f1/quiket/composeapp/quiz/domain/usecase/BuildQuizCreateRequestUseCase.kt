package com.f1.quiket.composeapp.quiz.domain.usecase

import com.f1.quiket.composeapp.quiz.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.QuizDifficulty
import com.f1.quiket.composeapp.quiz.QuizPlayMode
import com.f1.quiket.composeapp.quiz.QuizTimerScope
import com.f1.quiket.composeapp.quiz.ServerQuizType

internal class BuildQuizCreateRequestUseCase {
    operator fun invoke(
        subjectId: String,
        partIds: List<String>,
        quizType: ServerQuizType,
        choiceCount: Int?,
        questionCount: Int,
        difficulty: QuizDifficulty,
        playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
        timerEnabled: Boolean = false,
        timerScope: QuizTimerScope? = null,
        timerSeconds: Int? = null,
    ): QuizCreateRequest =
        QuizCreateRequest(
            subjectId = subjectId,
            partIds = partIds,
            quizType = quizType,
            choiceCount = if (quizType == ServerQuizType.MultipleChoice) choiceCount else null,
            questionCount = questionCount,
            playMode = playMode,
            timerEnabled = timerEnabled,
            timerScope = timerScope,
            timerSeconds = timerSeconds,
            difficulty = difficulty,
        )
}
