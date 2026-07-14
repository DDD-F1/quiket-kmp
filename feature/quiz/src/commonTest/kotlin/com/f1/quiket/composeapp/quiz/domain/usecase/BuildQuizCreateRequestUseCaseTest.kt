package com.f1.quiket.composeapp.quiz.domain.usecase

import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BuildQuizCreateRequestUseCaseTest {
    private val buildRequest = BuildQuizCreateRequestUseCase()

    @Test
    fun multipleChoiceRequestPreservesChoiceAndTimerConfiguration() {
        val request = buildRequest(
            subjectId = "subject-1",
            partIds = listOf("part-1", "part-2"),
            quizType = ServerQuizType.MultipleChoice,
            choiceCount = 4,
            questionCount = 10,
            difficulty = QuizDifficulty.Hard,
            playMode = QuizPlayMode.OneByOne,
            timerEnabled = true,
            timerScope = QuizTimerScope.PerQuestion,
            timerSeconds = 30,
        )

        assertEquals(4, request.choiceCount)
        assertEquals(QuizPlayMode.OneByOne, request.playMode)
        assertEquals(QuizTimerScope.PerQuestion, request.timerScope)
        assertEquals(30, request.timerSeconds)
        assertTrue(request.timerEnabled)
    }

    @Test
    fun oxRequestClearsIrrelevantChoiceCount() {
        val request = buildRequest(
            subjectId = "subject-1",
            partIds = listOf("part-1"),
            quizType = ServerQuizType.Ox,
            choiceCount = 4,
            questionCount = 5,
            difficulty = QuizDifficulty.Easy,
        )

        assertNull(request.choiceCount)
        assertEquals(ServerQuizType.Ox, request.quizType)
    }
}
