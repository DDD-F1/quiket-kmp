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
import com.f1.quiket.composeapp.home.presentation.dDayLabel
import com.f1.quiket.composeapp.home.presentation.toHomeExams
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.LegalTextScreen
import com.f1.quiket.composeapp.mypage.AccountSettingsRoute
import com.f1.quiket.composeapp.mypage.InquiryRoute
import com.f1.quiket.composeapp.mypage.MyPageSettingsScreen
import com.f1.quiket.composeapp.mypage.NotificationSettingsRoute
import com.f1.quiket.composeapp.mypage.PrivacyPolicyRawText
import com.f1.quiket.composeapp.mypage.ServiceTermsRawText
import com.f1.quiket.composeapp.main.presentation.HistoryTab
import com.f1.quiket.composeapp.main.presentation.HomeErrorCard
import com.f1.quiket.composeapp.main.presentation.HomeSummaryCard
import com.f1.quiket.composeapp.main.presentation.HomeTutorialOverlay
import com.f1.quiket.composeapp.main.presentation.HomeTutorialPage
import com.f1.quiket.composeapp.main.presentation.HomeUploadSubjectPickerScreen
import com.f1.quiket.composeapp.main.presentation.HomeUiState
import com.f1.quiket.composeapp.main.presentation.MainBottomBar
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import com.f1.quiket.composeapp.main.presentation.MainTab
import com.f1.quiket.composeapp.main.presentation.MyPageDashboardTab
import com.f1.quiket.composeapp.main.presentation.MyPageDestination
import com.f1.quiket.composeapp.main.presentation.MyPageUiState
import com.f1.quiket.composeapp.quiz.QuizCreateRoute
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.QuizPlayRoute
import com.f1.quiket.composeapp.quiz.QuizStartRoute
import com.f1.quiket.composeapp.result.QuizResultRoute
import com.f1.quiket.composeapp.review.ReviewRoute
import com.f1.quiket.composeapp.subject.domain.model.CreatedSubject
import com.f1.quiket.composeapp.subject.SubjectCreateRoute
import com.f1.quiket.composeapp.subject.SubjectDetailRoute
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_acorn
import quiket.composeapp.generated.resources.ic_floating_close
import quiket.composeapp.generated.resources.ic_floating_plus
import quiket.composeapp.generated.resources.ic_home_guide_tooltip
import quiket.composeapp.generated.resources.ic_home_guide_tooltip_close
import quiket.composeapp.generated.resources.ic_home_make
import quiket.composeapp.generated.resources.ic_home_upload
import quiket.composeapp.generated.resources.ic_next
import quiket.composeapp.generated.resources.ic_qring_profile
import quiket.composeapp.generated.resources.ic_small_floting_add
import quiket.composeapp.generated.resources.ic_small_floting_quiz
import quiket.composeapp.generated.resources.ic_small_floting_test
import quiket.composeapp.generated.resources.ic_small_floting_upload
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val HomeGenerationPollIntervalMillis = 1_000L
private const val HomeGenerationPollMaxAttempts = 120
private val MainTabTopPadding = 24.dp

@Composable
internal fun MainScreen(
    nickname: String?,
    initialHomeGuideCompleted: Boolean,
    onLogout: () -> Unit,
    onProfileChanged: (MyProfile) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    var resultDetailId by remember { mutableStateOf<String?>(null) }
    var quizPlayEntry by remember { mutableStateOf<QuizPlayLaunchConfig?>(null) }
    var quizStartEntry by remember { mutableStateOf<QuizPlayLaunchConfig?>(null) }
    var subjectDetailEntry by remember { mutableStateOf<SubjectSummary?>(null) }
    var myPageDestination by remember { mutableStateOf<MyPageDestination?>(null) }
    var examScheduleVisible by remember { mutableStateOf(false) }
    var quizCreateVisible by remember { mutableStateOf(false) }
    var subjectCreateVisible by remember { mutableStateOf(false) }
    var homeUploadPickerVisible by remember { mutableStateOf(false) }
    var subjectDetailOpenUpload by remember { mutableStateOf(false) }
    var homeReloadKey by remember { mutableStateOf(0) }
    var isQuizGenerationWaiting by remember { mutableStateOf(false) }
    val stateHolder = koinInject<MainStateHolder>()
    val homeState = stateHolder.homeState
    val historyState = stateHolder.historyState
    val myPageState = stateHolder.myPageState
    val starredSubjectIds = stateHolder.starredSubjectIds
    val homeGuideCompleted = stateHolder.homeGuideCompleted
    val coroutineScope = rememberCoroutineScope()
    val containerColor = when (selectedTab) {
        MainTab.History, MainTab.Review -> QuiketBrown50
        else -> QuiketWhite
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

    val selectedQuizPlayEntry = quizPlayEntry
    if (selectedQuizPlayEntry != null) {
        QuizPlayRoute(
            launchConfig = selectedQuizPlayEntry,
            onBackClick = { quizPlayEntry = null },
            onResultReady = { resultId ->
                quizPlayEntry = null
                resultDetailId = resultId
                homeReloadKey += 1
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    val selectedQuizStartEntry = quizStartEntry
    if (selectedQuizStartEntry != null) {
        QuizStartRoute(
            quizSessionId = selectedQuizStartEntry.quizSessionId,
            onBackClick = { quizStartEntry = null },
            onStartClick = { launchConfig ->
                quizStartEntry = null
                quizPlayEntry = launchConfig
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    val selectedResultId = resultDetailId
    if (selectedResultId != null) {
        QuizResultRoute(
            resultId = selectedResultId,
            onBackClick = { resultDetailId = null },
            onRetryReady = { launchConfig ->
                resultDetailId = null
                quizPlayEntry = launchConfig
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    if (quizCreateVisible) {
        QuizCreateRoute(
            onBackClick = {
                quizCreateVisible = false
            },
            onAddSubjectClick = {
                quizCreateVisible = false
                subjectCreateVisible = true
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
                quizCreateVisible = false
                isQuizGenerationWaiting = false
                quizStartEntry = launchConfig
                homeReloadKey += 1
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    if (subjectCreateVisible) {
        SubjectCreateRoute(
            onBackClick = { subjectCreateVisible = false },
            onCreated = { subject ->
                subjectCreateVisible = false
                subjectDetailEntry = subject.toHomeSubjectSummary()
                homeReloadKey += 1
            },
            onSessionExpired = onSessionExpired,
            modifier = modifier,
        )
        return
    }

    if (homeUploadPickerVisible) {
        HomeUploadSubjectPickerScreen(
            homeState = homeState,
            starredSubjectIds = starredSubjectIds,
            onBackClick = { homeUploadPickerVisible = false },
            onRetryClick = { homeReloadKey += 1 },
            onAddSubjectClick = {
                homeUploadPickerVisible = false
                subjectCreateVisible = true
            },
            onSubjectClick = { subject ->
                homeUploadPickerVisible = false
                subjectDetailEntry = subject
                subjectDetailOpenUpload = true
            },
            modifier = modifier,
        )
        return
    }

    LaunchedEffect(homeReloadKey) {
        stateHolder.loadHome(onSessionExpired = onSessionExpired)
    }

    fun loadHistory(reset: Boolean) {
        coroutineScope.launch {
            stateHolder.loadHistory(
                reset = reset,
                onSessionExpired = onSessionExpired,
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
            stateHolder.loadMyPage(onSessionExpired = onSessionExpired)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == MainTab.MyPage && myPageState == MyPageUiState.Idle) {
            loadMyPage()
        }
    }

    fun switchMainTab(tab: MainTab) {
        selectedTab = tab
        examScheduleVisible = false
        subjectDetailEntry = null
        subjectDetailOpenUpload = false
        myPageDestination = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = containerColor,
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onTabClick = ::switchMainTab,
            )
        },
    ) { paddingValues ->
        val activeMyPageDestination = if (selectedTab == MainTab.MyPage) myPageDestination else null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (subjectDetailEntry == null && activeMyPageDestination == null && !examScheduleVisible) {
                        Modifier.statusBarsPadding()
                    } else {
                        Modifier
                    },
                )
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(containerColor),
        ) {
            when (activeMyPageDestination) {
                MyPageDestination.Settings -> {
                    MyPageSettingsScreen(
                        onBackClick = { myPageDestination = null },
                        onAccountClick = { myPageDestination = MyPageDestination.AccountSettings },
                        onNotificationClick = { myPageDestination = MyPageDestination.NotificationSettings },
                        onInquiryClick = { myPageDestination = MyPageDestination.Inquiry },
                        onTermsClick = { myPageDestination = MyPageDestination.Terms },
                        onPrivacyPolicyClick = { myPageDestination = MyPageDestination.PrivacyPolicy },
                        onAppInfoClick = { },
                        onLogoutClick = onLogout,
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                MyPageDestination.AccountSettings -> {
                    AccountSettingsRoute(
                        onBackClick = { myPageDestination = MyPageDestination.Settings },
                        onAccountDeleted = onLogout,
                        onProfileChanged = { profile ->
                        onProfileChanged(profile)
                            stateHolder.updateMyPageProfile(profile)
                            homeReloadKey += 1
                        },
                        onSessionExpired = onSessionExpired,
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                MyPageDestination.NotificationSettings -> {
                    NotificationSettingsRoute(
                        onBackClick = { myPageDestination = MyPageDestination.Settings },
                        onSessionExpired = onSessionExpired,
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                MyPageDestination.Inquiry -> {
                    InquiryRoute(
                        onBackClick = { myPageDestination = MyPageDestination.Settings },
                        onSessionExpired = onSessionExpired,
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                MyPageDestination.Terms -> {
                    LegalTextScreen(
                        title = "이용 약관",
                        body = ServiceTermsRawText,
                        onBackClick = { myPageDestination = MyPageDestination.Settings },
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                MyPageDestination.PrivacyPolicy -> {
                    LegalTextScreen(
                        title = "개인정보 처리방침",
                        body = PrivacyPolicyRawText,
                        onBackClick = { myPageDestination = MyPageDestination.Settings },
                        modifier = Modifier.fillMaxSize(),
                    )
                    return@Box
                }

                null -> Unit
            }

            if (examScheduleVisible) {
                ExamScheduleRoute(
                    onBackClick = { examScheduleVisible = false },
                    onQuizClick = {
                        examScheduleVisible = false
                        quizCreateVisible = true
                    },
                    onSessionExpired = onSessionExpired,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Box
            }

            val selectedSubject = subjectDetailEntry
            if (selectedSubject != null) {
                SubjectDetailRoute(
                    subjectId = selectedSubject.id,
                    subjectName = selectedSubject.name,
                    isStarred = selectedSubject.id in starredSubjectIds,
                    openUploadOnStart = subjectDetailOpenUpload,
                    onOpenUploadConsumed = { subjectDetailOpenUpload = false },
                    onBackClick = {
                        subjectDetailEntry = null
                        subjectDetailOpenUpload = false
                        homeReloadKey += 1
                    },
                    onCreateQuizClick = {
                        quizCreateVisible = true
                    },
                    onStarToggle = { starred ->
                        stateHolder.setSubjectStarred(selectedSubject.id, starred)
                    },
                    onSubjectDeleted = {
                        subjectDetailEntry = null
                        stateHolder.removeStarredSubject(selectedSubject.id)
                        homeReloadKey += 1
                    },
                    onSessionExpired = onSessionExpired,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Box
            }

            when (selectedTab) {
                MainTab.Home -> HomeTab(
                    nickname = nickname,
                    homeState = homeState,
                    isQuizGenerationWaiting = isQuizGenerationWaiting,
                    starredSubjectIds = starredSubjectIds,
                    showGuideTooltip = !homeGuideCompleted,
                    onGuideTooltipDismiss = {
                        if (!homeGuideCompleted) {
                            coroutineScope.launch {
                                stateHolder.dismissHomeGuide()
                            }
                        }
                    },
                    onRetry = { homeReloadKey += 1 },
                    onSubjectClick = { subject -> subjectDetailEntry = subject },
                    onSubjectStarToggle = { subjectId ->
                        stateHolder.toggleStarredSubject(subjectId)
                    },
                    onRecentActivityClick = { activity ->
                        val resultId = activity.resultDetailId
                        val quizStartId = activity.quizStartId
                        when {
                            resultId != null -> {
                                resultDetailId = resultId
                            }

                            quizStartId != null -> {
                                quizStartEntry = QuizPlayLaunchConfig(quizSessionId = quizStartId)
                            }
                        }
                    },
                    onCreateQuizClick = {
                        quizCreateVisible = true
                    },
                    onUploadClick = { homeUploadPickerVisible = true },
                    onExamScheduleClick = { examScheduleVisible = true },
                    onAddSubjectClick = { subjectCreateVisible = true },
                )
                MainTab.History -> HistoryTab(
                    state = historyState,
                    onRefresh = { loadHistory(reset = true) },
                    onLoadMore = { loadHistory(reset = false) },
                    onActivityClick = { activity ->
                        val resultId = activity.resultId ?: activity.playSessionId
                        when {
                            activity.activityType == HistoryActivityType.QuizCompleted &&
                                resultId != null -> {
                                resultDetailId = resultId
                            }

                            activity.activityType in setOf(
                                HistoryActivityType.QuizReady,
                                HistoryActivityType.QuizInProgress,
                            ) &&
                                activity.quizSessionId != null -> {
                                quizStartEntry = QuizPlayLaunchConfig(quizSessionId = activity.quizSessionId)
                            }
                        }
                    },
                )

                MainTab.Review -> ReviewRoute()

                MainTab.MyPage -> MyPageDashboardTab(
                    nickname = nickname,
                    state = myPageState,
                    onRetry = ::loadMyPage,
                    onSettingsClick = { myPageDestination = MyPageDestination.Settings },
                )
            }
        }
    }
}

@Composable
private fun HomeTab(
    nickname: String?,
    homeState: HomeUiState,
    isQuizGenerationWaiting: Boolean,
    starredSubjectIds: Set<String>,
    showGuideTooltip: Boolean,
    onGuideTooltipDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSubjectClick: (SubjectSummary) -> Unit,
    onSubjectStarToggle: (String) -> Unit,
    onRecentActivityClick: (RecentActivity) -> Unit,
    onCreateQuizClick: () -> Unit,
    onUploadClick: () -> Unit,
    onExamScheduleClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeData = (homeState as? HomeUiState.Success)?.data
    val displayNickname = homeData?.user?.nickname?.takeIf { it.isNotBlank() }
        ?: nickname
        ?: "사용자"
    var tutorialPage by remember { mutableStateOf<HomeTutorialPage?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    var noSubjectUploadDialogVisible by remember { mutableStateOf(false) }
    var selectedContentTab by remember { mutableStateOf(HomeContentTab.Subjects) }
    var subjectTabRect by remember { mutableStateOf<Rect?>(null) }
    var uploadActionRect by remember { mutableStateOf<Rect?>(null) }
    var quizActionRect by remember { mutableStateOf<Rect?>(null) }
    var profileCardRect by remember { mutableStateOf<Rect?>(null) }
    var examCardRect by remember { mutableStateOf<Rect?>(null) }
    var activityTabRect by remember { mutableStateOf<Rect?>(null) }
    var fabRect by remember { mutableStateOf<Rect?>(null) }
    val hasSubjects = homeData?.subjects?.isNotEmpty() == true
    val activeQuiz = homeData
        ?.hero
        ?.activeQuiz
        ?.takeIf { activity -> activity.isActionClickable }
    val homeExams = remember(homeData) { homeData.toHomeExams() }
    val quizActionText = when {
        isQuizGenerationWaiting -> "퀴즈 생성 중..."
        activeQuiz != null -> "퀴즈 풀기"
        else -> "퀴즈 만들기"
    }
    val quizActionClick: (() -> Unit)? = when {
        isQuizGenerationWaiting -> null
        activeQuiz != null -> ({ onRecentActivityClick(activeQuiz) })
        homeState is HomeUiState.Success -> onCreateQuizClick
        else -> null
    }

    fun requestUpload() {
        if (hasSubjects) {
            onUploadClick()
        } else {
            noSubjectUploadDialogVisible = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = QuiketWhite,
            ) {
                QuiketTopBar(
                    onNoteIconClick = {
                        onGuideTooltipDismiss()
                        tutorialPage = HomeTutorialPage.First
                    },
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = QuiketWhite,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                ) {
                    HomeHeroActionPanel(
                        quizActionText = quizActionText,
                        onUploadClick = if (homeState is HomeUiState.Success) ::requestUpload else null,
                        onQuizClick = quizActionClick,
                        onUploadPositioned = { uploadActionRect = it },
                        onQuizPositioned = { quizActionRect = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    )
                }

                when (homeState) {
                    HomeUiState.Loading -> HomeSummaryCard(
                        title = "홈 정보를 불러오는 중",
                        description = "학습 현황을 확인하고 있어요.",
                        modifier = Modifier.padding(20.dp),
                    )

                    is HomeUiState.Error -> HomeErrorCard(
                        message = homeState.message,
                        onRetry = onRetry,
                        modifier = Modifier.padding(20.dp),
                    )

                    is HomeUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 10.dp),
                        ) {
                            HomeProfileSummaryCard(
                                nickname = displayNickname,
                                dotoriBalance = homeState.data.user.dotoriBalance,
                                onPositioned = { profileCardRect = it },
                            )

                            if (homeExams.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                        .captureRootRect { examCardRect = it },
                                ) {
                                    HomeEmptyExamCard(onClick = onExamScheduleClick)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 10.dp)
                                        .captureRootRect { examCardRect = it },
                                ) {
                                    HomeExamPager(
                                        exams = homeExams,
                                        onClick = onExamScheduleClick,
                                    )
                                }
                            }

                            HomeContentTabs(
                                selectedTab = selectedContentTab,
                                onTabSelected = { selectedContentTab = it },
                                onSubjectsTabPositioned = { subjectTabRect = it },
                                onRecentTabPositioned = { activityTabRect = it },
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                color = QuiketWhite,
                                shape = RoundedCornerShape(
                                    topEnd = 24.dp,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    when (selectedContentTab) {
                                        HomeContentTab.Subjects -> {
                                            val sortedSubjects = homeState.data.subjects
                                                .sortedForDisplay(starredSubjectIds)
                                            HomeSubjectGrid(
                                                subjects = sortedSubjects,
                                                starredSubjectIds = starredSubjectIds,
                                                onAddSubjectClick = onAddSubjectClick,
                                                onSubjectClick = onSubjectClick,
                                                onSubjectStarToggle = onSubjectStarToggle,
                                            )
                                        }

                                        HomeContentTab.Recent -> {
                                            if (homeState.data.recentActivities.isEmpty()) {
                                                HomeEmptyContent(
                                                    title = "아직 최근 활동이 없어요",
                                                    description = "퀴즈를 만들고 풀면 이곳에서 이어볼 수 있어요.",
                                                )
                                            } else {
                                                homeState.data.recentActivities.forEachIndexed { index, activity ->
                                                    HomeRecentActivityCard(
                                                        activity = activity,
                                                        position = index + 1,
                                                        onClick = if (activity.isActionClickable) {
                                                            { onRecentActivityClick(activity) }
                                                        } else {
                                                            null
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(QuiketGray950.copy(alpha = 0.24f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    ) {
                        fabExpanded = false
                    },
            )
        }

        HomeExpandableFab(
            expanded = fabExpanded,
            onToggle = { fabExpanded = !fabExpanded },
            onExamScheduleClick = {
                fabExpanded = false
                onExamScheduleClick()
            },
            onCreateQuizClick = {
                fabExpanded = false
                onCreateQuizClick()
            },
            onUploadClick = {
                fabExpanded = false
                requestUpload()
            },
            onAddSubjectClick = {
                fabExpanded = false
                onAddSubjectClick()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .captureRootRect { fabRect = it },
        )

        if (showGuideTooltip) {
            HomeGuideTooltip(
                onClose = onGuideTooltipDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 104.dp, end = 20.dp),
            )
        }

        if (noSubjectUploadDialogVisible) {
            NoSubjectUploadDialog(
                onAddSubjectClick = {
                    noSubjectUploadDialogVisible = false
                    onAddSubjectClick()
                },
                onDismiss = { noSubjectUploadDialogVisible = false },
            )
        }

        tutorialPage?.let { page ->
            HomeTutorialOverlay(
                page = page,
                subjectTabRect = subjectTabRect,
                uploadButtonRect = uploadActionRect,
                quizButtonRect = quizActionRect,
                profileCardRect = profileCardRect,
                examCardRect = examCardRect,
                activityTabRect = activityTabRect,
                fabRect = fabRect,
                onNext = {
                    tutorialPage = when (page) {
                        HomeTutorialPage.First -> HomeTutorialPage.Second
                        HomeTutorialPage.Second -> HomeTutorialPage.Third
                        HomeTutorialPage.Third -> null
                    }
                },
                onSkip = { tutorialPage = null },
            )
        }
    }
}

@Composable
private fun HomeSubjectGrid(
    subjects: List<SubjectSummary>,
    starredSubjectIds: Set<String>,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (SubjectSummary) -> Unit,
    onSubjectStarToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = listOf<SubjectSummary?>(null) + subjects
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        cells.chunked(2).forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowCells.forEach { subject ->
                    if (subject == null) {
                        HomeAddSubjectCard(
                            onClick = onAddSubjectClick,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        HomeSubjectCard(
                            subject = subject,
                            isStarred = subject.id in starredSubjectIds,
                            onClick = { onSubjectClick(subject) },
                            onStarClick = { onSubjectStarToggle(subject.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (rowCells.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHeroActionPanel(
    quizActionText: String,
    onUploadClick: (() -> Unit)?,
    onQuizClick: (() -> Unit)?,
    onUploadPositioned: (Rect) -> Unit,
    onQuizPositioned: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "오늘의 공부, 시작해 볼까요?",
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        Text(
            text = "내 강의 노트를 업로드 하거나 퀴즈를 만들어 보세요 !",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HomePrimaryActionCard(
                title = "자료 업로드",
                icon = Res.drawable.ic_home_upload,
                containerColor = QuiketGray100,
                onClick = onUploadClick,
                modifier = Modifier
                    .weight(1f)
                    .captureRootRect(onUploadPositioned),
            )
            HomePrimaryActionCard(
                title = quizActionText,
                icon = Res.drawable.ic_home_make,
                containerColor = QuiketOrange500,
                onClick = onQuizClick,
                modifier = Modifier
                    .weight(1f)
                    .captureRootRect(onQuizPositioned),
            )
        }
    }
}

@Composable
private fun HomePrimaryActionCard(
    title: String,
    icon: DrawableResource,
    containerColor: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun Content() {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(QuiketWhite.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = QuiketBrown950,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier.height(100.dp),
            color = containerColor.copy(alpha = 0.54f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Content()
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.height(100.dp),
            color = containerColor,
            shape = RoundedCornerShape(12.dp),
        ) {
            Content()
        }
    }
}

@Composable
private fun HomeProfileSummaryCard(
    nickname: String,
    dotoriBalance: Int,
    onPositioned: (Rect) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketBrown950,
        shape = RoundedCornerShape(1000.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
                .captureRootRect(onPositioned),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_qring_profile),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = nickname,
                color = QuiketWhite,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Image(
                painter = painterResource(Res.drawable.ic_acorn),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$dotoriBalance",
                color = QuiketWhite,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(Res.drawable.ic_next),
                contentDescription = null,
                tint = QuiketWhite,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun HomeEmptyExamCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(
                color = QuiketGray300,
                strokeWidth = 2.dp,
                cornerRadius = 100.dp,
                dashLength = 7.dp,
                gapLength = 5.dp,
            ),
        color = QuiketGray50,
        shape = RoundedCornerShape(1000.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "시험 일정이 아직 없어요",
                color = QuiketGray700,
                style = MaterialTheme.typography.labelSmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_small_floting_add),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "일정을 추가하고 D-Day를 확인해 보세요",
                    color = Color(0xFF535355),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun HomeExamPager(
    exams: List<HomeExamUiModel>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { exams.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            HomeExamCard(
                exam = exams[page],
                onClick = onClick,
            )
        }

        if (exams.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(exams.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (selected) QuiketBrown950 else QuiketGray100),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeExamCard(
    exam: HomeExamUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = QuiketWhite,
        shape = RoundedCornerShape(1000.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(QuiketGray50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_small_floting_test),
                    contentDescription = null,
                    tint = QuiketBrown950,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = exam.name,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = exam.date,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (exam.isUrgent) QuiketNegative else QuiketBrown950)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = exam.dDayLabel,
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun HomeContentTabs(
    selectedTab: HomeContentTab,
    onTabSelected: (HomeContentTab) -> Unit,
    onSubjectsTabPositioned: (Rect) -> Unit,
    onRecentTabPositioned: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        HomeContentTabButton(
            text = "내 과목",
            selected = selectedTab == HomeContentTab.Subjects,
            onClick = { onTabSelected(HomeContentTab.Subjects) },
            modifier = Modifier
                .weight(0.7f)
                .captureRootRect(onSubjectsTabPositioned),
        )
        HomeContentTabButton(
            text = "최근 활동",
            selected = selectedTab == HomeContentTab.Recent,
            onClick = { onTabSelected(HomeContentTab.Recent) },
            modifier = Modifier
                .weight(0.7f)
                .captureRootRect(onRecentTabPositioned),
        )
        Spacer(modifier = Modifier.weight(1.6f))
    }
}

@Composable
private fun HomeContentTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        color = if (selected) QuiketWhite else Color.Transparent,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) QuiketBrown950 else QuiketGray600,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun HomeEmptyContent(
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        if (buttonText != null && onButtonClick != null) {
            QuiketPrimaryButton(
                text = buttonText,
                enabled = true,
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HomeExpandableFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onExamScheduleClick: () -> Unit,
    onCreateQuizClick: () -> Unit,
    onUploadClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeFabAction(
                    text = "시험 등록하기",
                    icon = Res.drawable.ic_small_floting_test,
                    onClick = onExamScheduleClick,
                )
                HomeFabAction(
                    text = "퀴즈 만들기",
                    icon = Res.drawable.ic_small_floting_quiz,
                    onClick = onCreateQuizClick,
                )
                HomeFabAction(
                    text = "자료 업로드",
                    icon = Res.drawable.ic_small_floting_upload,
                    onClick = onUploadClick,
                )
                HomeFabAction(
                    text = "과목 추가",
                    icon = Res.drawable.ic_small_floting_add,
                    onClick = onAddSubjectClick,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(QuiketBrown950)
                .clickable(onClick = onToggle)
                .semantics {
                    role = Role.Button
                    contentDescription = if (expanded) "홈 플로팅 메뉴 닫기" else "홈 플로팅 메뉴 열기"
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (expanded) Res.drawable.ic_floating_close else Res.drawable.ic_floating_plus,
                ),
                contentDescription = null,
                tint = QuiketWhite,
            )
        }
    }
}

@Composable
private fun HomeFabAction(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = text
            },
        shape = RoundedCornerShape(24.dp),
        color = QuiketWhite,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(QuiketBrown50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = QuiketBrown950,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun NoSubjectUploadDialog(
    onAddSubjectClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "과목을 먼저 추가해주세요",
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                text = "자료를 업로드하려면 학습 중인 과목이 필요해요.",
                color = QuiketGray700,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onAddSubjectClick) {
                Text(
                    text = "과목 추가",
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "닫기",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        containerColor = QuiketWhite,
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun HomeGuideTooltip(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(288.dp),
        shape = RoundedCornerShape(12.dp),
        color = QuiketBrown50,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_home_guide_tooltip),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "가이드북을 클릭해서 앱을 \n한 번 둘러볼까요?",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
                    .semantics {
                        role = Role.Button
                        contentDescription = "홈 가이드 툴팁 닫기"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_home_guide_tooltip_close),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun Modifier.captureRootRect(onRectChanged: (Rect) -> Unit): Modifier =
    onGloballyPositioned { coordinates ->
        val position = coordinates.positionInRoot()
        onRectChanged(
            Rect(
                left = position.x,
                top = position.y,
                right = position.x + coordinates.size.width,
                bottom = position.y + coordinates.size.height,
            ),
        )
    }

private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp,
    dashLength: Dp,
    gapLength: Dp,
): Modifier = drawWithContent {
    drawContent()

    val strokeWidthPx = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            ),
            cap = StrokeCap.Round,
        ),
    )
}

@Composable
private fun HomeSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
private fun HomeAddSubjectCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketGray50)
            .dashedBorder(
                color = QuiketGray300,
                strokeWidth = 2.dp,
                cornerRadius = 12.dp,
                dashLength = 7.dp,
                gapLength = 5.dp,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "과목 추가"
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "+ 과목 추가",
                color = Color(0xFF96989B),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeSubjectCard(
    subject: SubjectSummary,
    isStarred: Boolean,
    onClick: () -> Unit,
    onStarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp)
            .semantics {
                contentDescription = subject.name
            },
        color = QuiketGray50,
        shape = RoundedCornerShape(4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFBAA38A)),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 8.dp, end = 38.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = subject.name,
                    color = Color(0xFF535355),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "챕터 ${subject.chapterCount}",
                        color = Color(0xFF684C40),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE8E2D9))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    subject.examSchedule?.dDay?.let { dDay ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dDayLabel(dDay),
                            color = QuiketOrange500,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onStarClick)
                    .clearAndSetSemantics {
                        contentDescription = if (isStarred) {
                            "즐겨찾기 해제"
                        } else {
                            "즐겨찾기 추가"
                        }
                        role = Role.Button
                        onClick {
                            onStarClick()
                            true
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isStarred) "★" else "☆",
                    color = if (isStarred) QuiketOrange500 else QuiketGray400,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HomeRecentActivityCard(
    activity: RecentActivity,
    position: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun Content() {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = activity.title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = activity.scoreText?.takeIf { it.isNotBlank() }
                    ?: activity.status?.takeIf { it.isNotBlank() }
                    ?: activity.subjectName,
                color = QuiketGray600,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = activity.accessibilityLabel(position)
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                },
            color = QuiketGray50,
            shape = RoundedCornerShape(18.dp),
        ) {
            Content()
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = activity.accessibilityLabel(position)
                },
            color = QuiketGray50,
            shape = RoundedCornerShape(18.dp),
        ) {
            Content()
        }
    }
}

@Composable
private fun EmptyTab(
    title: String,
    headline: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = MainTabTopPadding),
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(QuiketWhite),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_acorn),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = headline,
            modifier = Modifier.fillMaxWidth(),
            color = QuiketBrown950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            color = QuiketGray700,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.weight(1.2f))
    }
}

private enum class HomeContentTab {
    Subjects,
    Recent,
}

private fun String.toDateLabel(): String =
    substringBefore('T').takeIf { it.isNotBlank() } ?: this

private fun List<SubjectSummary>.sortedForDisplay(
    starredSubjectIds: Set<String>,
): List<SubjectSummary> =
    filter { subject -> subject.id in starredSubjectIds }.sortedBy { it.name } +
        filterNot { subject -> subject.id in starredSubjectIds }

private val RecentActivity.resultDetailId: String?
    get() = if (activityType.equals(RecentActivityQuizCompletedType, ignoreCase = true)) {
        resultId?.takeIf { it.isNotBlank() }
            ?: playSessionId?.takeIf { it.isNotBlank() }
    } else {
        null
    }

private val RecentActivity.quizStartId: String?
    get() {
        val canStartQuiz = activityType.equals(RecentActivityQuizReadyType, ignoreCase = true) ||
            activityType.equals(RecentActivityQuizInProgressType, ignoreCase = true)
        return if (canStartQuiz) {
            quizSessionId?.takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

private val RecentActivity.isActionClickable: Boolean
    get() = resultDetailId != null || quizStartId != null

private fun RecentActivity.accessibilityLabel(position: Int): String {
    val statusLabel = when {
        activityType.equals(RecentActivityQuizCompletedType, ignoreCase = true) -> "완료"
        activityType.equals(RecentActivityQuizReadyType, ignoreCase = true) -> "풀기 전"
        activityType.equals(RecentActivityQuizInProgressType, ignoreCase = true) -> "진행 중"
        else -> status?.takeIf { it.isNotBlank() } ?: "기록"
    }
    return listOfNotNull(
        "최근 활동 $position",
        title.takeIf { it.isNotBlank() },
        subjectName.takeIf { it.isNotBlank() },
        statusLabel,
        scoreText?.takeIf { it.isNotBlank() },
    ).joinToString(", ")
}

private const val RecentActivityQuizCompletedType = "quiz_completed"
private const val RecentActivityQuizReadyType = "quiz_ready"
private const val RecentActivityQuizInProgressType = "quiz_in_progress"

private fun CreatedSubject.toHomeSubjectSummary(): SubjectSummary =
    SubjectSummary(
        id = id,
        name = name,
        purpose = purpose,
        chapterCount = 0,
        partCount = 0,
        lastActivityAt = null,
        examSchedule = null,
    )
