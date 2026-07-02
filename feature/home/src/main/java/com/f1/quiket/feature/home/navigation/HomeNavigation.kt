package com.f1.quiket.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.f1.quiket.feature.floating.presentation.navigation.AddSubjectDestination
import com.f1.quiket.feature.floating.presentation.navigation.CreateQuizDestination
import com.f1.quiket.feature.floating.presentation.navigation.ScheduleExamDestination
import com.f1.quiket.feature.floating.presentation.navigation.UploadDestination
import com.f1.quiket.feature.floating.presentation.screen.UploadScreen
import com.f1.quiket.feature.floating.presentation.screen.addsubject.AddSubjectScreen
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.presentation.route.CreateQuizRoute
import com.f1.quiket.feature.home.presentation.route.ExamScheduleRoute
import com.f1.quiket.feature.home.presentation.route.HomeRoute
import com.f1.quiket.feature.home.presentation.screen.QuizPlayAllRoute
import com.f1.quiket.feature.home.presentation.screen.QuizResultRoute
import com.f1.quiket.feature.home.presentation.screen.QuizStartRoute

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    isQuizGenerating: Boolean,
    activeQuizSessionId: String?,
    onQuizGenerationStarted: () -> Unit,
    onQuizGenerationFinished: () -> Unit,
    onQuizCreated: (String) -> Unit,
    onQuizPlayCompleted: () -> Unit,
    navigateToQuizStart: (String?) -> Unit,
    navigateToScheduleExam: () -> Unit,
    navigateToCreateQuiz: () -> Unit,
    navigateToUpload: () -> Unit,
    navigateToAddSubject: () -> Unit,
    navigateToMyPage: () -> Unit,
) {
    composable(route = HomeDestination.route) { backStackEntry ->
        HomeRoute(
            isQuizGenerating = isQuizGenerating,
            activeQuizSessionId = activeQuizSessionId,
            onQuizGenerationFinished = onQuizGenerationFinished,
            navigateToQuizStart = navigateToQuizStart,
            navigateToQuizResult = { resultId ->
                navController.navigate(QuizResultDestination.createRoute(resultId))
            },
            navigateToScheduleExam = navigateToScheduleExam,
            navigateToCreateQuiz = navigateToCreateQuiz,
            navigateToUpload = navigateToUpload,
            navigateToAddSubject = navigateToAddSubject,
            navigateToMyPage = navigateToMyPage,
            navigateToExamScheduleList = { navController.navigate(ExamScheduleDestination.route) },
            homeBackStackEntry = backStackEntry,
        )
    }

    composable(route = ExamScheduleDestination.route) {
        ExamScheduleRoute(
            onBackClick = { navController.popBackStack() },
            onQuizClick = { navController.navigate(CreateQuizDestination.route) },
        )
    }

    composable(ScheduleExamDestination.route) {
        ExamScheduleRoute(
            onBackClick = { navController.popBackStack() },
            onQuizClick = { navController.navigate(CreateQuizDestination.route) },
        )
    }
    composable(CreateQuizDestination.route) { backStackEntry ->
        CreateQuizRoute(
            onBackClick = { navController.popBackStack() },
            onAddSubjectClick = { navController.navigate(AddSubjectDestination.route) },
            onQuizGenerationStarted = onQuizGenerationStarted,
            onQuizGenerationFinished = onQuizGenerationFinished,
            onQuizCreated = onQuizCreated,
            createQuizBackStackEntry = backStackEntry,
        )
    }
    composable(
        route = QuizStartDestination.route,
        arguments = listOf(
            navArgument(QuizStartDestination.ARG_QUIZ_SESSION_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val quizSessionId = backStackEntry.arguments
            ?.getString(QuizStartDestination.ARG_QUIZ_SESSION_ID)
        QuizStartRoute(
            quizSessionId = quizSessionId,
            onBackClick = { navController.popBackStack() },
            onStartClick = { config ->
                navController.navigate(
                    QuizPlayAllDestination.createRoute(
                        quizSessionId = quizSessionId,
                        playMode = config.playMode,
                        timerEnabled = config.timerEnabled,
                        timerScope = config.timerScope,
                        timerSeconds = config.timerSeconds,
                    ),
                )
            },
        )
    }
    composable(
        route = QuizPlayAllDestination.route,
        arguments = listOf(
            navArgument(QuizPlayAllDestination.ARG_QUIZ_SESSION_ID) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(QuizPlayAllDestination.ARG_PLAY_MODE) {
                type = NavType.StringType
                defaultValue = QuizPlayMode.AllAtOnce.wireValue
            },
            navArgument(QuizPlayAllDestination.ARG_TIMER_ENABLED) {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument(QuizPlayAllDestination.ARG_TIMER_SCOPE) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(QuizPlayAllDestination.ARG_TIMER_SECONDS) {
                type = NavType.IntType
                defaultValue = -1
            },
            navArgument(QuizPlayAllDestination.ARG_CLIENT_SESSION_ID) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(QuizPlayAllDestination.ARG_PLAY_SESSION_ID) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(QuizPlayAllDestination.ARG_PLAY_TYPE) {
                type = NavType.StringType
                defaultValue = QuizPlayType.First.wireValue
            },
        ),
    ) { backStackEntry ->
        val quizSessionId = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_QUIZ_SESSION_ID)
            ?.takeIf { id -> id.isNotBlank() }
        val playMode = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_PLAY_MODE)
            .toQuizPlayMode()
        val timerEnabled = backStackEntry.arguments
            ?.getBoolean(QuizPlayAllDestination.ARG_TIMER_ENABLED)
            ?: false
        val timerScope = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_TIMER_SCOPE)
            .toQuizTimerScope()
        val timerSeconds = backStackEntry.arguments
            ?.getInt(QuizPlayAllDestination.ARG_TIMER_SECONDS)
            ?.takeIf { seconds -> seconds > 0 }
        val clientSessionId = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_CLIENT_SESSION_ID)
            ?.takeIf { it.isNotBlank() }
        val playSessionId = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_PLAY_SESSION_ID)
            ?.takeIf { it.isNotBlank() }
        val playType = backStackEntry.arguments
            ?.getString(QuizPlayAllDestination.ARG_PLAY_TYPE)
            .toQuizPlayType()
        QuizPlayAllRoute(
            quizSessionId = quizSessionId,
            playMode = playMode,
            timerEnabled = timerEnabled,
            timerScope = timerScope,
            timerSeconds = timerSeconds,
            clientSessionId = clientSessionId,
            playSessionId = playSessionId,
            playType = playType,
            onCloseClick = { navController.popBackStack() },
            onResultReady = { resultId ->
                onQuizPlayCompleted()
                navController.navigate(QuizResultDestination.createRoute(resultId)) {
                    popUpTo(HomeDestination.route)
                }
            },
        )
    }
    composable(
        route = QuizResultDestination.route,
        arguments = listOf(
            navArgument(QuizResultDestination.ARG_RESULT_ID) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val resultId = backStackEntry.arguments
            ?.getString(QuizResultDestination.ARG_RESULT_ID)
            .orEmpty()
        QuizResultRoute(
            resultId = resultId,
            onBackClick = {
                navController.popBackStack()
            },
            onRetryReady = { config ->
                navController.navigate(
                    QuizPlayAllDestination.createRoute(
                        quizSessionId = config.quizSessionId,
                        playMode = config.playMode,
                        timerEnabled = config.timerEnabled,
                        timerScope = config.timerScope,
                        timerSeconds = config.timerSeconds,
                        clientSessionId = config.clientSessionId,
                        playSessionId = config.playSessionId,
                        playType = config.playType,
                    ),
                )
            },
        )
    }
    composable(UploadDestination.route) {
        UploadScreen(
            onBackClick = { navController.popBackStack() },
            onNextClick = { navController.popBackStack() },
        )
    }
    composable(AddSubjectDestination.route) { backStackEntry ->
        AddSubjectScreen(
            onDismiss = { navController.popBackStack() },
            onFinish = {
                runCatching {
                    navController.getBackStackEntry(CreateQuizDestination.route)
                        .savedStateHandle
                        .set("subject_created", true)
                }
                runCatching {
                    navController.getBackStackEntry(HomeDestination.route)
                        .savedStateHandle
                        .set("subject_created", true)
                }
                navController.popBackStack()
            },
        )
    }
}

private fun String?.toQuizPlayMode(): QuizPlayMode =
    QuizPlayMode.entries.firstOrNull { mode -> mode.wireValue == this }
        ?: QuizPlayMode.AllAtOnce

private fun String?.toQuizTimerScope(): QuizTimerScope? =
    QuizTimerScope.entries.firstOrNull { scope -> scope.wireValue == this }

private fun String?.toQuizPlayType(): QuizPlayType =
    QuizPlayType.entries.firstOrNull { type -> type.wireValue == this }
        ?: QuizPlayType.First
