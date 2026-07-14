package com.f1.quiket.composeapp.quiz

import org.koin.compose.koinInject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayMode
import com.f1.quiket.composeapp.quiz.domain.model.QuizTimerScope
import com.f1.quiket.composeapp.quiz.presentation.QuizStartStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizStartSummary
import com.f1.quiket.composeapp.quiz.presentation.QuizStartUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.feature.quiz.resources.Res
import com.f1.quiket.feature.quiz.resources.illust_quiz_solve

@Composable
fun QuizStartRoute(
    quizSessionId: String,
    onBackClick: () -> Unit,
    onStartClick: (QuizPlayLaunchConfig) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<QuizStartStateHolder>()
    val state = stateHolder.state

    fun loadQuizSession() {
        coroutineScope.launch {
            stateHolder.loadQuizSession(
                quizSessionId = quizSessionId,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    LaunchedEffect(quizSessionId) {
        loadQuizSession()
    }

    QuizStartScreen(
        state = state,
        onBackClick = onBackClick,
        onRetryClick = ::loadQuizSession,
        onStartClick = { config ->
            onStartClick(config.copy(quizSessionId = quizSessionId))
        },
        modifier = modifier,
    )
}

@Composable
private fun QuizStartScreen(
    state: QuizStartUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onStartClick: (QuizPlayLaunchConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPlayMode by remember { mutableStateOf(QuizPlayMode.OneByOne) }
    var selectedTimerOption by remember { mutableStateOf(QuizStartTimerOption.None) }
    var playModeTooltipVisible by remember { mutableStateOf(false) }
    var timerDialogVisible by remember { mutableStateOf(false) }
    var timerDialogMode by remember { mutableStateOf(QuizStartTimerDialogMode.PerQuestion) }
    var timerDialogValue by remember { mutableStateOf(QuizStartTimerDialogMode.PerQuestion.defaultValue) }
    var timerDialogUnit by remember { mutableStateOf(QuizStartTimerUnit.Seconds) }
    var timerOptionBeforeDialog by remember { mutableStateOf(QuizStartTimerOption.None) }
    var timerSeconds by remember { mutableStateOf<Int?>(null) }
    var timerScope by remember { mutableStateOf<QuizTimerScope?>(null) }
    val canStart = state is QuizStartUiState.Ready

    fun resetTimer() {
        playModeTooltipVisible = false
        selectedTimerOption = QuizStartTimerOption.None
        timerOptionBeforeDialog = QuizStartTimerOption.None
        timerSeconds = null
        timerScope = null
        timerDialogVisible = false
    }

    fun openTimerDialog() {
        playModeTooltipVisible = false
        val mode = if (selectedPlayMode == QuizPlayMode.AllAtOnce) {
            QuizStartTimerDialogMode.Total
        } else {
            QuizStartTimerDialogMode.PerQuestion
        }
        timerDialogMode = mode
        timerDialogValue = mode.defaultValue
        timerDialogUnit = mode.defaultUnit
        timerDialogVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizStartTopBar(
                title = (state as? QuizStartUiState.Ready)?.summary?.title ?: "퀴즈",
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                QuizStartOverview(
                    state = state,
                    onRetryClick = onRetryClick,
                )

                if (canStart) {
                    QuizStartPlayModeSection(
                        selectedPlayMode = selectedPlayMode,
                        onInfoClick = { playModeTooltipVisible = true },
                        onPlayModeClick = { nextMode ->
                            if (selectedPlayMode != nextMode) {
                                selectedPlayMode = nextMode
                                resetTimer()
                            }
                        },
                    )
                    if (playModeTooltipVisible) {
                        QuizStartPlayModeTooltip(
                            onDismiss = { playModeTooltipVisible = false },
                            modifier = Modifier.zIndex(2f),
                        )
                    }
                    QuizStartTimerSection(
                        selectedTimerOption = selectedTimerOption,
                        onTimerOptionClick = { option ->
                            when (option) {
                                QuizStartTimerOption.None -> resetTimer()
                                QuizStartTimerOption.Custom -> {
                                    timerOptionBeforeDialog = selectedTimerOption
                                    selectedTimerOption = QuizStartTimerOption.Custom
                                    openTimerDialog()
                                }
                            }
                        },
                    )
                }
            }
        }

        QuiketPrimaryButton(
            text = "퀴즈 시작하기",
            enabled = canStart,
            onClick = {
                onStartClick(
                    QuizPlayLaunchConfig(
                        quizSessionId = "",
                        playMode = selectedPlayMode,
                        timerEnabled = selectedTimerOption == QuizStartTimerOption.Custom &&
                            timerSeconds != null,
                        timerScope = timerScope.takeIf {
                            selectedTimerOption == QuizStartTimerOption.Custom &&
                                timerSeconds != null
                        },
                        timerSeconds = timerSeconds.takeIf {
                            selectedTimerOption == QuizStartTimerOption.Custom
                        },
                    ),
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )

        if (timerDialogVisible) {
            QuizStartTimerDialog(
                mode = timerDialogMode,
                value = timerDialogValue,
                onValueChange = { input -> timerDialogValue = input.filter(Char::isDigit).take(3) },
                selectedUnit = timerDialogUnit,
                onUnitClick = { timerDialogUnit = it },
                onDismiss = {
                    selectedTimerOption = timerOptionBeforeDialog
                    timerDialogVisible = false
                },
                onApply = {
                    timerSeconds = timerDialogValue.toTimerSeconds(timerDialogUnit)
                    timerScope = timerDialogMode.timerScope
                    timerDialogVisible = false
                },
            )
        }
    }
}

@Composable
private fun QuizStartTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
    ) {
        Text(
            text = "‹",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 34.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .semantics { contentDescription = "뒤로" }
                .clickable(role = Role.Button, onClick = onBackClick),
        )
        Text(
            text = title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 72.dp, end = 72.dp, top = 48.dp),
        )
    }
}

@Composable
private fun QuizStartOverview(
    state: QuizStartUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .background(QuiketBrown50),
        ) {
            Image(
                painter = painterResource(Res.drawable.illust_quiz_solve),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        when (state) {
            QuizStartUiState.Loading -> QuizStartInfoCard(
                title = "퀴즈 정보를 불러오는 중이에요",
                description = "문제와 설정 정보를 확인하고 있어요.",
            )

            is QuizStartUiState.Error -> QuizStartInfoCard(
                title = "퀴즈 정보를 불러올 수 없어요",
                description = state.message,
                actionText = "다시 시도",
                onActionClick = onRetryClick,
            )

            is QuizStartUiState.Ready -> QuizStartSummary(summary = state.summary)
        }
    }
}

@Composable
private fun QuizStartSummary(
    summary: QuizStartSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = summary.title,
            color = QuiketGray950,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuizStartDescriptionText(summary.quizTypeLabel)
            summary.choiceLabel?.let { choiceLabel ->
                QuizStartDescriptionText("·")
                QuizStartDescriptionText(choiceLabel)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizStartInfoTag(text = summary.questionCountLabel)
            QuizStartInfoTag(text = summary.difficultyLabel)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            summary.scopeLabels.take(3).forEach { label ->
                QuizStartScopeChip(text = label)
            }
        }
    }
}

@Composable
private fun QuizStartPlayModeSection(
    selectedPlayMode: QuizPlayMode,
    onInfoClick: () -> Unit,
    onPlayModeClick: (QuizPlayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuizStartSectionTitle("풀기 방식")
            QuizStartInfoButton(onClick = onInfoClick)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuizStartSelectButton(
                text = "한 문제씩 풀기",
                selected = selectedPlayMode == QuizPlayMode.OneByOne,
                onClick = { onPlayModeClick(QuizPlayMode.OneByOne) },
                modifier = Modifier.weight(1f),
            )
            QuizStartSelectButton(
                text = "한번에 풀기",
                selected = selectedPlayMode == QuizPlayMode.AllAtOnce,
                onClick = { onPlayModeClick(QuizPlayMode.AllAtOnce) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuizStartInfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(QuiketGray400)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "풀기 방식 도움말" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            color = QuiketWhite,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun QuizStartPlayModeTooltip(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(246.dp)
            .height(125.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val arrowStartX = 15.dp.toPx()
            val arrowWidth = 14.dp.toPx()
            val arrowHeight = 8.dp.toPx()
            val bodyTop = arrowHeight
            val radius = 12.dp.toPx()

            drawRoundRect(
                color = QuiketBrown950,
                topLeft = Offset(0f, bodyTop),
                size = Size(size.width, size.height - bodyTop),
                cornerRadius = CornerRadius(radius, radius),
            )
            drawPath(
                path = Path().apply {
                    moveTo(arrowStartX, bodyTop)
                    lineTo(arrowStartX + arrowWidth / 2f, 0f)
                    lineTo(arrowStartX + arrowWidth, bodyTop)
                    close()
                },
                color = QuiketBrown950,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 20.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "풀기 방식",
                    color = QuiketWhite,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "• 한 문제씩 풀기: 바로 정답을 확인할 수 있어요\n• 한번에 풀기: 전체 문제를 풀고 한번에 확인해요",
                    color = QuiketGray300,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            QuizStartTooltipCloseButton(
                onClick = onDismiss,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun QuizStartTooltipCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "도움말 닫기" },
    ) {
        val strokeWidth = 2.5.dp.toPx()
        drawLine(
            color = QuiketGray300,
            start = Offset(size.width * 0.24f, size.height * 0.24f),
            end = Offset(size.width * 0.76f, size.height * 0.76f),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = QuiketGray300,
            start = Offset(size.width * 0.76f, size.height * 0.24f),
            end = Offset(size.width * 0.24f, size.height * 0.76f),
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun QuizStartTimerSection(
    selectedTimerOption: QuizStartTimerOption,
    onTimerOptionClick: (QuizStartTimerOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizStartSectionTitle("타이머 설정")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuizStartSelectButton(
                text = "설정 안 함",
                selected = selectedTimerOption == QuizStartTimerOption.None,
                onClick = { onTimerOptionClick(QuizStartTimerOption.None) },
                modifier = Modifier.weight(1f),
            )
            QuizStartSelectButton(
                text = "직접 입력",
                selected = selectedTimerOption == QuizStartTimerOption.Custom,
                onClick = { onTimerOptionClick(QuizStartTimerOption.Custom) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuizStartSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = QuiketGray950,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
    )
}

@Composable
private fun QuizStartSelectButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(shape)
            .background(if (selected) QuiketWhite else QuiketGray50)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun QuizStartInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketGray50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketGray700,
            style = MaterialTheme.typography.bodySmall,
        )
        if (actionText != null && onActionClick != null) {
            QuiketPrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun QuizStartDescriptionText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray700,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        modifier = modifier,
    )
}

@Composable
private fun QuizStartInfoTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFFF1C8))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketOrange500,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun QuizStartScopeChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(22.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(QuiketGray100)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = QuiketGray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun QuizStartTimerDialog(
    mode: QuizStartTimerDialogMode,
    value: String,
    onValueChange: (String) -> Unit,
    selectedUnit: QuizStartTimerUnit,
    onUnitClick: (QuizStartTimerUnit) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {}),
            color = QuiketWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = mode.title,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                QuiketTextField(
                    value = value,
                    onValueChange = onValueChange,
                    hint = "시간 입력",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuizStartUnitButton(
                        text = "초",
                        selected = selectedUnit == QuizStartTimerUnit.Seconds,
                        onClick = { onUnitClick(QuizStartTimerUnit.Seconds) },
                        modifier = Modifier.weight(1f),
                    )
                    QuizStartUnitButton(
                        text = "분",
                        selected = selectedUnit == QuizStartTimerUnit.Minutes,
                        onClick = { onUnitClick(QuizStartTimerUnit.Minutes) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuiketPrimaryButton(
                        text = "취소",
                        onClick = onDismiss,
                        containerColor = QuiketGray100,
                        contentColor = QuiketGray700,
                        modifier = Modifier.weight(1f),
                    )
                    QuiketPrimaryButton(
                        text = "적용",
                        enabled = value.toIntOrNull()?.let { it > 0 } == true,
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizStartUnitButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) QuiketBrown50 else QuiketGray50)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) QuiketBrown950 else Color.Transparent,
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) QuiketBrown950 else QuiketGray600,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

private enum class QuizStartTimerOption {
    None,
    Custom,
}

private enum class QuizStartTimerUnit {
    Seconds,
    Minutes,
}

private enum class QuizStartTimerDialogMode(
    val title: String,
    val defaultValue: String,
    val defaultUnit: QuizStartTimerUnit,
    val timerScope: QuizTimerScope,
) {
    PerQuestion(
        title = "문제당 타이머 설정",
        defaultValue = "30",
        defaultUnit = QuizStartTimerUnit.Seconds,
        timerScope = QuizTimerScope.PerQuestion,
    ),
    Total(
        title = "전체 타이머 설정",
        defaultValue = "25",
        defaultUnit = QuizStartTimerUnit.Minutes,
        timerScope = QuizTimerScope.Total,
    ),
}

private fun String.toTimerSeconds(unit: QuizStartTimerUnit): Int? {
    val numericValue = toIntOrNull()?.takeIf { it > 0 } ?: return null
    return when (unit) {
        QuizStartTimerUnit.Seconds -> numericValue
        QuizStartTimerUnit.Minutes -> numericValue * 60
    }
}
