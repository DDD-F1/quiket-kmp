package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketOutlinedButton
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown100
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.R
import com.f1.quiket.feature.home.presentation.route.createQuizSubjectSamples

@Composable
fun CreateQuizOptionsScreen(
    subject: QuizSubjectUiModel,
    selectedChapterCount: Int,
    selectedPartCount: Int,
    selectedQuizType: QuizTypeOption?,
    selectedChoiceCount: Int?,
    selectedQuestionCountOption: QuizQuestionCountOption?,
    customQuestionCount: Int?,
    selectedDifficulty: QuizDifficultyOption?,
    quizCreationRequested: Boolean,
    customQuestionCountDialogVisible: Boolean,
    customQuestionCountText: String,
    onBackClick: () -> Unit,
    onQuizTypeClick: (QuizTypeOption) -> Unit,
    onChoiceCountClick: (Int) -> Unit,
    onQuestionCountClick: (QuizQuestionCountOption) -> Unit,
    onCustomQuestionCountClick: () -> Unit,
    onCustomQuestionCountTextChange: (String) -> Unit,
    onCustomQuestionCountDismiss: () -> Unit,
    onCustomQuestionCountApply: () -> Unit,
    onDifficultyClick: (QuizDifficultyOption) -> Unit,
    onCreateQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tooltipVisible by rememberSaveable { mutableStateOf(false) }
    val needsChoiceCount = selectedQuizType?.requiresChoiceCount == true
    val hasQuestionCount = selectedQuestionCountOption != null &&
        (selectedQuestionCountOption != QuizQuestionCountOption.Custom || customQuestionCount != null)
    val isCreateEnabled = selectedQuizType != null &&
        (!needsChoiceCount || selectedChoiceCount != null) &&
        hasQuestionCount &&
        selectedDifficulty != null &&
        !quizCreationRequested

    BackHandler(enabled = tooltipVisible && !customQuestionCountDialogVisible) {
        tooltipVisible = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 92.dp),
        ) {
            CreateQuizTopBar(
                title = "퀴즈 생성",
                onBackClick = onBackClick,
            )
            CreateQuizStepHeader(
                currentStep = 3,
                breadcrumbItems = listOf(
                    "선택된 과목",
                    subject.name,
                    "챕터 $selectedChapterCount, 파트 $selectedPartCount",
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                QuizTypeSection(
                    selectedQuizType = selectedQuizType,
                    tooltipVisible = tooltipVisible,
                    onInfoClick = { tooltipVisible = true },
                    onTooltipDismiss = { tooltipVisible = false },
                    onQuizTypeClick = onQuizTypeClick,
                )

                if (needsChoiceCount) {
                    ChoiceCountSection(
                        selectedChoiceCount = selectedChoiceCount,
                        onChoiceCountClick = onChoiceCountClick,
                    )
                }

                QuestionCountSection(
                    selectedQuestionCountOption = selectedQuestionCountOption,
                    customQuestionCount = customQuestionCount,
                    onQuestionCountClick = onQuestionCountClick,
                    onCustomQuestionCountClick = onCustomQuestionCountClick,
                )

                DifficultySection(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultyClick = onDifficultyClick,
                )
            }
        }

        QuiketPrimaryButton(
            text = if (quizCreationRequested) "퀴즈 생성 중" else "퀴즈 만들기",
            enabled = isCreateEnabled,
            onClick = onCreateQuizClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )

        if (customQuestionCountDialogVisible) {
            CustomQuestionCountDialog(
                value = customQuestionCountText,
                onValueChange = onCustomQuestionCountTextChange,
                onDismiss = onCustomQuestionCountDismiss,
                onApply = onCustomQuestionCountApply,
            )
        }
    }
}

@Composable
private fun QuizTypeSection(
    selectedQuizType: QuizTypeOption?,
    tooltipVisible: Boolean,
    onInfoClick: () -> Unit,
    onTooltipDismiss: () -> Unit,
    onQuizTypeClick: (QuizTypeOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CreateQuizSectionTitle(text = "퀴즈 유형") {
                QuizTypeInfoButton(onClick = onInfoClick)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuizTypeCard(
                        quizType = QuizTypeOption.MultipleChoice,
                        selected = selectedQuizType == QuizTypeOption.MultipleChoice,
                        onClick = { onQuizTypeClick(QuizTypeOption.MultipleChoice) },
                        modifier = Modifier.weight(1f),
                    )
                    QuizTypeCard(
                        quizType = QuizTypeOption.Ox,
                        selected = selectedQuizType == QuizTypeOption.Ox,
                        onClick = { onQuizTypeClick(QuizTypeOption.Ox) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuizTypeCard(
                        quizType = QuizTypeOption.Flashcard,
                        selected = selectedQuizType == QuizTypeOption.Flashcard,
                        onClick = { onQuizTypeClick(QuizTypeOption.Flashcard) },
                        modifier = Modifier.weight(1f),
                    )
                    QuizTypeCard(
                        quizType = QuizTypeOption.ShortAnswer,
                        selected = selectedQuizType == QuizTypeOption.ShortAnswer,
                        onClick = { onQuizTypeClick(QuizTypeOption.ShortAnswer) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (tooltipVisible) {
            QuizTypeTooltip(
                onDismiss = onTooltipDismiss,
                modifier = Modifier
                    .offset(x = 61.dp, y = 33.dp)
                    .zIndex(1f),
            )
        }
    }
}

@Composable
private fun QuizTypeInfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Gray400)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = "퀴즈 유형 설명 보기"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            color = White,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun QuizTypeTooltip(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tooltipColor = Black.copy(alpha = 0.9f)

    Column(
        modifier = modifier.width(255.dp),
    ) {
        Canvas(
            modifier = Modifier
                .padding(start = 15.dp)
                .size(width = 14.dp, height = 8.dp),
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = tooltipColor)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(tooltipColor)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "퀴즈 유형",
                    color = White,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "•  객관식: 보기 중 정답 선택\n" +
                        "•  O/X 퀴즈: 맞으면 O, 틀리면 X\n" +
                        "•  플래시카드: 빠르게 넘기며 암기\n" +
                        "•  쪽지시험: 단답형, 빈칸 등 답을 직접 입력",
                    color = Gray200,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = onDismiss,
                    )
                    .semantics {
                        contentDescription = "퀴즈 유형 설명 닫기"
                    },
                contentAlignment = Alignment.Center,
            ) {
                CloseIcon(
                    color = Gray200,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun QuizTypeCard(
    quizType: QuizTypeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .height(100.dp)
            .clip(shape)
            .background(if (selected) Brown50 else Gray50)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Brown950,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) Brown100 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            QuizTypeIcon(
                quizType = quizType,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = quizType.title,
            color = Gray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizTypeIcon(
    quizType: QuizTypeOption,
    modifier: Modifier = Modifier,
) {
    when (quizType) {
        QuizTypeOption.MultipleChoice -> Image(
            painter = painterResource(R.drawable.ic_quiz_multiple),
            contentDescription = null,
            modifier = modifier,
        )

        QuizTypeOption.Ox -> Image(
            painter = painterResource(R.drawable.ic_quiz_ox),
            contentDescription = null,
            modifier = modifier,
        )

        QuizTypeOption.Flashcard -> Image(
            painter = painterResource(R.drawable.ic_quiz_flashcard),
            contentDescription = null,
            modifier = modifier,
        )

        QuizTypeOption.ShortAnswer -> Image(
            painter = painterResource(R.drawable.ic_quiz_short),
            contentDescription = null,
            modifier = modifier,
        )
    }
}

@Composable
private fun ChoiceCountSection(
    selectedChoiceCount: Int?,
    onChoiceCountClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionSection(
        title = "보기수",
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(4, 5).forEach { count ->
                CreateQuizSelectButton(
                    text = "${count}지선다",
                    selected = selectedChoiceCount == count,
                    onClick = { onChoiceCountClick(count) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuestionCountSection(
    selectedQuestionCountOption: QuizQuestionCountOption?,
    customQuestionCount: Int?,
    onQuestionCountClick: (QuizQuestionCountOption) -> Unit,
    onCustomQuestionCountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionSection(
        title = "문제수",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuizQuestionCountOption.presets.take(2).forEach { option ->
                    CreateQuizSelectButton(
                        text = option.title,
                        selected = selectedQuestionCountOption == option,
                        onClick = { onQuestionCountClick(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isCustomSelected = selectedQuestionCountOption == QuizQuestionCountOption.Custom
                CreateQuizSelectButton(
                    text = QuizQuestionCountOption.Twenty.title,
                    selected = selectedQuestionCountOption == QuizQuestionCountOption.Twenty,
                    onClick = { onQuestionCountClick(QuizQuestionCountOption.Twenty) },
                    modifier = Modifier.weight(1f),
                )
                CreateQuizSelectButton(
                    text = if (isCustomSelected && customQuestionCount != null) {
                        customQuestionCount.toString()
                    } else {
                        QuizQuestionCountOption.Custom.title
                    },
                    selected = isCustomSelected,
                    onClick = onCustomQuestionCountClick,
                    modifier = Modifier.weight(1f),
                    trailingContent = if (isCustomSelected && customQuestionCount != null) {
                        {
                            EditIcon(
                                color = Gray500,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun DifficultySection(
    selectedDifficulty: QuizDifficultyOption?,
    onDifficultyClick: (QuizDifficultyOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionSection(
        title = "난이도",
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuizDifficultyOption.entries.forEach { difficulty ->
                CreateQuizSelectButton(
                    text = difficulty.title,
                    selected = selectedDifficulty == difficulty,
                    onClick = { onDifficultyClick(difficulty) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OptionSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CreateQuizSectionTitle(text = title)
        content()
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
                .background(Black.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-97).dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "문제수 직접 입력",
                        color = Gray950,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    BaseTextField(
                        value = value,
                        onValueChange = onValueChange,
                        hint = "내용을 입력해주세요",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuiketOutlinedButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = "적용",
                        enabled = value.toIntOrNull()?.let { count -> count > 0 } == true,
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.25f),
            end = Offset(size.width * 0.75f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.75f, size.height * 0.25f),
            end = Offset(size.width * 0.25f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun EditIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.18f, size.height * 0.28f),
            size = Size(size.width * 0.46f, size.height * 0.54f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.56f, size.height * 0.58f),
            end = Offset(size.width * 0.83f, size.height * 0.31f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.74f, size.height * 0.22f),
            end = Offset(size.width * 0.88f, size.height * 0.36f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateQuizOptionsScreenEmptyPreview() {
    val subject = createQuizSubjectSamples()[0]

    QuiketTheme {
        CreateQuizOptionsScreen(
            subject = subject,
            selectedChapterCount = 2,
            selectedPartCount = 6,
            selectedQuizType = null,
            selectedChoiceCount = null,
            selectedQuestionCountOption = null,
            customQuestionCount = null,
            selectedDifficulty = null,
            quizCreationRequested = false,
            customQuestionCountDialogVisible = false,
            customQuestionCountText = "",
            onBackClick = {},
            onQuizTypeClick = {},
            onChoiceCountClick = {},
            onQuestionCountClick = {},
            onCustomQuestionCountClick = {},
            onCustomQuestionCountTextChange = {},
            onCustomQuestionCountDismiss = {},
            onCustomQuestionCountApply = {},
            onDifficultyClick = {},
            onCreateQuizClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 854)
@Composable
private fun CreateQuizOptionsScreenSelectedPreview() {
    val subject = createQuizSubjectSamples()[0]

    QuiketTheme {
        CreateQuizOptionsScreen(
            subject = subject,
            selectedChapterCount = 2,
            selectedPartCount = 6,
            selectedQuizType = QuizTypeOption.MultipleChoice,
            selectedChoiceCount = 4,
            selectedQuestionCountOption = QuizQuestionCountOption.Five,
            customQuestionCount = null,
            selectedDifficulty = QuizDifficultyOption.Normal,
            quizCreationRequested = false,
            customQuestionCountDialogVisible = false,
            customQuestionCountText = "",
            onBackClick = {},
            onQuizTypeClick = {},
            onChoiceCountClick = {},
            onQuestionCountClick = {},
            onCustomQuestionCountClick = {},
            onCustomQuestionCountTextChange = {},
            onCustomQuestionCountDismiss = {},
            onCustomQuestionCountApply = {},
            onDifficultyClick = {},
            onCreateQuizClick = {},
        )
    }
}
