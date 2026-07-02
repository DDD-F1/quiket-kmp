package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Dimmed
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.NegativeBg
import com.f1.quiket.core.designsystem.theme.Positive
import com.f1.quiket.core.designsystem.theme.PositiveBg
import com.f1.quiket.core.designsystem.theme.Tutorial
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.domain.model.ServerQuizType

private val BookmarkBg = Color(0xFFFEF7EE)
private val BookmarkText = Color(0xFFEE7D36)

@Composable
internal fun QuizPlayAllTopBar(
    onListClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(White),
    ) {
        QuizPlayAllIconButton(
            contentDescription = "문제 목록 열기",
            onClick = onListClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 40.dp),
        ) {
            DrawListIcon(color = Gray700)
        }
        QuizPlayAllIconButton(
            contentDescription = "퀴즈 닫기",
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 40.dp),
        ) {
            DrawCloseIcon(color = Gray700)
        }
    }
}

@Composable
internal fun QuizPlayAllProgress(
    current: Int,
    total: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(21.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brown950),
            )
        }
        Text(
            text = "$current/$total",
            color = Gray900,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
internal fun QuizPlayAllQuestionHeader(
    questionNumber: Int,
    timerText: String?,
    timerWarning: Boolean,
    bookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Q",
            color = Brown950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = questionNumber.toString(),
            color = Brown950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(start = 4.dp),
        )
        QuizBookmarkButton(
            bookmarked = bookmarked,
            onClick = onBookmarkClick,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!timerText.isNullOrBlank()) {
            QuizTimerChip(
                text = timerText,
                warning = timerWarning,
            )
        }
    }
}

@Composable
internal fun QuizPlayAllOptionButton(
    option: QuizPlayAllOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Brown50 else Gray50)
            .then(
                if (selected) {
                    Modifier.border(2.dp, Brown950, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) Brown950 else Gray100),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.number.toString(),
                color = if (selected) White else Gray700,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
        Text(
            text = option.text,
            color = Gray950,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun QuizPlayOxOptionCard(
    option: QuizPlayAllOption,
    state: QuizPlayOxOptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = when (state) {
        QuizPlayOxOptionState.Default -> Gray50
        QuizPlayOxOptionState.Selected -> Brown50
        QuizPlayOxOptionState.Correct -> PositiveBg
        QuizPlayOxOptionState.Incorrect -> NegativeBg
    }
    val borderColor = when (state) {
        QuizPlayOxOptionState.Default -> null
        QuizPlayOxOptionState.Selected -> Brown950
        QuizPlayOxOptionState.Correct -> Positive
        QuizPlayOxOptionState.Incorrect -> Negative
    }
    val iconBackgroundColor = when (state) {
        QuizPlayOxOptionState.Default -> Gray100
        QuizPlayOxOptionState.Selected -> Brown50
        QuizPlayOxOptionState.Correct -> PositiveBg
        QuizPlayOxOptionState.Incorrect -> NegativeBg
    }

    Column(
        modifier = modifier
            .height(100.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(2.dp, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = enabled,
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
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.oxSymbol(),
                color = Brown950,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
        Text(
            text = option.oxLabel(),
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
internal fun QuizPlayChoiceOptionButton(
    option: QuizPlayAllOption,
    state: QuizPlayChoiceOptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    selectedBackgroundColor: Color = White,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = when (state) {
        QuizPlayChoiceOptionState.Default -> Gray50
        QuizPlayChoiceOptionState.Selected -> selectedBackgroundColor
        QuizPlayChoiceOptionState.Correct -> PositiveBg
        QuizPlayChoiceOptionState.Incorrect -> NegativeBg
    }
    val borderColor = when (state) {
        QuizPlayChoiceOptionState.Default -> null
        QuizPlayChoiceOptionState.Selected -> Brown950
        QuizPlayChoiceOptionState.Correct -> Positive
        QuizPlayChoiceOptionState.Incorrect -> Negative
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(2.dp, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizChoiceOptionIcon(state = state)
        Text(
            text = option.text,
            color = Gray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun QuizPlayAnswerDescriptionCard(
    question: QuizPlayAllQuestion,
    selectedOptionId: String?,
    modifier: Modifier = Modifier,
) {
    val correctOption = question.correctOption()
    val selectedCorrect = question.isSelectedAnswerCorrect(selectedOptionId) == true
    val correctExplanation = question.correctExplanation.orEmpty()
    val wrongExplanation = question.incorrectExplanation.orEmpty()
    var wrongExplanationExpanded by rememberSaveable(question.id, selectedOptionId) {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray50)
            .border(1.dp, Gray300, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Positive)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "정답",
                    color = White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            Text(
                text = correctOption?.answerTitle(question.questionType).orEmpty(),
                color = Gray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = correctExplanation.ifBlank { "해설이 준비되지 않았어요." },
            color = Gray900,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Gray300),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !selectedCorrect && wrongExplanation.isNotBlank(),
                    role = Role.Button,
                    onClick = { wrongExplanationExpanded = !wrongExplanationExpanded },
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (selectedCorrect) "정답 해설" else "오답 해설 보기",
                color = Gray900,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            if (!selectedCorrect && wrongExplanation.isNotBlank()) {
                DrawChevronDownCanvas(
                    color = Gray700,
                    expanded = wrongExplanationExpanded,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        if (!selectedCorrect && wrongExplanationExpanded && wrongExplanation.isNotBlank()) {
            Text(
                text = wrongExplanation,
                color = Gray900,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

internal enum class QuizPlayChoiceOptionState {
    Default,
    Selected,
    Correct,
    Incorrect,
}

internal enum class QuizPlayOxOptionState {
    Default,
    Selected,
    Correct,
    Incorrect,
}

private fun QuizPlayAllOption.oxSymbol(): String =
    when {
        (value ?: text).equals("O", ignoreCase = true) -> "O"
        (value ?: text).equals("X", ignoreCase = true) -> "X"
        text == "그렇다" || text == "맞다" -> "O"
        text == "아니다" || text == "틀리다" -> "X"
        number == 1 -> "O"
        number == 2 -> "X"
        else -> text.take(1)
    }

private fun QuizPlayAllOption.oxLabel(): String =
    when (oxSymbol()) {
        "O" -> "그렇다"
        "X" -> "아니다"
        else -> text
    }

private fun QuizPlayAllOption.answerTitle(questionType: ServerQuizType): String =
    when (questionType) {
        ServerQuizType.Ox -> "${oxSymbol()} (${oxLabel()})"
        ServerQuizType.MultipleChoice -> text
    }

@Composable
private fun QuizChoiceOptionIcon(
    state: QuizPlayChoiceOptionState,
    modifier: Modifier = Modifier,
) {
    val iconColor = when (state) {
        QuizPlayChoiceOptionState.Default -> Gray100
        QuizPlayChoiceOptionState.Selected -> Brown950
        QuizPlayChoiceOptionState.Correct -> Positive
        QuizPlayChoiceOptionState.Incorrect -> Negative
    }

    Box(
        modifier = modifier
            .size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            QuizPlayChoiceOptionState.Default -> Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(Gray100)
                    .border(1.dp, Gray300, CircleShape),
            )

            QuizPlayChoiceOptionState.Selected,
            QuizPlayChoiceOptionState.Correct -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center,
                ) {
                    DrawCheckCanvas(color = White, modifier = Modifier.size(16.dp))
                }
            }

            QuizPlayChoiceOptionState.Incorrect -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center,
                ) {
                    DrawSmallCloseIcon(color = White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
internal fun QuizPlayAllCircleButton(
    direction: QuizPlayAllMoveDirection,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (enabled) Brown950 else Gray100
    val iconColor = if (enabled) White else Gray300
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = when (direction) {
                    QuizPlayAllMoveDirection.Previous -> "이전 문제"
                    QuizPlayAllMoveDirection.Next -> "다음 문제"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 3.dp.toPx()
            val startX = if (direction == QuizPlayAllMoveDirection.Previous) 0.62f else 0.38f
            val endX = if (direction == QuizPlayAllMoveDirection.Previous) 0.38f else 0.62f
            drawLine(
                color = iconColor,
                start = Offset(size.width * startX, size.height * 0.2f),
                end = Offset(size.width * endX, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = iconColor,
                start = Offset(size.width * endX, size.height * 0.5f),
                end = Offset(size.width * startX, size.height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun QuizPlayAllQuestionListSheet(
    state: QuizPlayAllState,
    onDismiss: () -> Unit,
    onQuestionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Dimmed)
            .clickable(
                role = Role.Button,
                onClick = onDismiss,
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(White)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {},
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 7.dp)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(1000.dp))
                        .background(Gray200),
                )
                QuizPlayAllIconButton(
                    contentDescription = "문제 목록 닫기",
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                ) {
                    DrawCloseIcon(color = Gray500)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "문제 목록",
                        color = Gray950,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "푼 문제 ${state.solvedQuestionCount} | 안 푼 문제 ${state.unsolvedQuestionCount} | 나중에 다시 볼 문제 ${state.bookmarkedQuestionCount}",
                        color = Gray700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                QuizListGrid(
                    state = state,
                    onQuestionClick = onQuestionClick,
                )

                QuizListLegend()
            }
        }
    }
}

@Composable
internal fun QuizPlayAllSubmitConfirmDialog(
    unsolvedQuestions: List<QuizPlayAllQuestion>,
    bookmarkedQuestions: List<QuizPlayAllQuestion>,
    onContinueClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Dimmed),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .padding(top = 160.dp)
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(White),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "제출하기 전에 확인해주세요",
                    color = Color(0xFF2A2A2A),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                QuizSubmitCheckSection(
                    title = "안 푼 문제",
                    count = unsolvedQuestions.size,
                    questions = unsolvedQuestions,
                    color = Negative,
                    backgroundColor = NegativeBg,
                    bookmarkedIconOnFirstTag = unsolvedQuestions.firstOrNull()
                        ?.let { question -> bookmarkedQuestions.any { it.id == question.id } } == true,
                )
                QuizSubmitCheckSection(
                    title = "나중에 다시 볼 문제",
                    count = bookmarkedQuestions.size,
                    questions = bookmarkedQuestions,
                    color = BookmarkText,
                    backgroundColor = BookmarkBg,
                )
                Text(
                    text = "풀지 않은 문제는 오답으로 처리돼요.\n나중에 다시 보려고 표시한 문제는 제출 후에는 초기화 돼요. 지금 마지막으로 검토해야 해요.",
                    color = Gray700,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistiveButton(
                    text = "계속 풀기",
                    onClick = onContinueClick,
                    modifier = Modifier.weight(1f),
                )
                QuiketPrimaryButton(
                    text = "제출하기",
                    onClick = onSubmitClick,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                )
            }
        }
    }
}

@Composable
internal fun QuizPlayAllTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Tutorial),
    ) {
        TutorialMessage(
            title = "전체 문제 목록",
            body = "푼/안 푼 문제, 나중에 다시 볼 문제를 확인할 수 있어요",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 92.dp, end = 20.dp),
        )
        TutorialMessage(
            title = "찜 표시",
            body = "다시 한 번 검토해보고 싶은 문제를 체크할 수 있어요",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 58.dp, top = 168.dp, end = 20.dp),
        )
        TutorialMessage(
            title = "문제 이동",
            body = "현재 위치를 확인하고 이전/다음 문제로 이동할 수 있어요",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 120.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onDismiss,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "탭하고 퀴즈 풀기",
                color = White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            DrawChevronRightCanvas(color = White, modifier = Modifier.size(20.dp))
        }
    }
}

internal enum class QuizPlayAllMoveDirection {
    Previous,
    Next,
}

@Composable
private fun QuizBookmarkButton(
    bookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (bookmarked) BookmarkBg else Gray50)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = if (bookmarked) "찜 해제" else "찜 표시"
            },
        contentAlignment = Alignment.Center,
    ) {
        DrawBookmarkIcon(
            color = if (bookmarked) BookmarkText else Gray500,
            modifier = Modifier.size(20.dp),
            filled = bookmarked,
        )
    }
}

@Composable
private fun QuizTimerChip(
    text: String,
    warning: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (warning) NegativeBg else Gray50
    val contentColor = if (warning) Negative else Gray900

    Row(
        modifier = modifier
            .height(29.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawTimerIcon(
            color = if (warning) Negative else Gray700,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun QuizListGrid(
    state: QuizPlayAllState,
    onQuestionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.questions.chunked(7).forEach { rowQuestions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowQuestions.forEach { question ->
                    QuizListChip(
                        number = question.number,
                        selected = state.currentQuestionIndex == question.number - 1,
                        solved = state.selectedOptionIds.containsKey(question.id),
                        bookmarked = state.bookmarkedQuestionIds.contains(question.id),
                        onClick = { onQuestionClick(question.number - 1) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizListChip(
    number: Int,
    selected: Boolean,
    solved: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        selected -> Brown950
        solved -> Brown50
        else -> Gray50
    }
    val textColor = when {
        selected -> White
        solved -> Brown950
        else -> Gray700
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .then(
                if (bookmarked && !selected) {
                    Modifier.border(1.dp, BookmarkText, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
        )
        if (bookmarked) {
            DrawBookmarkIcon(
                color = BookmarkText,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 4.dp)
                    .size(10.dp),
                filled = true,
            )
        }
    }
}

@Composable
private fun QuizListLegend(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .border(width = 0.5.dp, color = Gray100)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = Brown950, text = "현재")
        LegendItem(color = Brown50, text = "푼 문제")
        LegendItem(color = Gray100, text = "미완료")
        LegendItem(color = BookmarkBg, text = "나중에 다시 볼 문제")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = text,
            color = Gray700,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun QuizSubmitCheckSection(
    title: String,
    count: Int,
    questions: List<QuizPlayAllQuestion>,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    bookmarkedIconOnFirstTag: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = Gray800,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
            Text(
                text = count.toString(),
                color = color,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            questions.take(4).forEachIndexed { index, question ->
                QuizSubmitTag(
                    text = "${question.number}번",
                    color = color,
                    backgroundColor = backgroundColor,
                    showBookmark = bookmarkedIconOnFirstTag && index == 0,
                )
            }
        }
    }
}

@Composable
private fun QuizSubmitTag(
    text: String,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    showBookmark: Boolean = false,
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (showBookmark) {
            DrawBookmarkIcon(
                color = color,
                modifier = Modifier.size(16.dp),
                filled = true,
            )
        }
    }
}

@Composable
private fun AssistiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(2.dp, Brown950, RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Gray950,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun TutorialMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Tutorial)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = White,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Text(
            text = body,
            color = Gray200,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun QuizPlayAllIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DrawListIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.4.dp.toPx()
        val left = size.width * 0.18f
        val right = size.width * 0.82f
        listOf(0.28f, 0.5f, 0.72f).forEach { yFraction ->
            drawLine(
                color = color,
                start = Offset(left, size.height * yFraction),
                end = Offset(right, size.height * yFraction),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun DrawCloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
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
private fun DrawSmallCloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.3f, size.height * 0.3f),
            end = Offset(size.width * 0.7f, size.height * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.7f, size.height * 0.3f),
            end = Offset(size.width * 0.3f, size.height * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawCheckCanvas(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.52f),
            end = Offset(size.width * 0.42f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.72f),
            end = Offset(size.width * 0.78f, size.height * 0.3f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawBookmarkIcon(
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.16f)
            lineTo(size.width * 0.72f, size.height * 0.16f)
            lineTo(size.width * 0.72f, size.height * 0.84f)
            lineTo(size.width * 0.5f, size.height * 0.68f)
            lineTo(size.width * 0.28f, size.height * 0.84f)
            close()
        }
        if (filled) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun DrawTimerIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.38f,
            style = Stroke(width = 1.8.dp.toPx()),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.5f, size.height * 0.28f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.66f, size.height * 0.58f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawChevronRightCanvas(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.36f, size.height * 0.2f),
            end = Offset(size.width * 0.62f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.62f, size.height * 0.5f),
            end = Offset(size.width * 0.36f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawChevronDownCanvas(
    color: Color,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val topY = if (expanded) 0.62f else 0.38f
        val bottomY = if (expanded) 0.38f else 0.62f
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * topY),
            end = Offset(size.width * 0.5f, size.height * bottomY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * bottomY),
            end = Offset(size.width * 0.78f, size.height * topY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
