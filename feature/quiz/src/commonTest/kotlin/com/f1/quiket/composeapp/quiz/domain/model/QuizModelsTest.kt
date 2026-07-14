package com.f1.quiket.composeapp.quiz.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizModelsTest {
    @Test
    fun optionMatchesServerIdentifiersAndOxAliases() {
        val option = QuizOption(
            id = "option-o",
            optionNumber = 1,
            content = "O",
            value = "O",
        )

        assertTrue(option.matchesAnswerValue("option-o") == true)
        assertTrue(option.matchesAnswerValue("1") == true)
        assertTrue(option.matchesAnswerValue("true") == true)
        assertTrue(option.matchesAnswerValue("맞다") == true)
        assertEquals(false, option.matchesAnswerValue("false"))
        assertNull(option.matchesAnswerValue("  "))
    }

    @Test
    fun oxTypeBuildsStableDefaultOptions() {
        val options = ServerQuizType.Ox.defaultOptions(questionId = "question-1")

        assertEquals(listOf("question-1-ox-o", "question-1-ox-x"), options.map { it.id })
        assertEquals(listOf("O", "X"), options.map { it.value })
        assertEquals(emptyList(), ServerQuizType.MultipleChoice.defaultOptions("question-1"))
    }

    @Test
    fun playSessionLaunchConfigUsesServerSessionConfiguration() {
        val playSession = QuizPlaySession(
            playSessionId = "play-1",
            clientSessionId = "client-1",
            quizSessionId = "fallback-session",
            playType = QuizPlayType.RetryWrong,
            status = "in_progress",
            quizSession = quizSession(id = "server-session"),
        )

        val config = playSession.toLaunchConfig()

        assertEquals("server-session", config.quizSessionId)
        assertEquals("client-1", config.clientSessionId)
        assertEquals("play-1", config.playSessionId)
        assertEquals(QuizPlayType.RetryWrong, config.playType)
        assertEquals(QuizPlayMode.OneByOne, config.playMode)
        assertTrue(config.timerEnabled)
        assertEquals(QuizTimerScope.Total, config.timerScope)
        assertEquals(300, config.timerSeconds)
    }

    @Test
    fun playSessionLaunchConfigFallsBackWhenNestedSessionIsMissing() {
        val config = QuizPlaySession(
            playSessionId = "play-1",
            clientSessionId = "client-1",
            quizSessionId = "fallback-session",
            playType = QuizPlayType.First,
            status = "ready",
            quizSession = null,
        ).toLaunchConfig()

        assertEquals("fallback-session", config.quizSessionId)
        assertEquals(QuizPlayMode.AllAtOnce, config.playMode)
        assertFalse(config.timerEnabled)
        assertNull(config.timerScope)
        assertNull(config.timerSeconds)
    }
}

private fun quizSession(id: String): QuizSession = QuizSession(
    id = id,
    subjectId = "subject-1",
    subjectName = "SQLD",
    quizType = ServerQuizType.MultipleChoice,
    choiceCount = 4,
    questionCount = 10,
    playMode = QuizPlayMode.OneByOne,
    timerEnabled = true,
    timerScope = QuizTimerScope.Total,
    timerSeconds = 300,
    difficulty = QuizDifficulty.Medium,
    status = "ready",
    questions = emptyList(),
)
