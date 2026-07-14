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


internal val MainTabTopPadding = 24.dp

@Composable
internal fun HomeTab(
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
