package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.f1.quiket.core.designsystem.component.HomeActionButton
import com.f1.quiket.core.designsystem.component.HomeEmptyExamCard
import com.f1.quiket.core.designsystem.component.HomeExamCard
import com.f1.quiket.core.designsystem.component.HomeProfileCard
import com.f1.quiket.core.designsystem.component.NoSubjectPopup
import com.f1.quiket.core.designsystem.component.QuiketTopBar
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.f1.quiket.feature.floating.domain.model.AddSubjectState
import com.f1.quiket.feature.floating.domain.model.Chapter
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import com.f1.quiket.feature.floating.domain.model.examTypeFromBackendValue
import com.f1.quiket.feature.floating.presentation.screen.UploadScreen
import com.f1.quiket.feature.floating.presentation.screen.addsubject.AddSubjectScreen
import com.f1.quiket.feature.floating.presentation.screen.addsubject.AddSubjectStep2Screen
import com.f1.quiket.feature.floating.presentation.screen.addsubject.AddSubjectStep3Screen
import com.f1.quiket.feature.floating.presentation.viewmodel.AddSubjectViewModel
import com.f1.quiket.feature.floating.presentation.screen.lectureselect.LectureSelectScreen
import com.f1.quiket.feature.floating.presentation.screen.lectureview.LectureViewScreen
import com.f1.quiket.feature.floating.presentation.screen.materialcheck.MaterialCheckScreen
import com.f1.quiket.feature.floating.presentation.screen.subjectdetail.SubjectDetailScreen
import com.f1.quiket.feature.home.presentation.component.ExpandableFab
import com.f1.quiket.feature.home.presentation.component.HomeGuideTooltip
import com.f1.quiket.feature.home.presentation.component.HomeTutorialOverlay
import com.f1.quiket.feature.home.domain.model.HomeData
import com.f1.quiket.feature.home.domain.model.RecentActivity
import com.f1.quiket.feature.home.presentation.model.Activity
import com.f1.quiket.feature.home.presentation.model.ActivityType
import com.f1.quiket.feature.home.presentation.model.Exam
import com.f1.quiket.feature.home.presentation.model.FabAction
import com.f1.quiket.feature.home.presentation.model.Subject
import com.f1.quiket.feature.home.presentation.model.TutorialPage
import com.f1.quiket.feature.home.presentation.model.buildTutorialPages
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    uiState: HomeState,
    isQuizGenerating: Boolean = false,
    hasActiveQuizSession: Boolean = false,
    onBoardingDone: () -> Unit,
    onQuizCardClick: () -> Unit,
    onQuizActionClick: () -> Unit,
    onQuizResultClick: (String) -> Unit = {},
    onFabItemClick: (FabAction) -> Unit,
    onHomeRefresh: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onExamCardClick: () -> Unit = {},
) {
    val editSubjectViewModel: AddSubjectViewModel = hiltViewModel()
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showNoSubjectPopup by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    // 과목 유형 수정 편집 상태
    var editSubjectId by remember { mutableStateOf("") }
    var editPurpose by remember { mutableStateOf<StudyPurpose?>(null) }
    var editExamType by remember { mutableStateOf<ExamType?>(null) }
    var editStudyField by remember { mutableStateOf<StudyField?>(null) }
    var editUsagePurpose by remember { mutableStateOf<UsagePurpose?>(null) }
    var subjectDetailRefreshTrigger by remember { mutableIntStateOf(0) }
    var depth by remember { mutableStateOf(0) }
    val homeData = uiState.homeData
    val serverSubjects = remember(homeData?.subjects) {
        homeData.toHomeSubjects()
    }
    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

    LaunchedEffect(serverSubjects) {
        subjects = serverSubjects
    }

    LaunchedEffect(editSubjectViewModel) {
        editSubjectViewModel.updateDetailsSuccess.collect {
            subjectDetailRefreshTrigger++
        }
    }

    // 강의 선택 후 UploadScreen에 넘길 정보
    var selectedLectureId by remember { mutableStateOf("") }
    var selectedLectureTitle by remember { mutableStateOf("") }
    var selectedLectureChapterCount by remember { mutableStateOf(0) }
    var selectedLectureCategory by remember { mutableStateOf("") }
    // SubjectDetail에서 챕터 추가/업로드 시 넘길 챕터 번호
    var nextChapterNumber by remember { mutableStateOf(1) }
    // 업로드 성공 후 MaterialCheckScreen/LectureViewScreen에 넘길 챕터 정보
    var uploadedLectureUploadId by remember { mutableStateOf("") }
    var uploadedSubjectId by remember { mutableStateOf("") }
    var uploadedChapterId by remember { mutableStateOf("") }
    var uploadedChapterName by remember { mutableStateOf("") }
    var uploadedChapterNumber by remember { mutableStateOf(1) }
    var uploadedPartCount by remember { mutableStateOf(0) }
    var lectureViewBackDepth by remember { mutableStateOf(0) }

    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    BackHandler(enabled = depth > 0) {
        when (depth) {
            1 -> { depth = 0; onHomeRefresh() }
            2 -> depth = 1
            3 -> depth = 1
            4 -> depth = 2
            5 -> depth = 0  // 홈 업로드용 LectureSelect → 홈
            6 -> depth = 5  // 홈 업로드용 UploadScreen → LectureSelect
            9 -> depth = 5  // LectureSelect에서 AddSubject → LectureSelect로
            7 -> depth = lectureViewBackDepth
            8 -> depth = lectureViewBackDepth  // MaterialCheckScreen → 이전 화면
            11 -> depth = 1  // 과목 유형 수정 Step2 → 과목 상세
            12 -> depth = 11 // 과목 유형 수정 Step3 → Step2
            else -> {}
        }
    }

    // depth 1~6: SubjectDetail / LectureSelect(SubjectDetail) / Upload(SubjectDetail) /
    //            Upload(SubjectDetail+Lecture) / LectureSelect(홈) / Upload(홈+Lecture)
    when (depth) {
        1 -> {
            SubjectDetailScreen(
                subjectId = selectedSubject?.id ?: "",
                subjectName = selectedSubject?.title ?: "",
                initialIsStarred = selectedSubject?.isStarred ?: false,
                refreshTrigger = subjectDetailRefreshTrigger,
                onBackClick = { depth = 0; onHomeRefresh() },
                onExamScheduleSaved = onHomeRefresh,
                onUploadClick = { count -> nextChapterNumber = count + 1; depth = 3 },
                onChapterAddClick = { count -> nextChapterNumber = count + 1; depth = 3 },
                onStarToggle = { starred ->
                    subjects = subjects.map { s ->
                        if (s.id == selectedSubject?.id) s.copy(isStarred = starred) else s
                    }
                    selectedSubject = selectedSubject?.copy(isStarred = starred)
                },
                onEditSubjectType = { detail ->
                    val purpose = when (detail?.purpose?.lowercase()) {
                        "exam" -> StudyPurpose.EXAM
                        "review", "self_study" -> StudyPurpose.SELF_STUDY
                        "other" -> StudyPurpose.OTHER
                        else -> StudyPurpose.EXAM
                    }
                    val examType = detail?.examDetail?.examType?.let { examTypeFromBackendValue(it) }
                    editSubjectId = selectedSubject?.id ?: ""
                    editPurpose = purpose
                    editExamType = examType
                    editStudyField = detail?.reviewDetail?.field?.let { field ->
                        StudyField.entries.find { it.name.equals(field, ignoreCase = true) }
                    }
                    editUsagePurpose = detail?.otherDetail?.usagePurpose?.let { up ->
                        UsagePurpose.entries.find { it.name.equals(up, ignoreCase = true) }
                    }
                    // examType이 이미 정해진 경우 Step3으로, 아니면 Step2부터
                    depth = if (purpose == StudyPurpose.EXAM && examType != null) 12 else 11
                },
                onSubjectNameChanged = { newName ->
                    subjects = subjects.map { s ->
                        if (s.title == selectedSubject?.title) s.copy(title = newName) else s
                    }
                    selectedSubject = selectedSubject?.copy(title = newName)
                },
                onSubjectDeleted = {
                    subjects = subjects.filter { it.id != selectedSubject?.id }
                    depth = 0
                    onHomeRefresh()
                },
                onCreateQuizClick = { onFabItemClick(FabAction.CreateQuiz) },
            )
            return
        }

        11 -> {
            AddSubjectStep2Screen(
                studyPurpose = editPurpose ?: StudyPurpose.EXAM,
                onBackClick = { depth = 1 },
                onSkipClick = { depth = 1 },
                onNextClick = { selection ->
                    when (selection) {
                        is ExamType -> { editExamType = selection; editStudyField = null; editUsagePurpose = null }
                        is StudyField -> { editStudyField = selection; editExamType = null; editUsagePurpose = null }
                        is UsagePurpose -> { editUsagePurpose = selection; editExamType = null; editStudyField = null }
                    }
                    depth = 12
                },
            )
            return
        }

        12 -> {
            AddSubjectStep3Screen(
                studyPurpose = editPurpose ?: StudyPurpose.EXAM,
                examType = editExamType,
                studyField = editStudyField,
                usagePurpose = editUsagePurpose,
                onBackClick = { depth = 11 },
                onSkipClick = {
                    val skipState = AddSubjectState(
                        subjectName = selectedSubject?.title ?: "",
                        studyPurpose = editPurpose,
                        examType = editExamType,
                        studyField = editStudyField,
                        usagePurpose = editUsagePurpose,
                    )
                    editSubjectViewModel.updateSubjectDetails(editSubjectId, skipState)
                    depth = 1
                },
                onCreateClick = { _, stateTransformer ->
                    val initState = AddSubjectState(
                        subjectName = selectedSubject?.title ?: "",
                        studyPurpose = editPurpose,
                        examType = editExamType,
                        studyField = editStudyField,
                        usagePurpose = editUsagePurpose,
                    )
                    val finalState = stateTransformer(initState)
                    editSubjectViewModel.updateSubjectDetails(editSubjectId, finalState)
                    depth = 1
                },
            )
            return
        }

        2 -> {
            LectureSelectScreen(
                onBackClick = { depth = 1 },
                onLectureSelected = { lectureId, title, count, category ->
                    selectedLectureId = lectureId
                    selectedLectureTitle = title
                    selectedLectureChapterCount = count
                    selectedLectureCategory = category
                    depth = 4
                },
                onAddSubjectClick = { onFabItemClick(FabAction.AddSubject) },
            )
            return
        }

        3 -> {
            UploadScreen(
                subjectId = selectedSubject?.id,
                chapterTitle = selectedSubject?.title,
                lecturePurpose = selectedSubject?.purpose?.ifBlank { null },
                chapterCount = nextChapterNumber - 1,
                onBackClick = { depth = 1 },
                onNextClick = { depth = 1 },
                onUploadSuccess = { lectureUploadId, chapterNum ->
                    uploadedLectureUploadId = lectureUploadId
                    uploadedChapterNumber = chapterNum
                    lectureViewBackDepth = 1
                    depth = 8
                },
            )
            return
        }

        4 -> {
            UploadScreen(
                subjectId = selectedLectureId,
                chapterTitle = selectedLectureTitle,
                lecturePurpose = selectedLectureCategory.ifBlank { null },
                chapterCount = selectedLectureChapterCount,
                onBackClick = { depth = 2 },
                onNextClick = { depth = 1 },
                onUploadSuccess = { lectureUploadId, chapterNum ->
                    uploadedLectureUploadId = lectureUploadId
                    uploadedChapterNumber = chapterNum
                    lectureViewBackDepth = 1
                    depth = 8
                },
            )
            return
        }

        5 -> {
            LectureSelectScreen(
                onBackClick = { depth = 0 },
                onLectureSelected = { lectureId, title, count, category ->
                    selectedLectureId = lectureId
                    selectedLectureTitle = title
                    selectedLectureChapterCount = count
                    selectedLectureCategory = category
                    depth = 6
                },
                onAddSubjectClick = { depth = 9 },
            )
            return
        }

        9 -> {
            AddSubjectScreen(
                onFinish = { depth = 5 },
                onDismiss = { depth = 5 },
            )
            return
        }

        6 -> {
            UploadScreen(
                subjectId = selectedLectureId,
                chapterTitle = selectedLectureTitle,
                lecturePurpose = selectedLectureCategory.ifBlank { null },
                chapterCount = selectedLectureChapterCount,
                onBackClick = { depth = 5 },
                onNextClick = { depth = 0 },
                onUploadSuccess = { lectureUploadId, chapterNum ->
                    uploadedLectureUploadId = lectureUploadId
                    uploadedChapterNumber = chapterNum
                    // SubjectDetailScreen에서 보여줄 과목 정보 설정
                    selectedSubject = Subject(
                        id = selectedLectureId,
                        title = selectedLectureTitle,
                        chapter = "챕터 $selectedLectureChapterCount",
                        purpose = selectedLectureCategory,
                        isStarred = false,
                    )
                    lectureViewBackDepth = 1
                    depth = 8
                },
            )
            return
        }

        7 -> {
            LectureViewScreen(
                subjectId = uploadedSubjectId,
                chapter = Chapter(
                    id = uploadedChapterId,
                    number = uploadedChapterNumber,
                    name = uploadedChapterName,
                    partCount = uploadedPartCount,
                ),
                onBackClick = { depth = lectureViewBackDepth },
            )
            return
        }

        8 -> {
            MaterialCheckScreen(
                lectureUploadId = uploadedLectureUploadId,
                chapterNumber = uploadedChapterNumber,
                onBackClick = { depth = lectureViewBackDepth },
                onComplete = { subjectId, chapterId, chapterName, partCount ->
                    uploadedSubjectId = subjectId
                    uploadedChapterId = chapterId
                    uploadedChapterName = chapterName
                    uploadedPartCount = partCount
                    depth = 7
                },
            )
            return
        }
    }
    val quizGeneratingText = rememberQuizGeneratingText(isQuizGenerating)
    val quizActionText = when {
        isQuizGenerating -> quizGeneratingText
        hasActiveQuizSession -> "퀴즈 풀기"
        else -> "퀴즈 만들기"
    }

    // 온보딩 툴팁
    var noteIconOffset by remember { mutableStateOf(Offset.Zero) }
    var noteIconSize by remember { mutableStateOf(IntSize.Zero) }
    var homeBoxOffset by remember { mutableStateOf(Offset.Zero) }

    // 튜토리얼 위치
    var uploadButtonRect by remember { mutableStateOf<Rect?>(null) }
    var quizButtonRect by remember { mutableStateOf<Rect?>(null) }
    var profileCardRect by remember { mutableStateOf<Rect?>(null) }
    var activityTabRect by remember { mutableStateOf<Rect?>(null) }
    var fabRect by remember { mutableStateOf<Rect?>(null) }
    var subjectTabRect by remember { mutableStateOf<Rect?>(null) }

    // 튜토리얼 상태
    var showTutorial by remember { mutableStateOf(false) }
    var tutorialPage by remember { mutableStateOf(TutorialPage.FIRST) }

    val exams = remember(homeData?.subjects) {
        homeData.toHomeExams()
    }
    val recentActivities = remember(homeData?.recentActivities) {
        homeData.toHomeActivities()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = White.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brown50)
            .onGloballyPositioned { coordinates ->
                homeBoxOffset = coordinates.positionInRoot()
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── TopBar (고정) ────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
            ) {
                QuiketTopBar(
                    onNoteIconClick = {
                        tutorialPage = TutorialPage.FIRST
                        showTutorial = true
                    },
                    onNoteIconPositioned = { offset, size ->
                        noteIconOffset = offset
                        noteIconSize = size
                    },
                )
            }

            // ── TopBar 아래 스크롤 영역 ──────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                // 상단 흰 카드 (제목 + 액션 버튼)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = White,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ) {
                        Text(
                            "오늘의 공부, 시작해 볼까요?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Black,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        )
                        Text(
                            "내 강의 노트를 업로드 하거나 퀴즈를 만들어 보세요 !",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Gray600
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { coords ->
                                        val pos = coords.positionInRoot()
                                        uploadButtonRect = Rect(
                                            pos.x, pos.y,
                                            pos.x + coords.size.width,
                                            pos.y + coords.size.height
                                        )
                                    }
                            ) {
                                HomeActionButton(
                                    text = "자료 업로드",
                                    iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_upload,
                                    backgroundColor = Gray100,
                                    onClick = {
                                        if (uiState.hasSubjects) depth = 5
                                        else showNoSubjectPopup = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { coords ->
                                        val pos = coords.positionInRoot()
                                        quizButtonRect = Rect(
                                            pos.x, pos.y,
                                            pos.x + coords.size.width,
                                            pos.y + coords.size.height
                                        )
                                    }
                            ) {
                                HomeActionButton(
                                    text = quizActionText,
                                    iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_make,
                                    backgroundColor = Orange500,
                                    onClick = onQuizActionClick,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isQuizGenerating) {
                                    QuizGeneratingSparkleOverlay(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                HomeProfileCard(
                    homeData?.user?.nickname ?: "닉네임",
                    homeData?.user?.dotoriBalance ?: 0,
                    com.f1.quiket.core.designsystem.R.drawable.ic_qring_profile,
                    onProfileClick,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            profileCardRect = Rect(
                                pos.x, pos.y,
                                pos.x + coords.size.width,
                                pos.y + coords.size.height
                            )
                        }
                )

                if (exams.isEmpty()) {
                    HomeEmptyExamCard(
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    val pagerState = rememberPagerState(pageCount = { exams.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 10.dp)
                    ) { page ->
                        HomeExamCard(
                            examName = exams[page].name,
                            date = exams[page].date,
                            dDay = exams[page].dDay,
                            onClick = onExamCardClick
                        )
                    }
                    if (exams.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp, top = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(exams.size) { index ->
                                val isActive = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) Brown950 else Gray200)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.padding(bottom = 10.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    TabItem(
                        "내 과목",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(0.7f)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                subjectTabRect = Rect(
                                    pos.x, pos.y,
                                    pos.x + coords.size.width,
                                    pos.y + coords.size.height
                                )
                            }
                    )
                    TabItem(
                        "최근 활동",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(0.7f)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                activityTabRect = Rect(
                                    pos.x, pos.y,
                                    pos.x + coords.size.width,
                                    pos.y + coords.size.height
                                )
                            }
                    )
                    Spacer(modifier = Modifier.weight(1.6f))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    color = White,
                    shape = RoundedCornerShape(topEnd = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxHeight()) {
                        if (selectedTab == 0) {
                            if (subjects.isEmpty()) {
                                EmptySubjectContent(
                                    onAddSubjectClick = { onFabItemClick(FabAction.AddSubject) },
                                )
                            } else {
                                ActiveSubjectContent(
                                    subjects = subjects,
                                    onSubjectsChange = { subjects = it },
                                    onSubjectAreaPositioned = {},
                                    onSubjectClick = { subject ->
                                        selectedSubject = subject
                                        depth = 1
                                    },
                                    onAddSubjectClick = { onFabItemClick(FabAction.AddSubject) },
                                )
                            }
                        } else {
                            if (recentActivities.isEmpty()) {
                                EmptyActivityContent()
                            } else {
                                ActiveActivityContent(
                                    activities = recentActivities,
                                    onActivityClick = { activity ->
                                        (activity.resultId ?: activity.playSessionId)
                                            ?.let(onQuizResultClick)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // 온보딩 툴팁
        if (uiState.showOnboarding && noteIconOffset != Offset.Zero) {
            val (yDp, endPadding) = rememberTooltipOffset(noteIconOffset, noteIconSize, homeBoxOffset)
            HomeGuideTooltip(
                onClose = onBoardingDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = yDp)
                    .padding(end = endPadding)
                    .zIndex(10f)
            )
        }

        // 튜토리얼 오버레이
        if (showTutorial) {
            val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val (firstPageSteps, secondPageSteps, thirdPageSteps) = buildTutorialPages(
                subjectTabRect = subjectTabRect,
                uploadButtonRect = uploadButtonRect,
                quizButtonRect = quizButtonRect,
                profileCardRect = profileCardRect,
                activityTabRect = activityTabRect,
                fabRect = fabRect,
            )
            HomeTutorialOverlay(
                currentPage = tutorialPage,
                firstPageSteps = firstPageSteps,
                secondPageSteps = secondPageSteps,
                thirdPageSteps = thirdPageSteps,
                onNext = {
                    when (tutorialPage) {
                        TutorialPage.FIRST -> tutorialPage = TutorialPage.SECOND
                        TutorialPage.SECOND -> tutorialPage = TutorialPage.THIRD
                        TutorialPage.THIRD -> showTutorial = false
                    }
                },
                onSkip = { showTutorial = false },
                statusBarHeight = statusBarHeight
            )
        }

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Gray900.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isExpanded = false }
            )
        }

        ExpandableFab(
            isExpanded = isExpanded,
            onFabClick = { isExpanded = !isExpanded },
            onItemClick = { action ->
                isExpanded = false
                when {
                    action == FabAction.Upload && !uiState.hasSubjects -> showNoSubjectPopup = true
                    action == FabAction.Upload -> depth = 5
                    else -> onFabItemClick(action)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    fabRect =
                        Rect(pos.x, pos.y, pos.x + coords.size.width, pos.y + coords.size.height)
                }
        )

        if (showNoSubjectPopup) {
            NoSubjectPopup(
                onAddSubject = {
                    showNoSubjectPopup = false
                    depth = 5
                    onFabItemClick(FabAction.AddSubject)
                },
                onDismiss = { showNoSubjectPopup = false },
            )
        }
    }
}

private fun HomeData?.toHomeSubjects(): List<Subject> =
    this?.subjects.orEmpty().map { subject ->
        Subject(
            id = subject.id,
            title = subject.name,
            chapter = "챕터 ${subject.chapterCount}",
            purpose = subject.purpose,
            isStarred = false,
        )
    }

private fun calcDDayInt(dateStr: String): Int =
    runCatching {
        val parts = dateStr.take(10).split(".", "-")
        val exam = java.util.Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(exam.timeInMillis - today.timeInMillis).toInt()
    }.getOrDefault(Int.MIN_VALUE)

private fun dDayToString(d: Int): String = when {
    d > 0 -> "D-$d"
    d == 0 -> "D-Day"
    else -> "D+${-d}"
}

private fun HomeData?.toHomeExams(): List<Exam> {
    // dDayCards + subjects[n].examSchedule 를 합쳐서 id 기준 중복 제거
    val cardExams = this?.dDayCards.orEmpty()
    val subjectExams = this?.subjects.orEmpty().mapNotNull { it.examSchedule }
    val allExams = (cardExams + subjectExams).distinctBy { it.id }

    return allExams
        .map { schedule -> schedule to (schedule.dDay ?: calcDDayInt(schedule.examDate)) }
        .filter { (_, d) -> d >= -7 }               // D+7(7일 전)까지 표시, D+8 이상 지난 시험 미노출
        .withIndex()
        .sortedWith(
            Comparator { a, b ->
                val (_, da) = a.value
                val (_, db) = b.value
                // D- 먼저(가까운 순), D+는 뒤에(D+1 → D+7 순)
                val order = when {
                    da >= 0 && db >= 0 -> da.compareTo(db)   // 둘 다 D-: 작은 수(가까운 날) 먼저
                    da < 0 && db < 0 -> db.compareTo(da)    // 둘 다 D+: 덜 지난 것(D+1) 먼저
                    da >= 0 -> -1                             // a가 D-, b가 D+: a 먼저
                    else -> 1                                 // a가 D+, b가 D-: b 먼저
                }
                if (order != 0) order else b.index - a.index // 동일 날짜 → 최신 등록 순
            }
        )
        .map { it.value }
        .take(5)                                     // 최대 5개
        .map { (schedule, d) ->
            Exam(
                name = schedule.examName,
                date = schedule.examDate,
                dDay = dDayToString(d),
            )
        }
}

private fun HomeData?.toHomeActivities(): List<Activity> =
    this?.recentActivities.orEmpty().map { activity ->
        Activity(
            title = activity.title,
            questionCount = activity.scoreText.toQuestionCount(),
            activityType = activity.toActivityType(),
            description = activity.toDescription(),
            progressPercent = activity.progressPct,
            isQuizCreated = activity.resultId != null ||
                activity.playSessionId != null ||
                activity.status == "submitted",
            quizSessionId = activity.quizSessionId,
            playSessionId = activity.playSessionId,
            resultId = activity.resultId,
        )
    }

private fun RecentActivity.toActivityType(): ActivityType =
    when {
        activityType.contains("ox", ignoreCase = true) -> ActivityType.OX_QUIZ
        activityType.contains("short", ignoreCase = true) -> ActivityType.SHORT_ANSWER
        else -> ActivityType.MULTIPLE_CHOICE
    }

private fun RecentActivity.toDescription(): String =
    when {
        !scoreText.isNullOrBlank() -> "결과 $scoreText"
        progressPct != null -> "진행률 ${progressPct}%"
        status == "completed" -> "퀴즈 생성이 완료됐어요"
        status == "in_progress" -> "퀴즈 생성 중이에요"
        else -> subjectName
    }

private fun String?.toQuestionCount(): Int =
    this
        ?.substringAfter("/", missingDelimiterValue = "")
        ?.filter(Char::isDigit)
        ?.toIntOrNull()
        ?: 0

@Composable
private fun rememberQuizGeneratingText(isGenerating: Boolean): String {
    var dotCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(isGenerating) {
        if (!isGenerating) {
            dotCount = 1
            return@LaunchedEffect
        }

        dotCount = 1
        while (true) {
            delay(1_000L)
            dotCount = if (dotCount == 3) 1 else dotCount + 1
        }
    }

    return if (isGenerating) {
        "퀴즈 생성 중${".".repeat(dotCount)}"
    } else {
        "퀴즈 만들기"
    }
}

@Composable
private fun QuizGeneratingSparkleOverlay(
    modifier: Modifier = Modifier,
) {
    val hasSparkleAsset = rememberAssetAvailable(QUIZ_GENERATING_SPARKLE_ASSET)

    if (!hasSparkleAsset) {
        QuizGeneratingSparkleFallback(
            modifier = modifier,
        )
        return
    }

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(QUIZ_GENERATING_SPARKLE_ASSET),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        ignoreSystemAnimatorScale = true,
    )

    val loadedComposition = composition
    if (loadedComposition != null) {
        LottieAnimation(
            composition = loadedComposition,
            progress = { progress },
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        QuizGeneratingSparkleFallback(
            modifier = modifier,
        )
    }
}

@Composable
private fun rememberAssetAvailable(assetPath: String): Boolean {
    val context = LocalContext.current
    return remember(context, assetPath) {
        runCatching {
            context.assets.open(assetPath).use { }
        }.isSuccess
    }
}

@Composable
private fun QuizGeneratingSparkleFallback(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "quiz_generating_sparkle")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1_200,
                easing = LinearEasing,
            ),
        ),
        label = "quiz_generating_sparkle_phase",
    )

    Canvas(modifier = modifier) {
        QuizGeneratingSparkleDots.forEach { sparkle ->
            val rawPulse = (phase + sparkle.phaseOffset) % 1f
            val pulse = if (rawPulse < 0.5f) rawPulse * 2f else (1f - rawPulse) * 2f
            drawCircle(
                color = White.copy(alpha = 0.18f + pulse * 0.28f),
                radius = sparkle.radiusDp.dp.toPx() * (0.75f + pulse * 0.3f),
                center = Offset(
                    x = size.width * sparkle.xFraction,
                    y = size.height * sparkle.yFraction,
                ),
            )
        }
    }
}

@Preview(name = "Home — 기본", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    QuiketTheme {
        HomeScreen(
            uiState = HomeState(isLoading = false, showOnboarding = false),
            onBoardingDone = {},
            onQuizCardClick = {},
            onQuizActionClick = {},
            onFabItemClick = {},
        )
    }
}

@Preview(name = "Home — 온보딩 툴팁", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenOnboardingPreview() {
    QuiketTheme {
        HomeScreen(
            uiState = HomeState(isLoading = false, showOnboarding = true),
            onBoardingDone = {},
            onQuizCardClick = {},
            onQuizActionClick = {},
            onFabItemClick = {},
        )
    }
}

@Preview(name = "Home — 퀴즈 생성 중", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenQuizGeneratingPreview() {
    QuiketTheme {
        HomeScreen(
            uiState = HomeState(isLoading = false, showOnboarding = false),
            isQuizGenerating = true,
            onBoardingDone = {},
            onQuizCardClick = {},
            onQuizActionClick = {},
            onFabItemClick = {},
        )
    }
}

@Composable
fun rememberTooltipOffset(
    noteIconOffset: Offset,
    noteIconSize: IntSize,
    boxOffset: Offset = Offset.Zero,
): Pair<Dp, Dp> {
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // positionInRoot()은 화면 y=0 기준, offset(y=)은 Box 내부 기준이므로 Box의 root 위치를 빼줘야 함
    val yDp = with(density) { (noteIconOffset.y + noteIconSize.height - boxOffset.y).toDp() }
    val noteIconCenterDp = with(density) { (noteIconOffset.x + noteIconSize.width / 2f - boxOffset.x).toDp() }
    val endPadding = screenWidth - noteIconCenterDp - 19.dp
    return Pair(yDp, endPadding)
}

private const val QUIZ_GENERATING_SPARKLE_ASSET = "lottie/anim_quiz_generating_sparkle.json"

private data class SparkleDot(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val phaseOffset: Float,
)

private val QuizGeneratingSparkleDots = listOf(
    SparkleDot(xFraction = 0.46f, yFraction = 0.28f, radiusDp = 4.5f, phaseOffset = 0.00f),
    SparkleDot(xFraction = 0.62f, yFraction = 0.32f, radiusDp = 5.0f, phaseOffset = 0.17f),
    SparkleDot(xFraction = 0.82f, yFraction = 0.42f, radiusDp = 4.0f, phaseOffset = 0.34f),
    SparkleDot(xFraction = 0.58f, yFraction = 0.64f, radiusDp = 4.0f, phaseOffset = 0.51f),
    SparkleDot(xFraction = 0.90f, yFraction = 0.72f, radiusDp = 5.0f, phaseOffset = 0.68f),
)
