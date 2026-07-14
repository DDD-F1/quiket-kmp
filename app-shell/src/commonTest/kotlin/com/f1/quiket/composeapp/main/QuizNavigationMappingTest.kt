package com.f1.quiket.composeapp.main

import com.f1.quiket.composeapp.navigation.MainDestination
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import kotlin.test.Test
import kotlin.test.assertEquals

class QuizNavigationMappingTest {
    @Test
    fun launchConfigRoundTripsThroughDestination() {
        val launchConfig = QuizPlayLaunchConfig(
            quizSessionId = "quiz-1",
            clientSessionId = "client-1",
            playSessionId = "play-1",
            playType = QuizPlayType.RetryWrong,
            playMode = QuizPlayMode.OneByOne,
            timerEnabled = true,
            timerScope = QuizTimerScope.PerQuestion,
            timerSeconds = 25,
        )

        assertEquals(launchConfig, launchConfig.toDestination().toLaunchConfig())
    }

    @Test
    fun unknownWireValuesFallBackToSafeQuizDefaults() {
        val launchConfig = MainDestination.QuizPlay(
            quizSessionId = "quiz-1",
            playType = "future_play_type",
            playMode = "future_play_mode",
            timerScope = "future_timer_scope",
        ).toLaunchConfig()

        assertEquals(QuizPlayType.First, launchConfig.playType)
        assertEquals(QuizPlayMode.AllAtOnce, launchConfig.playMode)
        assertEquals(null, launchConfig.timerScope)
    }
}
