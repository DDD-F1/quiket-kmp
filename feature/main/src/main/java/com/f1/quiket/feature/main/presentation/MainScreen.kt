package com.f1.quiket.feature.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.White
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.feature.floating.presentation.navigation.AddSubjectDestination
import com.f1.quiket.feature.floating.presentation.navigation.CreateQuizDestination
import com.f1.quiket.feature.floating.presentation.navigation.ScheduleExamDestination
import com.f1.quiket.feature.floating.presentation.navigation.UploadDestination
import com.f1.quiket.feature.floating.presentation.navigation.lectureSelectGraph
import com.f1.quiket.feature.floating.presentation.navigation.lectureUploadFileGraph
import com.f1.quiket.feature.history.navigation.HistoryDestination
import com.f1.quiket.feature.history.navigation.historyGraph
import com.f1.quiket.feature.home.navigation.HomeDestination
import com.f1.quiket.feature.home.navigation.QuizPlayAllDestination
import com.f1.quiket.feature.home.navigation.QuizResultDestination
import com.f1.quiket.feature.home.navigation.QuizStartDestination
import com.f1.quiket.feature.home.navigation.homeGraph
import com.f1.quiket.feature.mypage.navigation.MyPageDestination
import com.f1.quiket.feature.mypage.navigation.myPageGraph
import com.f1.quiket.feature.review.navigation.ReviewDestination
import com.f1.quiket.feature.review.navigation.reviewGraph

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    var isQuizGenerating by rememberSaveable { mutableStateOf(false) }
    var activeQuizSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainTab.Home.destination.route

    val floatingRoutes = setOf(
        AddSubjectDestination.route,
        ScheduleExamDestination.route,
        CreateQuizDestination.route,
        QuizStartDestination.route,
        QuizPlayAllDestination.route,
        QuizResultDestination.route,
        UploadDestination.route,
    )
    val showBottomBar = currentRoute !in floatingRoutes

    val scaffoldContainerColor = when (currentRoute) {
        HistoryDestination.route, ReviewDestination.route -> Brown50
        else -> White
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = scaffoldContainerColor.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
        }
    }

    Scaffold(
        containerColor = scaffoldContainerColor,
        //topBar = { QuiketTopBar(title = currentTab.label) },
        bottomBar = {
            if (!showBottomBar) return@Scaffold
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.destination.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selected) tab.selectedIconRes else tab.unselectedIconRes
                                ),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Brown950 else Gray400
                            )
                        },
                        // 선택한 Bottom바 색상 제거
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination.route,
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            homeGraph(
                navController = navController,
                isQuizGenerating = isQuizGenerating,
                activeQuizSessionId = activeQuizSessionId,
                onQuizGenerationStarted = {
                    isQuizGenerating = true
                },
                onQuizGenerationFinished = {
                    isQuizGenerating = false
                },
                onQuizCreated = { quizSessionId ->
                    isQuizGenerating = false
                    activeQuizSessionId = quizSessionId
                    navController.navigate(QuizStartDestination.createRoute(quizSessionId)) {
                        popUpTo(CreateQuizDestination.route) {
                            inclusive = true
                        }
                    }
                },
                onQuizPlayCompleted = {
                    isQuizGenerating = false
                    activeQuizSessionId = null
                },
                navigateToQuizStart = { quizSessionId ->
                    navController.navigate(QuizStartDestination.createRoute(quizSessionId ?: activeQuizSessionId))
                },
                navigateToScheduleExam = {
                    navController.navigate(ScheduleExamDestination.route)
                },
                navigateToCreateQuiz = {
                    navController.navigate(CreateQuizDestination.route)
                },
                navigateToUpload = {
                    navController.navigate(UploadDestination.route)
                },
                navigateToAddSubject = {
                    navController.navigate(AddSubjectDestination.route)
                },
                navigateToMyPage = {
                    navController.navigate(MyPageDestination.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
            )
            historyGraph(
                onQuizStartClick = { quizSessionId ->
                    activeQuizSessionId = quizSessionId
                    navController.navigate(QuizStartDestination.createRoute(quizSessionId))
                },
                onQuizResultClick = { resultId ->
                    navController.navigate(QuizResultDestination.createRoute(resultId))
                },
            )
            reviewGraph()
            myPageGraph(navController, onLogout = onLogout)
            lectureSelectGraph(navController = navController, onFinish = { navController.popBackStack() })
            lectureUploadFileGraph(navController = navController, onFinish = { navController.popBackStack() })
        }
    }
}
