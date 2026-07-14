package com.f1.quiket.composeapp.main

import org.koin.compose.koinInject
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketTopBar
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.history.domain.model.HistoryActivityType
import com.f1.quiket.composeapp.home.ExamScheduleRoute
import com.f1.quiket.composeapp.home.domain.model.RecentActivity
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import com.f1.quiket.composeapp.home.presentation.HomeExamUiModel
import com.f1.quiket.composeapp.home.presentation.HomeErrorCard
import com.f1.quiket.composeapp.home.presentation.HomeSummaryCard
import com.f1.quiket.composeapp.home.presentation.HomeTutorialOverlay
import com.f1.quiket.composeapp.home.presentation.HomeTutorialPage
import com.f1.quiket.composeapp.home.presentation.HomeUiState
import com.f1.quiket.composeapp.home.presentation.HomeUploadSubjectPickerScreen
import com.f1.quiket.composeapp.home.presentation.dDayLabel
import com.f1.quiket.composeapp.home.presentation.toHomeExams
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.LegalTextScreen
import com.f1.quiket.composeapp.mypage.AccountSettingsRoute
import com.f1.quiket.composeapp.mypage.InquiryRoute
import com.f1.quiket.composeapp.mypage.MyPageSettingsScreen
import com.f1.quiket.composeapp.mypage.NotificationSettingsRoute
import com.f1.quiket.composeapp.legal.PrivacyPolicyRawText
import com.f1.quiket.composeapp.legal.ServiceTermsRawText
import com.f1.quiket.composeapp.history.presentation.HistoryTab
import com.f1.quiket.composeapp.main.presentation.MainBottomBar
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import com.f1.quiket.composeapp.main.presentation.MainTab
import com.f1.quiket.composeapp.mypage.presentation.MyPageDashboardTab
import com.f1.quiket.composeapp.mypage.presentation.MyPageUiState
import com.f1.quiket.composeapp.navigation.AppNavigationSavedStateConfiguration
import com.f1.quiket.composeapp.navigation.MainDestination
import com.f1.quiket.composeapp.navigation.replaceAllWith
import com.f1.quiket.composeapp.navigation.replaceTopWith
import com.f1.quiket.composeapp.quiz.QuizCreateRoute
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.QuizPlayRoute
import com.f1.quiket.composeapp.quiz.QuizStartRoute
import com.f1.quiket.composeapp.result.QuizResultRoute
import com.f1.quiket.composeapp.review.ReviewRoute
import com.f1.quiket.composeapp.subject.SubjectCreateRoute
import com.f1.quiket.composeapp.subject.SubjectDetailRoute
import com.f1.quiket.composeapp.util.generateUuid
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.appshell.resources.Res
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_home_make
import com.f1.quiket.core.designsystem.resources.ic_home_upload
import com.f1.quiket.core.designsystem.resources.ic_next
import com.f1.quiket.core.designsystem.resources.ic_qring_profile
import com.f1.quiket.appshell.resources.ic_floating_close
import com.f1.quiket.appshell.resources.ic_floating_plus
import com.f1.quiket.appshell.resources.ic_home_guide_tooltip
import com.f1.quiket.appshell.resources.ic_home_guide_tooltip_close
import com.f1.quiket.appshell.resources.ic_small_floting_add
import com.f1.quiket.appshell.resources.ic_small_floting_quiz
import com.f1.quiket.appshell.resources.ic_small_floting_test
import com.f1.quiket.appshell.resources.ic_small_floting_upload
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val HomeGenerationPollIntervalMillis = 1_000L
private const val HomeGenerationPollMaxAttempts = 120

@Composable
internal fun MainScreen(
    nickname: String?,
    initialHomeGuideCompleted: Boolean,
    onLogout: () -> Unit,
    onProfileChanged: (MyProfile) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainBackStack = rememberNavBackStack(
        AppNavigationSavedStateConfiguration,
        MainDestination.Home,
    )
    var homeReloadKey by remember { mutableStateOf(0) }
    var isQuizGenerationWaiting by remember { mutableStateOf(false) }
    var consumedUploadRequestId by rememberSaveable { mutableStateOf<String?>(null) }
    val stateHolder = koinInject<MainStateHolder>()
    val homeState = stateHolder.homeState
    val historyState = stateHolder.historyState
    val myPageState = stateHolder.myPageState
    val currentNickname by rememberUpdatedState(nickname)
    val currentOnLogout by rememberUpdatedState(onLogout)
    val currentOnProfileChanged by rememberUpdatedState(onProfileChanged)
    val currentOnSessionExpired by rememberUpdatedState(onSessionExpired)
    val coroutineScope = rememberCoroutineScope()
    val currentDestination = mainBackStack.last() as MainDestination
    val selectedTab = mainBackStack.firstOrNull().toMainTab()
    val containerColor = when (selectedTab) {
        MainTab.History, MainTab.Review -> QuiketBrown50
        else -> QuiketWhite
    }
    val isStandaloneDestination = when (currentDestination) {
        is MainDestination.QuizPlay,
        is MainDestination.QuizStart,
        is MainDestination.QuizResult,
        MainDestination.QuizCreate,
        MainDestination.SubjectCreate,
        MainDestination.HomeUploadPicker,
        -> true

        else -> false
    }
    val usesTabStatusBarPadding = when (currentDestination) {
        MainDestination.Home,
        MainDestination.History,
        MainDestination.Review,
        MainDestination.MyPage,
        -> true

        else -> false
    }

    val activeHomeQuizSessionId = (homeState as? HomeUiState.Success)
        ?.data
        ?.hero
        ?.activeQuiz
        ?.quizSessionId

    LaunchedEffect(initialHomeGuideCompleted) {
        stateHolder.setInitialHomeGuideCompleted(initialHomeGuideCompleted)
        stateHolder.loadHomeGuideCompleted()
    }

    LaunchedEffect(activeHomeQuizSessionId, isQuizGenerationWaiting) {
        if (isQuizGenerationWaiting && !activeHomeQuizSessionId.isNullOrBlank()) {
            isQuizGenerationWaiting = false
        }
    }

    LaunchedEffect(isQuizGenerationWaiting) {
        if (!isQuizGenerationWaiting) return@LaunchedEffect
        repeat(HomeGenerationPollMaxAttempts) {
            delay(HomeGenerationPollIntervalMillis)
            homeReloadKey += 1
        }
        if (isQuizGenerationWaiting) {
            isQuizGenerationWaiting = false
        }
    }

    LaunchedEffect(homeReloadKey, isStandaloneDestination) {
        if (!isStandaloneDestination) {
            stateHolder.loadHome(onSessionExpired = currentOnSessionExpired)
        }
    }

    fun loadHistory(reset: Boolean) {
        coroutineScope.launch {
            stateHolder.loadHistory(
                reset = reset,
                onSessionExpired = currentOnSessionExpired,
            )
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.History && !historyState.hasLoaded) {
            loadHistory(reset = true)
        }
    }

    fun loadMyPage() {
        coroutineScope.launch {
            stateHolder.loadMyPage(onSessionExpired = currentOnSessionExpired)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.MyPage && myPageState == MyPageUiState.Idle) {
            loadMyPage()
        }
    }

    fun popMainDestination() {
        if (mainBackStack.size > 1) {
            mainBackStack.removeAt(mainBackStack.lastIndex)
        }
    }

    fun switchMainTab(tab: MainTab) {
        mainBackStack.replaceAllWith(tab.toDestination())
    }

    fun showCreatedSubject(subjectId: String, subjectName: String) {
        mainBackStack.replaceTopWith(
            MainDestination.SubjectDetail(
                subjectId = subjectId,
                subjectName = subjectName,
            ),
        )
        val previousIndex = mainBackStack.lastIndex - 1
        if (previousIndex > 0 && mainBackStack[previousIndex] is MainDestination.SubjectDetail) {
            mainBackStack.removeAt(previousIndex)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = containerColor,
        bottomBar = {
            if (!isStandaloneDestination) {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onTabClick = ::switchMainTab,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (usesTabStatusBarPadding) {
                        Modifier.statusBarsPadding()
                    } else {
                        Modifier
                    },
                )
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(containerColor),
        ) {
            NavDisplay(
                backStack = mainBackStack,
                modifier = Modifier.fillMaxSize(),
                onBack = ::popMainDestination,
                entryProvider = { key ->
                    NavEntry(key) { entry ->
                        when (val destination = entry as MainDestination) {
                            MainDestination.Home -> HomeTab(
                                nickname = currentNickname,
                                homeState = stateHolder.homeState,
                                isQuizGenerationWaiting = isQuizGenerationWaiting,
                                starredSubjectIds = stateHolder.starredSubjectIds,
                                showGuideTooltip = !stateHolder.homeGuideCompleted,
                                onGuideTooltipDismiss = {
                                    if (!stateHolder.homeGuideCompleted) {
                                        coroutineScope.launch {
                                            stateHolder.dismissHomeGuide()
                                        }
                                    }
                                },
                                onRetry = { homeReloadKey += 1 },
                                onSubjectClick = { subject ->
                                    mainBackStack.add(
                                        MainDestination.SubjectDetail(
                                            subjectId = subject.id,
                                            subjectName = subject.name,
                                        ),
                                    )
                                },
                                onSubjectStarToggle = stateHolder::toggleStarredSubject,
                                onRecentActivityClick = { activity ->
                                    val resultId = activity.resultDetailId
                                    val quizStartId = activity.quizStartId
                                    when {
                                        resultId != null -> {
                                            mainBackStack.add(MainDestination.QuizResult(resultId))
                                        }

                                        quizStartId != null -> {
                                            mainBackStack.add(MainDestination.QuizStart(quizStartId))
                                        }
                                    }
                                },
                                onCreateQuizClick = {
                                    mainBackStack.add(MainDestination.QuizCreate)
                                },
                                onUploadClick = {
                                    mainBackStack.add(MainDestination.HomeUploadPicker)
                                },
                                onExamScheduleClick = {
                                    mainBackStack.add(MainDestination.ExamSchedule)
                                },
                                onAddSubjectClick = {
                                    mainBackStack.add(MainDestination.SubjectCreate)
                                },
                            )

                            MainDestination.History -> HistoryTab(
                                state = stateHolder.historyState,
                                onRefresh = { loadHistory(reset = true) },
                                onLoadMore = { loadHistory(reset = false) },
                                onActivityClick = { activity ->
                                    val resultId = activity.resultId ?: activity.playSessionId
                                    val quizSessionId = activity.quizSessionId
                                    when {
                                        activity.activityType == HistoryActivityType.QuizCompleted &&
                                            resultId != null -> {
                                            mainBackStack.add(MainDestination.QuizResult(resultId))
                                        }

                                        activity.activityType in setOf(
                                            HistoryActivityType.QuizReady,
                                            HistoryActivityType.QuizInProgress,
                                        ) && quizSessionId != null -> {
                                            mainBackStack.add(MainDestination.QuizStart(quizSessionId))
                                        }
                                    }
                                },
                            )

                            MainDestination.Review -> ReviewRoute()

                            MainDestination.MyPage -> MyPageDashboardTab(
                                nickname = currentNickname,
                                state = stateHolder.myPageState,
                                onRetry = ::loadMyPage,
                                onSettingsClick = {
                                    mainBackStack.add(MainDestination.MyPageSettings)
                                },
                            )

                            MainDestination.MyPageSettings -> MyPageSettingsScreen(
                                onBackClick = ::popMainDestination,
                                onAccountClick = {
                                    mainBackStack.add(MainDestination.AccountSettings)
                                },
                                onNotificationClick = {
                                    mainBackStack.add(MainDestination.NotificationSettings)
                                },
                                onInquiryClick = { mainBackStack.add(MainDestination.Inquiry) },
                                onTermsClick = { mainBackStack.add(MainDestination.Terms) },
                                onPrivacyPolicyClick = {
                                    mainBackStack.add(MainDestination.PrivacyPolicy)
                                },
                                onAppInfoClick = {},
                                onLogoutClick = currentOnLogout,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.AccountSettings -> AccountSettingsRoute(
                                onBackClick = ::popMainDestination,
                                onAccountDeleted = currentOnLogout,
                                onProfileChanged = { profile ->
                                    currentOnProfileChanged(profile)
                                    stateHolder.updateMyPageProfile(profile)
                                    homeReloadKey += 1
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.NotificationSettings -> NotificationSettingsRoute(
                                onBackClick = ::popMainDestination,
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.Inquiry -> InquiryRoute(
                                onBackClick = ::popMainDestination,
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.Terms -> LegalTextScreen(
                                title = "이용 약관",
                                body = ServiceTermsRawText,
                                onBackClick = ::popMainDestination,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.PrivacyPolicy -> LegalTextScreen(
                                title = "개인정보 처리방침",
                                body = PrivacyPolicyRawText,
                                onBackClick = ::popMainDestination,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.ExamSchedule -> ExamScheduleRoute(
                                onBackClick = ::popMainDestination,
                                onQuizClick = {
                                    mainBackStack.replaceTopWith(MainDestination.QuizCreate)
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.QuizCreate -> QuizCreateRoute(
                                onBackClick = ::popMainDestination,
                                onAddSubjectClick = {
                                    mainBackStack.replaceTopWith(MainDestination.SubjectCreate)
                                },
                                onQuizGenerationStarted = {
                                    isQuizGenerationWaiting = true
                                    homeReloadKey += 1
                                },
                                onQuizGenerationFinished = {
                                    isQuizGenerationWaiting = false
                                    homeReloadKey += 1
                                },
                                onQuizReady = { launchConfig ->
                                    isQuizGenerationWaiting = false
                                    mainBackStack.replaceTopWith(
                                        MainDestination.QuizStart(launchConfig.quizSessionId),
                                    )
                                    homeReloadKey += 1
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.SubjectCreate -> SubjectCreateRoute(
                                onBackClick = ::popMainDestination,
                                onCreated = { subject ->
                                    showCreatedSubject(subject.id, subject.name)
                                    homeReloadKey += 1
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            MainDestination.HomeUploadPicker -> HomeUploadSubjectPickerScreen(
                                homeState = stateHolder.homeState,
                                starredSubjectIds = stateHolder.starredSubjectIds,
                                onBackClick = ::popMainDestination,
                                onRetryClick = { homeReloadKey += 1 },
                                onAddSubjectClick = {
                                    mainBackStack.replaceTopWith(MainDestination.SubjectCreate)
                                },
                                onSubjectClick = { subject ->
                                    mainBackStack.replaceTopWith(
                                        MainDestination.SubjectDetail(
                                            subjectId = subject.id,
                                            subjectName = subject.name,
                                            uploadRequestId = generateUuid(),
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxSize(),
                            )

                            is MainDestination.SubjectDetail -> SubjectDetailRoute(
                                subjectId = destination.subjectId,
                                subjectName = destination.subjectName,
                                isStarred = destination.subjectId in stateHolder.starredSubjectIds,
                                openUploadOnStart = destination.uploadRequestId != null &&
                                    destination.uploadRequestId != consumedUploadRequestId,
                                onOpenUploadConsumed = {
                                    consumedUploadRequestId = destination.uploadRequestId
                                },
                                onBackClick = {
                                    popMainDestination()
                                    homeReloadKey += 1
                                },
                                onCreateQuizClick = {
                                    mainBackStack.add(MainDestination.QuizCreate)
                                },
                                onStarToggle = { starred ->
                                    stateHolder.setSubjectStarred(destination.subjectId, starred)
                                },
                                onSubjectDeleted = {
                                    popMainDestination()
                                    stateHolder.removeStarredSubject(destination.subjectId)
                                    homeReloadKey += 1
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            is MainDestination.QuizStart -> QuizStartRoute(
                                quizSessionId = destination.quizSessionId,
                                onBackClick = ::popMainDestination,
                                onStartClick = { launchConfig ->
                                    mainBackStack.replaceTopWith(launchConfig.toDestination())
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            is MainDestination.QuizPlay -> QuizPlayRoute(
                                launchConfig = destination.toLaunchConfig(),
                                onBackClick = ::popMainDestination,
                                onResultReady = { resultId ->
                                    mainBackStack.replaceTopWith(MainDestination.QuizResult(resultId))
                                    homeReloadKey += 1
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )

                            is MainDestination.QuizResult -> QuizResultRoute(
                                resultId = destination.resultId,
                                onBackClick = ::popMainDestination,
                                onRetryReady = { launchConfig ->
                                    mainBackStack.replaceTopWith(launchConfig.toDestination())
                                },
                                onSessionExpired = currentOnSessionExpired,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                },
            )
        }
    }
}

private fun Any?.toMainTab(): MainTab = when (this) {
    MainDestination.History -> MainTab.History
    MainDestination.Review -> MainTab.Review
    MainDestination.MyPage -> MainTab.MyPage
    else -> MainTab.Home
}

private fun MainTab.toDestination(): MainDestination = when (this) {
    MainTab.Home -> MainDestination.Home
    MainTab.History -> MainDestination.History
    MainTab.Review -> MainDestination.Review
    MainTab.MyPage -> MainDestination.MyPage
}

internal fun QuizPlayLaunchConfig.toDestination(): MainDestination.QuizPlay =
    MainDestination.QuizPlay(
        quizSessionId = quizSessionId,
        clientSessionId = clientSessionId,
        playSessionId = playSessionId,
        playType = playType.wireValue,
        playMode = playMode.wireValue,
        timerEnabled = timerEnabled,
        timerScope = timerScope?.wireValue,
        timerSeconds = timerSeconds,
    )

internal fun MainDestination.QuizPlay.toLaunchConfig(): QuizPlayLaunchConfig =
    QuizPlayLaunchConfig(
        quizSessionId = quizSessionId,
        clientSessionId = clientSessionId,
        playSessionId = playSessionId,
        playType = QuizPlayType.entries.firstOrNull { it.wireValue == playType }
            ?: QuizPlayType.First,
        playMode = QuizPlayMode.entries.firstOrNull { it.wireValue == playMode }
            ?: QuizPlayMode.AllAtOnce,
        timerEnabled = timerEnabled,
        timerScope = QuizTimerScope.entries.firstOrNull { it.wireValue == timerScope },
        timerSeconds = timerSeconds,
    )
