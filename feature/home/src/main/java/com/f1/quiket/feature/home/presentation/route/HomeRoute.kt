package com.f1.quiket.feature.home.presentation.route

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*
import com.f1.quiket.feature.home.presentation.screen.*

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.f1.quiket.feature.home.presentation.model.FabAction
import kotlinx.coroutines.delay

private const val MAX_EXAM_COUNT = 5
private const val EXAM_LIMIT_MESSAGE = "시험은 최대 5개까지 등록할 수 있어요. 기존 일정을 삭제 후 등록해주세요"

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    isQuizGenerating: Boolean,
    activeQuizSessionId: String?,
    onQuizGenerationFinished: () -> Unit,
    navigateToQuizStart: (String?) -> Unit,
    navigateToQuizResult: (String) -> Unit,
    navigateToScheduleExam: () -> Unit,
    navigateToCreateQuiz: () -> Unit,
    navigateToUpload: () -> Unit,
    navigateToAddSubject: () -> Unit,
    navigateToMyPage: () -> Unit,
    navigateToExamScheduleList: () -> Unit = {},
    homeBackStackEntry: NavBackStackEntry? = null,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(HomeIntent.LoadHomeData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val subjectCreatedFlow = remember(homeBackStackEntry) {
        homeBackStackEntry?.savedStateHandle?.getStateFlow("subject_created", false)
            ?: MutableStateFlow(false)
    }
    val subjectCreated by subjectCreatedFlow.collectAsStateWithLifecycle()

    LaunchedEffect(subjectCreated) {
        if (subjectCreated) {
            homeBackStackEntry?.savedStateHandle?.set("subject_created", false)
            viewModel.onIntent(HomeIntent.LoadHomeData)
        }
    }
    val serverActiveQuizSessionId = uiState.homeData
        ?.hero
        ?.takeIf { hero -> hero.hasActiveQuiz }
        ?.activeQuiz
        ?.quizSessionId
    val effectiveActiveQuizSessionId = serverActiveQuizSessionId ?: activeQuizSessionId
    val isWaitingQuizGeneration = isQuizGenerating && effectiveActiveQuizSessionId == null

    LaunchedEffect(isWaitingQuizGeneration) {
        if (!isWaitingQuizGeneration) return@LaunchedEffect
        repeat(HOME_GENERATION_POLL_MAX_ATTEMPTS) {
            delay(HOME_GENERATION_POLL_INTERVAL_MILLIS)
            viewModel.onIntent(HomeIntent.LoadHomeData)
        }
        if (isWaitingQuizGeneration) {
            onQuizGenerationFinished()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        isQuizGenerating = isWaitingQuizGeneration,
        hasActiveQuizSession = effectiveActiveQuizSessionId != null,
        onBoardingDone = {
            viewModel.onIntent(
                HomeIntent.OnboardingDoneClick
            )
        },
        onQuizCardClick = {
            effectiveActiveQuizSessionId?.let(navigateToQuizStart)
        },
        onQuizActionClick = {
            when {
                effectiveActiveQuizSessionId != null -> navigateToQuizStart(effectiveActiveQuizSessionId)
                isWaitingQuizGeneration -> Unit
                else -> navigateToCreateQuiz()
            }
        },
        onQuizResultClick = navigateToQuizResult,
        onHomeRefresh = { viewModel.onIntent(HomeIntent.LoadHomeData) },
        onFabItemClick = { action ->
            when (action) {
                FabAction.ScheduleExam -> {
                    val examCount = uiState.homeData?.subjects
                        .orEmpty()
                        .mapNotNull { it.examSchedule }
                        .filter { (it.dDay ?: 0) >= 0 }
                        .distinctBy { it.subjectId }
                        .size
                    if (examCount >= MAX_EXAM_COUNT) {
                        Toast.makeText(context, EXAM_LIMIT_MESSAGE, Toast.LENGTH_LONG).show()
                        navigateToExamScheduleList()
                    } else {
                        navigateToScheduleExam()
                    }
                }
                FabAction.CreateQuiz -> navigateToCreateQuiz()
                FabAction.Upload -> navigateToUpload()
                FabAction.AddSubject -> navigateToAddSubject()
            }
        },
        onProfileClick = navigateToMyPage,
        onExamCardClick = navigateToExamScheduleList,
    )
}

private const val HOME_GENERATION_POLL_INTERVAL_MILLIS = 3_000L
private const val HOME_GENERATION_POLL_MAX_ATTEMPTS = 120
