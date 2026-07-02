package com.f1.quiket.feature.home.navigation

import com.f1.quiket.core.navigation.QuiketDestination
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizTimerScope

data object HomeDestination : QuiketDestination {
    override val route: String = "main/home"
}

data object QuizStartDestination : QuiketDestination {
    const val ARG_QUIZ_SESSION_ID = "quizSessionId"
    const val BASE_ROUTE = "home/quiz-start"
    override val route: String = "$BASE_ROUTE?$ARG_QUIZ_SESSION_ID={$ARG_QUIZ_SESSION_ID}"

    fun createRoute(quizSessionId: String?): String =
        if (quizSessionId.isNullOrBlank()) {
            BASE_ROUTE
        } else {
            "$BASE_ROUTE?$ARG_QUIZ_SESSION_ID=$quizSessionId"
        }
}

data object QuizPlayAllDestination : QuiketDestination {
    const val ARG_QUIZ_SESSION_ID = "quizSessionId"
    const val ARG_PLAY_MODE = "playMode"
    const val ARG_TIMER_ENABLED = "timerEnabled"
    const val ARG_TIMER_SCOPE = "timerScope"
    const val ARG_TIMER_SECONDS = "timerSeconds"
    const val ARG_CLIENT_SESSION_ID = "clientSessionId"
    const val ARG_PLAY_SESSION_ID = "playSessionId"
    const val ARG_PLAY_TYPE = "playType"
    const val BASE_ROUTE = "home/quiz-play/all"
    override val route: String = "$BASE_ROUTE?" +
        "$ARG_QUIZ_SESSION_ID={$ARG_QUIZ_SESSION_ID}&" +
        "$ARG_PLAY_MODE={$ARG_PLAY_MODE}&" +
        "$ARG_TIMER_ENABLED={$ARG_TIMER_ENABLED}&" +
        "$ARG_TIMER_SCOPE={$ARG_TIMER_SCOPE}&" +
        "$ARG_TIMER_SECONDS={$ARG_TIMER_SECONDS}&" +
        "$ARG_CLIENT_SESSION_ID={$ARG_CLIENT_SESSION_ID}&" +
        "$ARG_PLAY_SESSION_ID={$ARG_PLAY_SESSION_ID}&" +
        "$ARG_PLAY_TYPE={$ARG_PLAY_TYPE}"

    fun createRoute(
        quizSessionId: String?,
        playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
        timerEnabled: Boolean = false,
        timerScope: QuizTimerScope? = null,
        timerSeconds: Int? = null,
        clientSessionId: String? = null,
        playSessionId: String? = null,
        playType: QuizPlayType = QuizPlayType.First,
    ): String = BASE_ROUTE +
        "?$ARG_QUIZ_SESSION_ID=${quizSessionId.orEmpty()}" +
        "&$ARG_PLAY_MODE=${playMode.wireValue}" +
        "&$ARG_TIMER_ENABLED=$timerEnabled" +
        "&$ARG_TIMER_SCOPE=${timerScope?.wireValue.orEmpty()}" +
        "&$ARG_TIMER_SECONDS=${timerSeconds ?: -1}" +
        "&$ARG_CLIENT_SESSION_ID=${clientSessionId.orEmpty()}" +
        "&$ARG_PLAY_SESSION_ID=${playSessionId.orEmpty()}" +
        "&$ARG_PLAY_TYPE=${playType.wireValue}"
}

data object QuizResultDestination : QuiketDestination {
    const val ARG_RESULT_ID = "resultId"
    const val BASE_ROUTE = "home/quiz-result"
    override val route: String = "$BASE_ROUTE/{$ARG_RESULT_ID}"

    fun createRoute(resultId: String): String = "$BASE_ROUTE/$resultId"
}

data object ExamScheduleDestination : QuiketDestination {
    override val route: String = "home/exam-schedule"
}
