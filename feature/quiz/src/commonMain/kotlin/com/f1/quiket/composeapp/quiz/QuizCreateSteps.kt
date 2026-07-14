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
import com.f1.quiket.feature.quiz.resources.Res
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_detail_edit
import com.f1.quiket.core.designsystem.resources.ic_qring_profile


@Composable
internal fun QuizCreateScreen(
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
internal fun QuizSubjectStep(
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
internal fun QuizScopeStep(
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
internal fun QuizOptionsStep(
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
                                painter = painterResource(DesignSystemRes.drawable.ic_detail_edit),
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
internal fun CustomQuestionCountDialog(
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
internal fun QuizDialogOutlinedButton(
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
internal fun QuizLoadingStep(
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
internal fun QuizLoadingAnimation(
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
internal fun QuizLoadingFallback(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(236.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(DesignSystemRes.drawable.ic_qring_profile),
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
                painter = painterResource(DesignSystemRes.drawable.ic_acorn),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}
