package com.f1.quiket.composeapp.main

import org.koin.compose.koinInject
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import com.f1.quiket.composeapp.history.HistoryActivity
import com.f1.quiket.composeapp.history.HistoryActivityType
import com.f1.quiket.composeapp.home.HomeData
import com.f1.quiket.composeapp.home.ExamScheduleRoute
import com.f1.quiket.composeapp.home.RecentActivity
import com.f1.quiket.composeapp.home.SubjectExamSchedule
import com.f1.quiket.composeapp.home.SubjectSummary
import com.f1.quiket.composeapp.mypage.MyPageData
import com.f1.quiket.composeapp.mypage.MyProfile
import com.f1.quiket.composeapp.mypage.LegalTextScreen
import com.f1.quiket.composeapp.mypage.AccountSettingsRoute
import com.f1.quiket.composeapp.mypage.InquiryRoute
import com.f1.quiket.composeapp.mypage.MyPageSettingsScreen
import com.f1.quiket.composeapp.mypage.NotificationSettingsRoute
import com.f1.quiket.composeapp.mypage.PrivacyPolicyRawText
import com.f1.quiket.composeapp.mypage.ServiceTermsRawText
import com.f1.quiket.composeapp.main.presentation.HistoryUiState
import com.f1.quiket.composeapp.main.presentation.HomeUiState
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import com.f1.quiket.composeapp.main.presentation.MyPageUiState
import com.f1.quiket.composeapp.quiz.QuizCreateRoute
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.QuizPlayRoute
import com.f1.quiket.composeapp.quiz.QuizStartRoute
import com.f1.quiket.composeapp.result.QuizResultRoute
import com.f1.quiket.composeapp.review.ReviewRoute
import com.f1.quiket.composeapp.subject.CreatedSubject
import com.f1.quiket.composeapp.subject.SubjectCreateRoute
import com.f1.quiket.composeapp.subject.SubjectDetailRoute
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_acorn
import quiket.composeapp.generated.resources.ic_bottom_home_gray
import quiket.composeapp.generated.resources.ic_bottom_home_primary
import quiket.composeapp.generated.resources.ic_bottom_my_gray
import quiket.composeapp.generated.resources.ic_bottom_my_primary
import quiket.composeapp.generated.resources.ic_bottom_record_gray
import quiket.composeapp.generated.resources.ic_bottom_record_primary
import quiket.composeapp.generated.resources.ic_bottom_review_gray
import quiket.composeapp.generated.resources.ic_bottom_review_primary
import quiket.composeapp.generated.resources.ic_floating_close
import quiket.composeapp.generated.resources.ic_floating_plus
import quiket.composeapp.generated.resources.ic_home_guide_tooltip
import quiket.composeapp.generated.resources.ic_home_guide_tooltip_close
import quiket.composeapp.generated.resources.ic_home_make
import quiket.composeapp.generated.resources.ic_home_upload
import quiket.composeapp.generated.resources.ic_item_carpet
import quiket.composeapp.generated.resources.ic_item_clock
import quiket.composeapp.generated.resources.ic_item_flower
import quiket.composeapp.generated.resources.ic_item_sofa
import quiket.composeapp.generated.resources.ic_my_acorn
import quiket.composeapp.generated.resources.ic_my_fire
import quiket.composeapp.generated.resources.ic_my_lock
import quiket.composeapp.generated.resources.ic_my_qring
import quiket.composeapp.generated.resources.ic_my_store
import quiket.composeapp.generated.resources.ic_next
import quiket.composeapp.generated.resources.ic_qring_profile
import quiket.composeapp.generated.resources.ic_small_floting_add
import quiket.composeapp.generated.resources.ic_small_floting_quiz
import quiket.composeapp.generated.resources.ic_small_floting_test
import quiket.composeapp.generated.resources.ic_small_floting_upload
import quiket.composeapp.generated.resources.ic_speech_balloon
import quiket.composeapp.generated.resources.ic_topbar_setting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
            BottomBar(
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
private fun BottomBar(
    selectedTab: MainTab,
    onTabClick: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(QuiketWhite)
            .padding(top = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MainTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Tab) { onTabClick(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Icon(
                    painter = painterResource(if (selected) tab.selectedIcon else tab.unselectedIcon),
                    contentDescription = tab.label,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) QuiketBrown950 else QuiketGray400,
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

private val TutorialOverlayColor = Color(0xE62A2A2A)
private val TutorialHighlightTextColor = Color(0xFFFFBB70)

@Composable
private fun HomeTutorialOverlay(
    page: HomeTutorialPage,
    subjectTabRect: Rect?,
    uploadButtonRect: Rect?,
    quizButtonRect: Rect?,
    profileCardRect: Rect?,
    examCardRect: Rect?,
    activityTabRect: Rect?,
    fabRect: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val steps = page.steps(
        subjectTabRect = subjectTabRect,
        uploadButtonRect = uploadButtonRect,
        quizButtonRect = quizButtonRect,
        profileCardRect = profileCardRect,
        examCardRect = examCardRect,
        activityTabRect = activityTabRect,
        fabRect = fabRect,
    )
    val isLastPage = page == HomeTutorialPage.Third
    val density = LocalDensity.current
    var overlayRootOffset by remember { mutableStateOf(Offset.Zero) }
    val localSteps = steps.map { step ->
        step.copy(anchorRect = step.anchorRect?.toLocalRect(overlayRootOffset))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayRootOffset = coordinates.positionInRoot()
            }
            .zIndex(100f),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onNext() },
        ) {
            drawRect(color = TutorialOverlayColor)

            localSteps.forEach { step ->
                step.anchorRect?.let { rect ->
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.right - rect.left, rect.bottom - rect.top),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        blendMode = BlendMode.Clear,
                    )
                }
            }

            localSteps.forEach { step ->
                step.anchorRect?.let { rect ->
                    val left = rect.left
                    val top = rect.top
                    val right = rect.right
                    val bottom = rect.bottom
                    val anchorCenterX = (left + right) / 2f
                    val anchorCenterY = (top + bottom) / 2f
                    val (tooltipX, tooltipY) = step.tooltipOrigin(rect, density)

                    val tooltipW = with(density) { 120.dp.toPx() }
                    val tooltipH = with(density) { 60.dp.toPx() }
                    val tooltipCenterX = tooltipX + tooltipW / 2f
                    val tooltipCenterY = tooltipY + tooltipH / 2f
                    val anchorGapPx = with(density) { 10.dp.toPx() }

                    val (baseStartX, baseStartY) = when {
                        tooltipCenterY < top -> anchorCenterX to top - anchorGapPx
                        tooltipCenterY > bottom -> anchorCenterX to bottom + anchorGapPx
                        tooltipCenterX < left -> left - anchorGapPx to anchorCenterY
                        else -> right + anchorGapPx to anchorCenterY
                    }
                    val start = Offset(
                        x = baseStartX + with(density) { step.arrowStartOffset.x.dp.toPx() },
                        y = baseStartY + with(density) { step.arrowStartOffset.y.dp.toPx() },
                    )

                    val tooltipGapPx = with(density) { 12.dp.toPx() }
                    val (baseEndX, baseEndY) = when {
                        tooltipCenterY < top -> tooltipCenterX to tooltipY + tooltipH + tooltipGapPx
                        tooltipCenterY > bottom -> tooltipCenterX to tooltipY - tooltipGapPx
                        tooltipCenterX < left -> tooltipX + tooltipW + tooltipGapPx to tooltipCenterY
                        else -> tooltipX - tooltipGapPx to tooltipCenterY
                    }
                    val end = Offset(
                        x = baseEndX + with(density) { step.arrowEndOffset.x.dp.toPx() },
                        y = baseEndY + with(density) { step.arrowEndOffset.y.dp.toPx() },
                    )

                    drawDashedCurvedArrow(
                        start = start,
                        end = end,
                        color = QuiketWhite,
                        strokeWidth = with(density) { 2.dp.toPx() },
                        dashLength = with(density) { 3.dp.toPx() },
                        gapLength = with(density) { 3.dp.toPx() },
                        arrowSize = with(density) { 7.dp.toPx() },
                        curvature = step.arrowCurvature,
                    )
                }
            }
        }

        localSteps.forEach { step ->
            step.anchorRect?.let { rect ->
                val (x, y) = step.tooltipOffset(rect, density)
                HomeTutorialTooltip(
                    step = step,
                    modifier = Modifier.offset(x = x, y = y),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onNext() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isLastPage) {
                    "Quiket 사용해보기"
                } else {
                    "탭하여 다음으로 넘어가기"
                },
                color = QuiketWhite,
                style = MaterialTheme.typography.labelSmall,
            )
            if (!isLastPage) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ">",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

private fun Rect.toLocalRect(rootOffset: Offset): Rect = Rect(
    left = left - rootOffset.x,
    top = top - rootOffset.y,
    right = right - rootOffset.x,
    bottom = bottom - rootOffset.y,
)

@Composable
private fun HomeTutorialTooltip(
    step: HomeTutorialStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(220.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(QuiketOrange500),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${step.step}",
                color = QuiketWhite,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = QuiketWhite)) {
                    append(step.startText)
                }
                withStyle(SpanStyle(color = TutorialHighlightTextColor)) {
                    append(step.highlightedText)
                }
                withStyle(SpanStyle(color = QuiketWhite)) {
                    append(step.endText)
                }
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun HomeTutorialStep.tooltipOrigin(rect: Rect, density: androidx.compose.ui.unit.Density): Pair<Float, Float> = with(density) {
    val left = rect.left
    val top = rect.top
    val right = rect.right
    val bottom = rect.bottom
    return when (tooltipAlignment) {
        HomeTutorialAlignment.Step1 -> left + -30.dp.toPx() to bottom + 50.dp.toPx()
        HomeTutorialAlignment.Step2 -> left to top - 30.dp.toPx()
        HomeTutorialAlignment.Step3 -> right - 150.dp.toPx() to top - 70.dp.toPx()
        HomeTutorialAlignment.Step4 -> left to top - 40.dp.toPx()
        HomeTutorialAlignment.Step5 -> left + -30.dp.toPx() to bottom + 50.dp.toPx()
        HomeTutorialAlignment.Step6 -> right - 60.dp.toPx() to top - 70.dp.toPx()
        HomeTutorialAlignment.Step7 -> left to top - 40.dp.toPx()
        HomeTutorialAlignment.Step8 -> left + 10.dp.toPx() to bottom + 10.dp.toPx()
    }
}

private fun HomeTutorialStep.tooltipOffset(rect: Rect, density: androidx.compose.ui.unit.Density): Pair<Dp, Dp> = with(density) {
    val left = rect.left.toDp()
    val top = rect.top.toDp()
    val right = rect.right.toDp()
    val bottom = rect.bottom.toDp()
    return when (tooltipAlignment) {
        HomeTutorialAlignment.Step1 -> left + 40.dp to bottom + 20.dp
        HomeTutorialAlignment.Step2 -> left to top - 60.dp
        HomeTutorialAlignment.Step3 -> right - 170.dp to top - 100.dp
        HomeTutorialAlignment.Step4 -> left to top - 80.dp
        HomeTutorialAlignment.Step5 -> right - 60.dp to bottom + 25.dp
        HomeTutorialAlignment.Step6 -> right - 240.dp to top - 60.dp
        HomeTutorialAlignment.Step7 -> left to top - 80.dp
        HomeTutorialAlignment.Step8 -> right - 220.dp to bottom + 50.dp
    }
}

private fun DrawScope.drawDashedCurvedArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    dashLength: Float,
    gapLength: Float,
    arrowSize: Float,
    curvature: Float,
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val controlX = start.x + dx * 0.5f - dy * curvature
    val controlY = start.y + dy * 0.5f + dx * curvature

    val curvePath = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(controlX, controlY, end.x, end.y)
    }
    val measure = PathMeasure()
    measure.setPath(curvePath, false)
    val curveLength = measure.length
    val arrowReserve = arrowSize * 1.6f
    val dashedPath = Path()
    measure.getSegment(
        startDistance = 0f,
        stopDistance = (curveLength - arrowReserve).coerceAtLeast(0f),
        destination = dashedPath,
        startWithMoveTo = true,
    )

    drawPath(
        path = dashedPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength, gapLength),
                phase = 0f,
            ),
        ),
    )

    val tangentOffset = measure.getTangent(curveLength - 1f)
    val angle = atan2(tangentOffset.y, tangentOffset.x)
    val leftAngle = angle + PI.toFloat() * 5f / 6f
    val rightAngle = angle - PI.toFloat() * 5f / 6f
    val arrowPath = Path().apply {
        moveTo(end.x + arrowSize * cos(leftAngle), end.y + arrowSize * sin(leftAngle))
        lineTo(end.x, end.y)
        lineTo(end.x + arrowSize * cos(rightAngle), end.y + arrowSize * sin(rightAngle))
    }

    drawPath(
        path = arrowPath,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

private enum class HomeTutorialPage(
    val index: Int,
) {
    First(index = 1),
    Second(index = 2),
    Third(index = 3),
}

private fun HomeTutorialPage.steps(
    subjectTabRect: Rect?,
    uploadButtonRect: Rect?,
    quizButtonRect: Rect?,
    profileCardRect: Rect?,
    examCardRect: Rect?,
    activityTabRect: Rect?,
    fabRect: Rect?,
): List<HomeTutorialStep> = when (this) {
    HomeTutorialPage.First -> listOf(
        HomeTutorialStep(
            step = 1,
            startText = "공부하고 싶은 과목을 추가해\n",
            highlightedText = "챕터, 파트",
            endText = " 별로 분류해 보관할\n수 있어요",
            anchorRect = subjectTabRect,
            tooltipAlignment = HomeTutorialAlignment.Step1,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -10f),
            arrowCurvature = 0.3f,
        ),
        HomeTutorialStep(
            step = 2,
            startText = "나의 강의 자료를 ",
            highlightedText = "pdf, 이미지,\n텍스트",
            endText = "로 업로드할 수 있어요",
            anchorRect = uploadButtonRect,
            tooltipAlignment = HomeTutorialAlignment.Step2,
            arrowStartOffset = Offset(-150f, -60f),
            arrowEndOffset = Offset(40f, -30f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 3,
            startText = "업로드한 강의를 기반으로\n",
            highlightedText = "AI가 퀴즈를 만들어줘요",
            endText = "",
            anchorRect = quizButtonRect,
            tooltipAlignment = HomeTutorialAlignment.Step3,
            arrowStartOffset = Offset(30f, 0f),
            arrowEndOffset = Offset(40f, -50f),
            arrowCurvature = 0.3f,
        ),
    )

    HomeTutorialPage.Second -> listOf(
        HomeTutorialStep(
            step = 4,
            startText = "누르면 마이페이지로 이동해요.퀴즈로\n모은 ",
            highlightedText = "도토리",
            endText = "를 쓸 수 있어요!",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step4,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 5,
            startText = "최근에 생성하고 풀어본 퀴즈",
            highlightedText = " 항목",
            endText = "을 볼 수 있어요",
            anchorRect = activityTabRect,
            tooltipAlignment = HomeTutorialAlignment.Step5,
            arrowStartOffset = Offset(-30f, -1f),
            arrowEndOffset = Offset(0f, -15f),
            arrowCurvature = 0.3f,
        ),
        HomeTutorialStep(
            step = 6,
            startText = "플로팅 버튼으로도 ",
            highlightedText = "과목 추가,\n강의 업로드, 퀴즈 만들기",
            endText = " 등을 모두 할 수 있어요!",
            anchorRect = fabRect,
            tooltipAlignment = HomeTutorialAlignment.Step6,
            arrowStartOffset = Offset(0f, 0f),
            arrowEndOffset = Offset(-40f, -30f),
            arrowCurvature = 0.3f,
        ),
    )

    HomeTutorialPage.Third -> listOf(
        HomeTutorialStep(
            step = 7,
            startText = "퀴켓의 마스코트 다람쥐,\n",
            highlightedText = "",
            endText = "'큐링이'에요.",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step7,
            arrowStartOffset = Offset(-160f, 5f),
            arrowEndOffset = Offset(-30f, -55f),
            arrowCurvature = -0.3f,
        ),
        HomeTutorialStep(
            step = 8,
            startText = "획득한 도토리를 통해 '도토리\n",
            highlightedText = "",
            endText = "상점'에서 큐링이를 위한 아이\n템을 구매할 수 있게 돼요!",
            anchorRect = profileCardRect,
            tooltipAlignment = HomeTutorialAlignment.Step8,
            arrowStartOffset = Offset(100f, 0f),
            arrowEndOffset = Offset(170f, 50f),
            arrowCurvature = 0.3f,
        ),
    )
}

private data class HomeTutorialStep(
    val step: Int,
    val startText: String,
    val highlightedText: String,
    val endText: String,
    val anchorRect: Rect?,
    val tooltipAlignment: HomeTutorialAlignment,
    val arrowStartOffset: Offset,
    val arrowEndOffset: Offset,
    val arrowCurvature: Float,
)

private enum class HomeTutorialAlignment {
    Step1,
    Step2,
    Step3,
    Step4,
    Step5,
    Step6,
    Step7,
    Step8,
}

@Composable
private fun HomeUploadSubjectPickerScreen(
    homeState: HomeUiState,
    starredSubjectIds: Set<String>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (SubjectSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subjects = (homeState as? HomeUiState.Success)
        ?.data
        ?.subjects
        .orEmpty()
        .sortedForDisplay(starredSubjectIds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = MainBottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HomeUploadPickerTopBar(onBackClick = onBackClick)
        Text(
            text = "자료를 추가할 과목을 선택해주세요",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyMedium,
        )
        QuiketPrimaryButton(
            text = "새 과목 추가하기",
            enabled = true,
            onClick = onAddSubjectClick,
            containerColor = QuiketWhite,
            contentColor = QuiketGray950,
            modifier = Modifier.border(2.dp, QuiketBrown950, RoundedCornerShape(12.dp)),
        )

        when (homeState) {
            HomeUiState.Loading -> HomeSummaryCard(
                title = "과목을 불러오는 중",
                description = "업로드할 과목 목록을 확인하고 있어요.",
            )

            is HomeUiState.Error -> HomeErrorCard(
                title = "과목 목록을 불러오지 못했어요",
                message = homeState.message,
                onRetry = onRetryClick,
            )

            is HomeUiState.Success -> {
                if (subjects.isEmpty()) {
                    HomeSummaryCard(
                        title = "아직 과목이 없어요",
                        description = "자료를 업로드하려면 먼저 과목을 추가해주세요.",
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        subjects.forEach { subject ->
                            HomeUploadSubjectPickerCard(
                                subject = subject,
                                isStarred = subject.id in starredSubjectIds,
                                onClick = { onSubjectClick(subject) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeUploadPickerTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "‹",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .semantics { contentDescription = "뒤로" }
                .clickable(role = Role.Button, onClick = onBackClick),
        )
        Text(
            text = "자료 업로드",
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun HomeUploadSubjectPickerCard(
    subject: SubjectSummary,
    isStarred: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "자료 업로드 과목 ${subject.name}"
            },
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(QuiketBrown50),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = subject.name.take(1),
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subject.name,
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (isStarred) {
                        Text(
                            text = "★",
                            color = QuiketOrange500,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                Text(
                    text = "${subject.chapterCount}챕터 · ${subject.partCount}파트",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = ">",
                color = QuiketGray400,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun HomeSummaryCard(
    homeData: HomeData,
    modifier: Modifier = Modifier,
) {
    val levelName = homeData.user.levelName?.takeIf { it.isNotBlank() } ?: "Lv.${homeData.user.currentLevel}"
    HomeSummaryCard(
        title = "$levelName · 도토리 ${homeData.user.dotoriBalance}개",
        description = "과목 ${homeData.subjects.size}개와 최근 활동 ${homeData.recentActivities.size}개를 이어서 볼 수 있어요.",
        modifier = modifier,
    )
}

@Composable
private fun HomeSummaryCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    @Composable
    fun Content() {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(QuiketOrange500),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_acorn),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = QuiketWhite,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = description,
                    color = QuiketGray300,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = QuiketBrown950,
            shape = RoundedCornerShape(24.dp),
        ) {
            Content()
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            color = QuiketBrown950,
            shape = RoundedCornerShape(24.dp),
        ) {
            Content()
        }
    }
}

@Composable
private fun HomeErrorCard(
    message: String,
    onRetry: () -> Unit,
    title: String = "홈 정보를 불러오지 못했어요",
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = message,
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
            QuiketPrimaryButton(
                text = "다시 시도",
                enabled = true,
                onClick = onRetry,
            )
        }
    }
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
private fun HistoryTab(
    state: HistoryUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onActivityClick: (HistoryActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50),
    ) {
        QuiketTopBar(onNoteIconClick = {})

        Text(
            text = "퀴즈 기록",
            color = QuiketGray950,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp),
        )

        when {
            state.isLoading && state.activities.isEmpty() -> {
                HistoryMessage(
                    text = "기록을 불러오는 중이에요",
                    modifier = Modifier.weight(1f),
                )
            }

            state.activities.isEmpty() -> {
                HistoryEmpty(
                    message = state.errorMessage ?: "아직 퀴즈 기록이 없어요",
                    retryVisible = state.errorMessage != null,
                    onRetryClick = onRefresh,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(
                        items = state.activities,
                        key = { _, activity -> activity.activityId },
                    ) { index, activity ->
                        HistoryActivityCard(
                            activity = activity,
                            position = index + 1,
                            onClick = { onActivityClick(activity) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    item {
                        when {
                            state.isLoadingMore -> HistoryMessage(
                                text = "더 불러오는 중이에요",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            )

                            state.hasNext -> QuiketPrimaryButton(
                                text = "더 보기",
                                enabled = true,
                                onClick = onLoadMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryActivityCard(
    activity: HistoryActivity,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = activity.isActionClickable,
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = activity.accessibilityLabel(position)
                if (activity.isActionClickable) {
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = QuiketWhite,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HistoryStatusChip(activity = activity)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = activity.createdAt.toDateLabel(),
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                )
            }

            Text(
                text = activity.title,
                color = QuiketGray950,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = activity.subjectName,
                    color = QuiketGray700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                activity.scoreText?.takeIf { it.isNotBlank() }?.let { score ->
                    Text(
                        text = score,
                        color = QuiketBrown950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                activity.progressPct?.let { progress ->
                    Text(
                        text = "$progress%",
                        color = QuiketGray700,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryStatusChip(
    activity: HistoryActivity,
    modifier: Modifier = Modifier,
) {
    val label = activity.statusLabel()
    val completed = activity.activityType == HistoryActivityType.QuizCompleted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (completed) QuiketBrown50 else QuiketGray100)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (completed) QuiketOrange500 else QuiketGray700,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun HistoryEmpty(
    message: String,
    retryVisible: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
        if (retryVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            QuiketPrimaryButton(
                text = "다시 불러오기",
                enabled = true,
                onClick = onRetryClick,
            )
        }
    }
}

@Composable
private fun HistoryMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketGray400,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
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

@Composable
private fun MyPageDashboardTab(
    nickname: String?,
    state: MyPageUiState,
    onRetry: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myPageData = (state as? MyPageUiState.Success)?.data
    val displayNickname = myPageData?.profile?.nickname?.takeIf { it.isNotBlank() }
        ?: nickname
        ?: "사용자"
    val level = (myPageData?.gamification?.currentLevel ?: myPageData?.profile?.currentLevel ?: 1)
        .coerceIn(1, 10)
    val dotori = myPageData?.gamification?.dotoriBalance ?: myPageData?.profile?.dotoriBalance ?: 0
    val xpTotal = myPageData?.gamification?.xpTotal ?: myPageData?.profile?.xpTotal ?: 0
    val progress = ((myPageData?.gamification?.levelProgressPct ?: levelProgressPct(level, xpTotal)) / 100f)
        .coerceIn(0f, 1f)
    val unlockedItems = remember(level) { unlockedRoomItems(level) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        MyPageDashboardTopBar(
            onSettingsClick = onSettingsClick,
        )

        when (state) {
            MyPageUiState.Idle,
            MyPageUiState.Loading,
            -> {
                Spacer(modifier = Modifier.height(18.dp))
                MyPageInfoCard(
                    title = "프로필을 불러오는 중",
                    rows = listOf("학습 정보를 확인하고 있어요."),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            is MyPageUiState.Error -> {
                Spacer(modifier = Modifier.height(18.dp))
                HomeErrorCard(
                    message = state.message,
                    onRetry = onRetry,
                    title = "프로필을 불러오지 못했어요",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            is MyPageUiState.Success -> {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(roomBackgroundColor(level)),
                ) {
                    LevelProfileCard(
                        nickname = displayNickname,
                        level = level,
                        progress = progress,
                    )
                    CharacterRoomSection(
                        level = level,
                        unlockedItems = unlockedItems,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                MyPageStatsRow(
                    acornCount = dotori,
                    streakDays = 0,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                AcornShopButton(
                    isLocked = true,
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MyPageDashboardTopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "마이페이지",
            color = QuiketGray950,
            fontSize = 17.sp,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(Res.drawable.ic_topbar_setting),
            contentDescription = "설정",
            tint = QuiketGray700,
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSettingsClick)
        )
    }
}

@Composable
private fun LevelProfileCard(
    nickname: String,
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    var progressAnimationStarted by remember(progress) { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (progressAnimationStarted) progress else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "levelProgress",
    )

    LaunchedEffect(progress) {
        progressAnimationStarted = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(QuiketWhite),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_qring_profile),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(level = level)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = nickname.ifBlank { "User" },
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = QuiketGray950,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(horizontal = 15.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(QuiketGray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(QuiketOrange500),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun LevelBadge(
    level: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(1.dp, QuiketOrange500, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF1E8))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Lv.$level",
            color = QuiketOrange500,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun CharacterRoomSection(
    level: Int,
    unlockedItems: Set<RoomItem>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(roomBackgroundColor(level)),
    ) {
        if (RoomItem.Rug in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_carpet),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
                    .size(width = 240.dp, height = 64.dp),
            )
        }
        if (RoomItem.Sofa in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_sofa),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(width = 150.dp, height = 150.dp),
            )
        }
        if (RoomItem.Plant in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_flower),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 80.dp, y = (-20).dp)
                    .size(56.dp),
            )
        }
        if (RoomItem.Clock in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_clock),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 40.dp, y = 10.dp)
                    .size(56.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_my_qring),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
            Image(
                painter = painterResource(Res.drawable.ic_speech_balloon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 50.dp, y = (-30).dp),
            )
        }
    }
}

@Composable
private fun MyPageStatsRow(
    acornCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MyPageStatCard(
            icon = Res.drawable.ic_my_acorn,
            label = "보유 도토리",
            value = "${acornCount}개",
            modifier = Modifier.weight(1f),
        )
        MyPageStatCard(
            icon = Res.drawable.ic_my_fire,
            label = "연속 학습",
            value = "${streakDays}일",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyPageStatCard(
    icon: DrawableResource,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(1.dp, QuiketGray100, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketWhite)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(QuiketBrown950)
                .padding(8.dp),
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = QuiketGray700,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = QuiketBrown950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun AcornShopButton(
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, QuiketGray100, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(QuiketWhite)
                .clickable(enabled = !isLocked, role = Role.Button, onClick = onClick)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_my_store),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "도토리 상점",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_my_lock),
                    contentDescription = "잠금",
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

private enum class RoomItem {
    BasicRoom,
    Plant,
    Rug,
    Sofa,
    Clock,
}

private fun unlockedRoomItems(level: Int): Set<RoomItem> = buildSet {
    add(RoomItem.BasicRoom)
    if (level >= 2) add(RoomItem.Plant)
    if (level >= 3) add(RoomItem.Rug)
    if (level >= 4) add(RoomItem.Sofa)
    if (level >= 5) add(RoomItem.Clock)
}

private fun roomBackgroundColor(level: Int): Color = when (level.coerceIn(1, 5)) {
    1 -> Color(0xFFFFEDD5)
    2 -> Color(0xFFDFF2E1)
    3 -> Color(0xFFFFF9C4)
    4 -> Color(0xFFE3F2FD)
    else -> Color(0xFFD6EAF8)
}

private fun levelProgressPct(level: Int, totalXp: Int): Int {
    val currentRequired = levelRequiredXp(level)
    val nextRequired = nextLevelRequiredXp(level) ?: return 100
    val progress = (totalXp - currentRequired).coerceAtLeast(0).toFloat() /
        (nextRequired - currentRequired).coerceAtLeast(1).toFloat()
    return (progress.coerceIn(0f, 1f) * 100).toInt()
}

private fun levelRequiredXp(level: Int): Int = when (level.coerceIn(1, 10)) {
    1 -> 0
    2 -> 100
    3 -> 350
    4 -> 850
    5 -> 1750
    6 -> 3250
    7 -> 5750
    8 -> 9750
    9 -> 16250
    else -> 26250
}

private fun nextLevelRequiredXp(level: Int): Int? = when (level.coerceIn(1, 10)) {
    1 -> 100
    2 -> 350
    3 -> 850
    4 -> 1750
    5 -> 3250
    6 -> 5750
    7 -> 9750
    8 -> 16250
    9 -> 26250
    else -> null
}

@Composable
private fun MyPageInfoCard(
    title: String,
    rows: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            rows.forEach { row ->
                Text(
                    text = row,
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private enum class MyPageDestination {
    Settings,
    AccountSettings,
    NotificationSettings,
    Inquiry,
    Terms,
    PrivacyPolicy,
}

private enum class HomeContentTab {
    Subjects,
    Recent,
}

private data class HomeExamUiModel(
    val id: String,
    val name: String,
    val date: String,
    val dDay: Int,
) {
    val dDayLabel: String
        get() = dDayLabel(dDay)

    val isUrgent: Boolean
        get() = dDay in 0..7
}

private fun dDayLabel(dDay: Int): String = when {
    dDay > 0 -> "D-$dDay"
    dDay == 0 -> "D-Day"
    else -> "D+${-dDay}"
}

private fun HomeData?.toHomeExams(): List<HomeExamUiModel> {
    val schedules = this?.dDayCards.orEmpty() + this?.subjects.orEmpty().mapNotNull { it.examSchedule }
    return schedules
        .distinctBy { schedule -> schedule.id }
        .mapNotNull { schedule ->
            val resolvedDDay = schedule.resolvedDDay() ?: return@mapNotNull null
            HomeExamUiModel(
                id = schedule.id,
                name = schedule.examName,
                date = schedule.examDate.toHomeDateLabel(),
                dDay = resolvedDDay,
            )
        }
        .filter { exam -> exam.dDay >= -7 }
        .withIndex()
        .sortedWith(
            Comparator { first, second ->
                val firstDay = first.value.dDay
                val secondDay = second.value.dDay
                val dayOrder = when {
                    firstDay >= 0 && secondDay >= 0 -> firstDay.compareTo(secondDay)
                    firstDay < 0 && secondDay < 0 -> secondDay.compareTo(firstDay)
                    firstDay >= 0 -> -1
                    else -> 1
                }
                if (dayOrder != 0) dayOrder else second.index - first.index
            },
        )
        .map { it.value }
        .take(5)
}

private fun SubjectExamSchedule.resolvedDDay(): Int? =
    dDay ?: parseHomeDate(examDate)
        ?.let { date -> date.toEpochDays() - currentHomeDate().toEpochDays() }
        ?.toInt()

private fun String.toHomeDateLabel(): String {
    val parsedDate = parseHomeDate(this) ?: return this
    return "${parsedDate.year}-${parsedDate.monthNumberValue().twoDigits()}-${parsedDate.day.twoDigits()}"
}

private fun parseHomeDate(raw: String): LocalDate? {
    val parts = raw.take(10).split("-", ".")
    if (parts.size < 3) return null
    return runCatching {
        LocalDate(
            year = parts[0].toInt(),
            month = parts[1].toInt().toHomeMonth(),
            day = parts[2].toInt(),
        )
    }.getOrNull()
}

private fun Int.toHomeMonth(): Month =
    Month.entries[this - 1]

private fun LocalDate.monthNumberValue(): Int =
    month.ordinal + 1

private fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()

@OptIn(ExperimentalTime::class)
private fun currentHomeDate(): LocalDate =
    Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun String.toDateLabel(): String =
    substringBefore('T').takeIf { it.isNotBlank() } ?: this

private fun List<SubjectSummary>.sortedForDisplay(
    starredSubjectIds: Set<String>,
): List<SubjectSummary> =
    filter { subject -> subject.id in starredSubjectIds }.sortedBy { it.name } +
        filterNot { subject -> subject.id in starredSubjectIds }

private val MainBottomContentPadding = 112.dp

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

private val HistoryActivity.isActionClickable: Boolean
    get() = when (activityType) {
        HistoryActivityType.QuizCompleted -> resultId != null || playSessionId != null
        HistoryActivityType.QuizReady,
        HistoryActivityType.QuizInProgress -> quizSessionId != null
        HistoryActivityType.QuizGenerating,
        HistoryActivityType.LectureUploaded,
        HistoryActivityType.Unknown -> false
    }

private fun HistoryActivity.accessibilityLabel(position: Int): String =
    listOfNotNull(
        "기록 $position",
        title.takeIf { it.isNotBlank() },
        subjectName.takeIf { it.isNotBlank() },
        statusLabel(),
        scoreText?.takeIf { it.isNotBlank() },
        progressPct?.let { "$it%" },
    ).joinToString(", ")

private fun HistoryActivity.statusLabel(): String = when (activityType) {
    HistoryActivityType.QuizGenerating -> "생성 중"
    HistoryActivityType.QuizReady -> "풀기 전"
    HistoryActivityType.QuizInProgress -> "진행 중"
    HistoryActivityType.QuizCompleted -> "완료"
    HistoryActivityType.LectureUploaded -> "업로드"
    HistoryActivityType.Unknown -> status?.takeIf { it.isNotBlank() } ?: "기록"
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

private enum class MainTab(
    val label: String,
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource,
) {
    Home(
        label = "홈",
        selectedIcon = Res.drawable.ic_bottom_home_primary,
        unselectedIcon = Res.drawable.ic_bottom_home_gray,
    ),
    History(
        label = "기록",
        selectedIcon = Res.drawable.ic_bottom_record_primary,
        unselectedIcon = Res.drawable.ic_bottom_record_gray,
    ),
    Review(
        label = "오답노트",
        selectedIcon = Res.drawable.ic_bottom_review_primary,
        unselectedIcon = Res.drawable.ic_bottom_review_gray,
    ),
    MyPage(
        label = "마이",
        selectedIcon = Res.drawable.ic_bottom_my_primary,
        unselectedIcon = Res.drawable.ic_bottom_my_gray,
    ),
}
