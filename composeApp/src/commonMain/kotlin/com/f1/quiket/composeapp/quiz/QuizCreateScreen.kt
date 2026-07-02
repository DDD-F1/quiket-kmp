package com.f1.quiket.composeapp.quiz

import org.koin.compose.koinInject
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.designsystem.QuiketBrown100
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizDifficulty
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStep
import com.f1.quiket.composeapp.quiz.presentation.QuizCreateStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizTypeOption
import com.f1.quiket.composeapp.quiz.presentation.icon
import com.f1.quiket.composeapp.subject.domain.model.PartSummary
import com.f1.quiket.composeapp.subject.domain.model.SubjectListItem
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_acorn
import quiket.composeapp.generated.resources.ic_qring_profile
import quiket.composeapp.generated.resources.ic_detail_edit

private const val QuizLoadingFullLottieResource = "files/quiz_loading_full.json"
private val QuizBrown700 = Color(0xFF684C40)
private val QuizGreen100 = Color(0xFFEFF4D3)
private val QuizGreen300 = Color(0xFFC8DA7C)
private val QuizGreen800 = Color(0xFF465420)
private val QuizBlue100 = Color(0xFFE2F2FC)
private val QuizBlue300 = Color(0xFF84CFF5)
private val QuizBlue800 = Color(0xFF0E567E)

@Composable
internal fun QuizCreateRoute(
    onBackClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onQuizGenerationStarted: () -> Unit = {},
    onQuizGenerationFinished: () -> Unit = {},
    onQuizReady: (QuizPlayLaunchConfig) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<QuizCreateStateHolder>()
    val selectedSubjectId = stateHolder.selectedSubjectId

    fun loadSubjects() {
        coroutineScope.launch {
            stateHolder.loadSubjects(onSessionExpired = onSessionExpired)
        }
    }

    fun createQuiz() {
        coroutineScope.launch {
            stateHolder.createQuiz(
                onSessionExpired = onSessionExpired,
                onQuizGenerationStarted = onQuizGenerationStarted,
                onQuizGenerationFinished = onQuizGenerationFinished,
            )?.let(onQuizReady)
        }
    }

    fun moveBack() {
        if (stateHolder.moveBack()) {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        loadSubjects()
    }

    LaunchedEffect(selectedSubjectId) {
        if (selectedSubjectId != null) {
            stateHolder.loadSelectedSubjectDetail(onSessionExpired = onSessionExpired)
        }
    }

    QuizCreateScreen(
        currentStep = stateHolder.currentStep,
        subjects = stateHolder.subjects,
        subjectDetail = stateHolder.selectedSubjectDetail,
        selectedSubjectId = selectedSubjectId,
        expandedChapterId = stateHolder.expandedChapterId,
        selectedPartIds = stateHolder.selectedPartIds,
        quizType = stateHolder.quizType,
        choiceCount = stateHolder.choiceCount,
        questionCountPreset = stateHolder.questionCountPreset,
        questionCountText = stateHolder.questionCountText,
        difficulty = stateHolder.difficulty,
        isLoadingSubjects = stateHolder.isLoadingSubjects,
        isLoadingDetail = stateHolder.isLoadingDetail,
        isGenerating = stateHolder.isGenerating,
        canBrowseDuringGeneration = stateHolder.canBrowseDuringGeneration,
        generationProgress = stateHolder.generationProgress,
        message = stateHolder.message,
        onBackClick = ::moveBack,
        onRetryClick = ::loadSubjects,
        onAddSubjectClick = onAddSubjectClick,
        onSubjectNextClick = stateHolder::moveToScope,
        onScopeNextClick = stateHolder::moveToOptions,
        onSubjectClick = stateHolder::selectSubject,
        onChapterExpandClick = stateHolder::toggleChapterExpanded,
        onChapterClick = stateHolder::toggleChapterParts,
        onPartClick = stateHolder::togglePart,
        onClearPartsClick = stateHolder::clearParts,
        onQuizTypeClick = stateHolder::selectQuizType,
        onChoiceCountClick = stateHolder::selectChoiceCount,
        onQuestionCountClick = stateHolder::selectQuestionCount,
        onQuestionCountTextChange = stateHolder::updateQuestionCountText,
        onDifficultyClick = stateHolder::selectDifficulty,
        onCreateClick = ::createQuiz,
        modifier = modifier,
    )
}

@Composable
private fun QuizCreateScreen(
    currentStep: QuizCreateStep,
    subjects: List<SubjectListItem>,
    subjectDetail: QuizScope?,
    selectedSubjectId: String?,
    expandedChapterId: String?,
    selectedPartIds: Set<String>,
    quizType: QuizTypeOption?,
    choiceCount: Int?,
    questionCountPreset: Int?,
    questionCountText: String,
    difficulty: QuizDifficulty?,
    isLoadingSubjects: Boolean,
    isLoadingDetail: Boolean,
    isGenerating: Boolean,
    canBrowseDuringGeneration: Boolean,
    generationProgress: Float,
    message: String?,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectNextClick: () -> Unit,
    onScopeNextClick: () -> Unit,
    onSubjectClick: (SubjectListItem) -> Unit,
    onChapterExpandClick: (String) -> Unit,
    onChapterClick: (String) -> Unit,
    onPartClick: (PartSummary) -> Unit,
    onClearPartsClick: () -> Unit,
    onQuizTypeClick: (QuizTypeOption) -> Unit,
    onChoiceCountClick: (Int) -> Unit,
    onQuestionCountClick: (Int) -> Unit,
    onQuestionCountTextChange: (String) -> Unit,
    onDifficultyClick: (QuizDifficulty) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        when (currentStep) {
            QuizCreateStep.Subject -> QuizSubjectStep(
                subjects = subjects,
                selectedSubjectId = selectedSubjectId,
                isLoading = isLoadingSubjects,
                message = message,
                onBackClick = onBackClick,
                onRetryClick = onRetryClick,
                onAddSubjectClick = onAddSubjectClick,
                onSubjectClick = onSubjectClick,
                onNextClick = onSubjectNextClick,
            )

            QuizCreateStep.Scope -> QuizScopeStep(
                subjectDetail = subjectDetail,
                selectedPartIds = selectedPartIds,
                expandedChapterId = expandedChapterId,
                isLoading = isLoadingDetail,
                message = message,
                onBackClick = onBackClick,
                onChapterExpandClick = onChapterExpandClick,
                onChapterClick = onChapterClick,
                onPartClick = onPartClick,
                onClearPartsClick = onClearPartsClick,
                onNextClick = onScopeNextClick,
            )

            QuizCreateStep.Options -> QuizOptionsStep(
                subjectDetail = subjectDetail,
                selectedPartIds = selectedPartIds,
                quizType = quizType,
                choiceCount = choiceCount,
                questionCountPreset = questionCountPreset,
                questionCountText = questionCountText,
                difficulty = difficulty,
                isGenerating = isGenerating,
                message = message,
                onBackClick = onBackClick,
                onQuizTypeClick = onQuizTypeClick,
                onChoiceCountClick = onChoiceCountClick,
                onQuestionCountClick = onQuestionCountClick,
                onQuestionCountTextChange = onQuestionCountTextChange,
                onDifficultyClick = onDifficultyClick,
                onCreateClick = onCreateClick,
            )

            QuizCreateStep.Loading -> QuizLoadingStep(
                progress = generationProgress,
                rewardCount = 10,
                browseEnabled = canBrowseDuringGeneration,
                onBrowseClick = onBackClick,
            )
        }
    }
}

@Composable
private fun QuizSubjectStep(
    subjects: List<SubjectListItem>,
    selectedSubjectId: String?,
    isLoading: Boolean,
    message: String?,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (SubjectListItem) -> Unit,
    onNextClick: () -> Unit,
) {
    QuizStepScaffold(
        title = "과목 선택",
        onBackClick = onBackClick,
        bottomButtonText = "다음",
        bottomButtonEnabled = selectedSubjectId != null && !isLoading,
        onBottomButtonClick = onNextClick,
    ) {
        QuizStepHeader(currentStep = 1)
        QuizCreateTitle(
            title = "어떤 과목의 퀴즈를 만들까요?",
            description = "퀴즈를 만들 과목을 하나 선택해 주세요",
        )
        if (isLoading) {
            QuizCreateInfoCard(
                title = "퀴즈 재료를 불러오는 중",
                description = "과목과 파트를 확인하고 있어요.",
            )
        }
        if (!message.isNullOrBlank()) {
            QuizCreateInfoCard(
                title = "알림",
                description = message,
            )
        }
        if (!isLoading && subjects.isEmpty()) {
            QuizCreateInfoCard(
                title = "과목이 없어요",
                description = "먼저 노트 업로드 또는 과목 추가를 진행해주세요.",
                actionText = "다시 시도",
                onActionClick = onRetryClick,
            )
        }
        subjects.forEach { subject ->
            QuizSubjectCard(
                subject = subject,
                selected = subject.id == selectedSubjectId,
                enabled = !isLoading,
                onClick = { onSubjectClick(subject) },
            )
        }
        QuizAddSubjectCard(onClick = onAddSubjectClick)
    }
}

@Composable
private fun QuizScopeStep(
    subjectDetail: QuizScope?,
    selectedPartIds: Set<String>,
    expandedChapterId: String?,
    isLoading: Boolean,
    message: String?,
    onBackClick: () -> Unit,
    onChapterExpandClick: (String) -> Unit,
    onChapterClick: (String) -> Unit,
    onPartClick: (PartSummary) -> Unit,
    onClearPartsClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val detail = subjectDetail
    QuizStepScaffold(
        title = "범위 선택",
        onBackClick = onBackClick,
        bottomButtonText = "다음",
        bottomButtonEnabled = selectedPartIds.isNotEmpty() && !isLoading,
        onBottomButtonClick = onNextClick,
    ) {
        QuizStepHeader(
            currentStep = 2,
            breadcrumbItems = listOfNotNull("선택된 과목", detail?.name),
        )
        if (isLoading || detail == null) {
            QuizCreateInfoCard(
                title = "출제 범위를 불러오는 중",
                description = "챕터와 파트 정보를 확인하고 있어요.",
            )
        }
        if (!message.isNullOrBlank()) {
            QuizCreateInfoCard(
                title = "알림",
                description = message,
            )
        }
        if (detail != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuizCreateSectionTitle(
                    text = "출제 범위",
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "전체 해제",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = onClearPartsClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            detail.chapters.sortedBy { chapter -> chapter.displayOrder }.forEach { chapter ->
                QuizScopeChapterCard(
                    chapterName = chapter.name,
                    chapterNumber = chapter.displayOrder,
                    parts = chapter.parts.sortedBy { part -> part.partNumber },
                    selectedPartIds = selectedPartIds,
                    expanded = expandedChapterId == chapter.id,
                    onExpandClick = { onChapterExpandClick(chapter.id) },
                    onChapterClick = { onChapterClick(chapter.id) },
                    onPartClick = onPartClick,
                )
            }
            val selectedChapterCount = detail.chapters.count { chapter ->
                chapter.parts.any { part -> part.id in selectedPartIds }
            }
            val selectedPartCount = selectedPartIds.size
            QuizScopeSummary(
                allSelected = selectedChapterCount == detail.chapters.size &&
                    selectedPartCount == detail.allParts().size,
                selectedChapterCount = selectedChapterCount,
                selectedPartCount = selectedPartCount,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun QuizOptionsStep(
    subjectDetail: QuizScope?,
    selectedPartIds: Set<String>,
    quizType: QuizTypeOption?,
    choiceCount: Int?,
    questionCountPreset: Int?,
    questionCountText: String,
    difficulty: QuizDifficulty?,
    isGenerating: Boolean,
    message: String?,
    onBackClick: () -> Unit,
    onQuizTypeClick: (QuizTypeOption) -> Unit,
    onChoiceCountClick: (Int) -> Unit,
    onQuestionCountClick: (Int) -> Unit,
    onQuestionCountTextChange: (String) -> Unit,
    onDifficultyClick: (QuizDifficulty) -> Unit,
    onCreateClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val needsChoiceCount = quizType?.requiresChoiceCount == true
    val questionCount = questionCountPreset ?: questionCountText.toIntOrNull()
    val customQuestionCount = questionCountText.toIntOrNull()
    var quizTypeTooltipVisible by remember { mutableStateOf(false) }
    var customQuestionCountDialogVisible by remember { mutableStateOf(false) }
    var customQuestionCountDraft by remember { mutableStateOf("") }
    val canCreate = quizType != null &&
        (!needsChoiceCount || choiceCount != null) &&
        questionCount != null &&
        difficulty != null &&
        !isGenerating
    fun hideKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        hidePlatformKeyboard()
    }
    fun openCustomQuestionCountDialog() {
        hideKeyboard()
        customQuestionCountDraft = customQuestionCount?.toString().orEmpty()
        customQuestionCountDialogVisible = true
    }

    QuizStepScaffold(
        title = "퀴즈 생성",
        onBackClick = onBackClick,
        bottomButtonText = if (isGenerating) "퀴즈 생성 중" else "퀴즈 만들기",
        bottomButtonEnabled = canCreate,
        onBottomButtonClick = {
            hideKeyboard()
            onCreateClick()
        },
    ) {
        val selectedChapterCount = subjectDetail?.chapters?.count { chapter ->
            chapter.parts.any { part -> part.id in selectedPartIds }
        } ?: 0
        QuizStepHeader(
            currentStep = 3,
            breadcrumbItems = listOfNotNull(
                "선택된 과목",
                subjectDetail?.name,
                "챕터 $selectedChapterCount, 파트 ${selectedPartIds.size}",
            ),
        )
        if (!message.isNullOrBlank()) {
            QuizCreateInfoCard(
                title = "알림",
                description = message,
            )
        }

        QuizCreateSectionTitle("퀴즈 유형") {
            QuizTypeInfoButton(
                onClick = { quizTypeTooltipVisible = !quizTypeTooltipVisible },
                enabled = !isGenerating,
            )
        }
        if (quizTypeTooltipVisible) {
            QuizTypeTooltip(
                onDismiss = { quizTypeTooltipVisible = false },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuizTypeOption.entries.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { option ->
                        QuizTypeCard(
                            option = option,
                            selected = quizType == option,
                            enabled = !isGenerating,
                            onClick = {
                                hideKeyboard()
                                onQuizTypeClick(option)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (needsChoiceCount) {
            QuizCreateSectionTitle("보기수")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(4, 5).forEach { count ->
                    QuizChoiceChip(
                        text = "${count}지선다",
                        selected = choiceCount == count,
                        enabled = !isGenerating,
                        onClick = {
                            hideKeyboard()
                            onChoiceCountClick(count)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        QuizCreateSectionTitle("문제수")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(5, 10).forEach { count ->
                    QuizChoiceChip(
                        text = count.toString(),
                        selected = questionCountPreset == count,
                        enabled = !isGenerating,
                        onClick = {
                            hideKeyboard()
                            onQuestionCountClick(count)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val isCustomSelected = questionCountPreset == null && customQuestionCount != null
                QuizChoiceChip(
                    text = "20",
                    selected = questionCountPreset == 20,
                    enabled = !isGenerating,
                    onClick = {
                        hideKeyboard()
                        onQuestionCountClick(20)
                    },
                    modifier = Modifier.weight(1f),
                )
                QuizChoiceChip(
                    text = if (isCustomSelected) {
                        customQuestionCount.toString()
                    } else {
                        "직접 입력"
                    },
                    selected = isCustomSelected,
                    enabled = !isGenerating,
                    onClick = ::openCustomQuestionCountDialog,
                    modifier = Modifier.weight(1f),
                    trailingContent = if (isCustomSelected) {
                        {
                            Image(
                                painter = painterResource(Res.drawable.ic_detail_edit),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }

        QuizCreateSectionTitle("난이도")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizDifficulty.entries.forEach { item ->
                QuizChoiceChip(
                    text = item.displayLabel(),
                    selected = difficulty == item,
                    enabled = !isGenerating,
                    onClick = {
                        hideKeyboard()
                        onDifficultyClick(item)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    if (customQuestionCountDialogVisible) {
        CustomQuestionCountDialog(
            value = customQuestionCountDraft,
            onValueChange = { input ->
                customQuestionCountDraft = input.filter(Char::isDigit).take(3)
            },
            onDismiss = {
                hideKeyboard()
                customQuestionCountDialogVisible = false
            },
            onApply = {
                hideKeyboard()
                onQuestionCountTextChange(customQuestionCountDraft)
                customQuestionCountDialogVisible = false
            },
        )
    }
}

@Composable
private fun CustomQuestionCountDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-97).dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(QuiketWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "문제수 직접 입력",
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    QuiketTextField(
                        value = value,
                        onValueChange = onValueChange,
                        hint = "내용을 입력해주세요",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (value.toIntOrNull()?.let { count -> count > 0 } == true) {
                                onApply()
                            }
                        }),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuizDialogOutlinedButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = "적용",
                        enabled = value.toIntOrNull()?.let { count -> count > 0 } == true,
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizDialogOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .border(2.dp, QuiketBrown950, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketBrown950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun QuizLoadingStep(
    progress: Float,
    rewardCount: Int,
    browseEnabled: Boolean,
    onBrowseClick: () -> Unit,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val progressPercent = (clampedProgress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 82.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center,
            ) {
                QuizLoadingAnimation()
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "큐링이가 열심히\n퀴즈랑 도토리 배달을 준비하고 있어요!",
                color = QuiketGray950,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "잠시 쉬다 오셔도 돼요!\n배달이 완료되면 불러드릴게요",
                color = QuiketGray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { clampedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = QuiketBrown950,
                    trackColor = QuiketGray100,
                )
                Text(
                    text = "$progressPercent%",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "완료 후 도토리 $rewardCount 획득 예정이에요!",
                color = QuiketGray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    enabled = browseEnabled,
                    role = Role.Button,
                    onClick = onBrowseClick,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (browseEnabled) "잠시 딴짓하러 가기" else "퀴즈 생성 요청 중이에요",
                color = if (browseEnabled) QuiketBrown950 else QuiketGray700,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
            Text(
                text = "›",
                color = if (browseEnabled) QuiketBrown950 else QuiketGray700,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun QuizLoadingAnimation(
    modifier: Modifier = Modifier,
) {
    var lottieJson by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        lottieJson = Res.readBytes(QuizLoadingFullLottieResource).decodeToString()
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(lottieJson.orEmpty()),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
    )

    if (composition != null && lottieJson != null) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = progress,
            ),
            contentDescription = null,
            modifier = modifier.size(width = 236.dp, height = 252.dp),
        )
    } else {
        QuizLoadingFallback(modifier = modifier)
    }
}

@Composable
private fun QuizLoadingFallback(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(236.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_qring_profile),
            contentDescription = null,
            modifier = Modifier.size(210.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(64.dp)
                .clip(CircleShape)
                .background(QuiketBrown50),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_acorn),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun QuizStepScaffold(
    title: String,
    onBackClick: () -> Unit,
    bottomButtonText: String,
    bottomButtonEnabled: Boolean,
    onBottomButtonClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
        ) {
            QuizCreateTopBar(
                title = title,
                onBackClick = onBackClick,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .background(QuiketWhite)
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 28.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            QuiketPrimaryButton(
                text = bottomButtonText,
                enabled = bottomButtonEnabled,
                onClick = onBottomButtonClick,
            )
        }
    }
}

@Composable
private fun QuizStepHeader(
    currentStep: Int,
    breadcrumbItems: List<String> = emptyList(),
) {
    val totalSteps = 3
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(21.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (index < currentStep) QuiketBrown950 else QuiketGray100),
                )
            }
            Text(
                text = "$currentStep/$totalSteps",
                color = QuiketGray900,
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
        if (breadcrumbItems.isNotEmpty()) {
            QuizBreadcrumb(items = breadcrumbItems)
        }
    }
}

@Composable
private fun QuizBreadcrumb(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .quizHorizontalFullBleed()
            .fillMaxWidth()
            .height(37.dp)
            .background(QuiketGray50)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            Text(
                text = item,
                color = if (index == 0) QuiketGray500 else QuiketGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = if (index == 0) FontWeight.Medium else FontWeight.Normal,
                ),
                modifier = if (isLast) Modifier.weight(1f) else Modifier,
            )
            if (!isLast) {
                QuizChevronRight(
                    color = QuiketGray400,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun Modifier.quizHorizontalFullBleed(): Modifier = layout { measurable, constraints ->
    val insetPx = 16.dp.roundToPx()
    val extraWidth = insetPx * 2
    val expandedMaxWidth = if (constraints.hasBoundedWidth) {
        constraints.maxWidth + extraWidth
    } else {
        constraints.maxWidth
    }
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = (constraints.minWidth + extraWidth).coerceAtMost(expandedMaxWidth),
            maxWidth = expandedMaxWidth,
        ),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.placeRelative(-insetPx, 0)
    }
}

@Composable
private fun QuizChevronRight(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.2f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun QuizCreateTitle(
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun QuizAddSubjectCard(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(QuiketGray50)
            .border(1.dp, QuiketGray300, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+ 과목 추가",
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun QuizScopeChapterCard(
    chapterName: String,
    chapterNumber: Int,
    parts: List<PartSummary>,
    selectedPartIds: Set<String>,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onChapterClick: () -> Unit,
    onPartClick: (PartSummary) -> Unit,
) {
    val allSelected = parts.isNotEmpty() && parts.all { part -> part.id in selectedPartIds }
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (expanded) 185.dp else 70.dp)
            .clip(shape)
            .background(if (expanded) QuiketWhite else QuiketGray50)
            .border(if (expanded) 2.dp else 0.dp, if (expanded) QuiketBrown950 else Color.Transparent, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuizScopeExpandIcon(
                expanded = expanded,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = if (expanded) {
                            "$chapterName 접기"
                        } else {
                            "$chapterName 펼치기"
                        }
                    }
                    .clickable(role = Role.Button, onClick = onExpandClick),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = chapterName,
                    color = QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuizScopeChapterChip(
                        count = chapterNumber,
                        selected = expanded,
                    )
                    Text(
                        text = "파트 ${parts.size}개 포함",
                        color = QuiketGray600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }
            }
            QuizCheckbox(
                checked = allSelected,
                onClick = onChapterClick,
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            parts.forEach { part ->
                QuizPartRow(
                    part = part,
                    selected = part.id in selectedPartIds,
                    onClick = { onPartClick(part) },
                )
            }
        }
    }
}

@Composable
private fun QuizScopeChapterChip(
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(1000.dp))
            .background(if (selected) QuiketBrown100 else QuiketGray100)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "챕터 $count",
            color = if (selected) QuizBrown700 else QuiketGray800,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun QuizPartRow(
    part: PartSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(37.dp)
            .background(QuiketBrown100)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = part.name,
            color = QuiketGray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.weight(1f),
        )
        QuizCheckMark(
            color = if (selected) QuiketBrown950 else QuiketGray400,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun QuizCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(role = Role.Checkbox, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(QuiketBrown950),
                contentAlignment = Alignment.Center,
            ) {
                QuizCheckMark(
                    color = QuiketWhite,
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, QuiketGray500, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
private fun QuizCheckMark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.53f),
            end = Offset(size.width * 0.42f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.75f),
            end = Offset(size.width * 0.82f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun QuizScopeSummary(
    allSelected: Boolean,
    selectedChapterCount: Int,
    selectedPartCount: Int,
) {
    val backgroundColor = if (allSelected) QuizGreen100 else QuizBlue100
    val borderColor = if (allSelected) QuizGreen300 else QuizBlue300
    val accentColor = if (allSelected) QuizGreen800 else QuizBlue800

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .quizDashedBorder(
                color = borderColor,
                cornerRadius = 12.dp,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (allSelected) "전체 범위 선택" else "부분 범위 선택",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "챕터 ${selectedChapterCount}개",
                color = accentColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(1000.dp))
                    .background(accentColor),
            )
            Text(
                text = "파트 ${selectedPartCount}개를 선택했어요",
                color = accentColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

private fun Modifier.quizDashedBorder(
    color: Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
            ),
        ),
    )
}

@Composable
private fun QuizScopeExpandIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        if (expanded) {
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.2f, size.height * 0.38f),
                end = Offset(size.width * 0.5f, size.height * 0.66f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.5f, size.height * 0.66f),
                end = Offset(size.width * 0.8f, size.height * 0.38f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        } else {
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.36f, size.height * 0.18f),
                end = Offset(size.width * 0.66f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = QuiketGray600,
                start = Offset(size.width * 0.66f, size.height * 0.5f),
                end = Offset(size.width * 0.36f, size.height * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun QuizCreateTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        Text(
            text = "‹",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 50.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onBackClick)
                .semantics { contentDescription = "뒤로" },
        )
        Text(
            text = title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp),
        )
    }
}

@Composable
private fun QuizCreateSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(27.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        trailingContent?.invoke()
    }
}

@Composable
private fun QuizTypeCard(
    option: QuizTypeOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .height(100.dp)
            .clip(shape)
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = shape,
            )
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) QuiketBrown100 else QuiketGray100),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(option.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = option.title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizTypeInfoButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(QuiketGray400)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "퀴즈 유형 설명 보기" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            color = QuiketWhite,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun QuizTypeTooltip(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray950,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "퀴즈 유형",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "x",
                    color = QuiketGray300,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .semantics { contentDescription = "퀴즈 유형 설명 닫기" },
                )
            }
            QuizTypeTooltipRow(
                title = "객관식 설명",
                description = "보기 중 정답을 선택해요.",
            )
            QuizTypeTooltipRow(
                title = "O/X 퀴즈 설명",
                description = "참/거짓 중 정답을 선택해요.",
            )
            QuizTypeTooltipRow(
                title = "플래시카드 설명",
                description = "빠르게 넘기며 암기해요.",
            )
            QuizTypeTooltipRow(
                title = "쪽지시험 설명",
                description = "단답형, 빈칸 등 답을 직접 입력해요.",
            )
        }
    }
}

@Composable
private fun QuizTypeTooltipRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            color = QuiketWhite,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketWhite,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun QuizSubjectCard(
    subject: SubjectListItem,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(shape)
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = QuiketBrown950,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = subject.name,
                color = QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuizSubjectChapterChip(
                    count = subject.chapterCount,
                    selected = selected,
                )
                Text(
                    text = "파트 ${subject.partCount}",
                    color = QuiketGray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

@Composable
private fun QuizSubjectChapterChip(
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "챕터 $count",
        color = QuiketGray600,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) QuiketWhite else QuiketGray100)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun QuizSelectableCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) QuiketBrown950 else Color.Transparent
    val backgroundColor = if (selected) QuiketBrown50 else QuiketGray50
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) QuiketBrown950 else QuiketWhite)
                .border(1.dp, if (selected) QuiketBrown950 else QuiketGray300, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = description,
                color = QuiketGray600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun QuizChoiceChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) QuiketBrown950 else QuiketGray50)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = if (selected) QuiketWhite else QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(4.dp))
                trailingContent()
            }
        }
    }
}

@Composable
private fun QuizCreateInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = description,
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall,
            )
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = QuiketBrown950,
                    trackColor = QuiketGray100,
                )
            }
            if (actionText != null && onActionClick != null) {
                QuiketPrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                )
            }
        }
    }
}

private fun QuizScope.allParts(): List<PartSummary> =
    chapters
        .sortedBy { chapter -> chapter.displayOrder }
        .flatMap { chapter -> chapter.parts.sortedBy { part -> part.partNumber } }

private fun QuizDifficulty.displayLabel(): String =
    when (this) {
        QuizDifficulty.Easy -> "쉬움"
        QuizDifficulty.Medium -> "보통"
        QuizDifficulty.Hard -> "어려움"
    }
