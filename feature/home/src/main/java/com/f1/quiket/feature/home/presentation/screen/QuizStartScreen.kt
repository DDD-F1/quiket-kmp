package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.component.QuiketTimerInputDialog
import com.f1.quiket.core.designsystem.component.QuiketTimerInputUnit
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.Tutorial
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.core.designsystem.theme.Yellow100
import com.f1.quiket.core.designsystem.theme.Yellow50
import com.f1.quiket.core.designsystem.theme.Yellow500
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.R

data class QuizStartConfig(
    val playMode: QuizPlayMode,
    val timerEnabled: Boolean,
    val timerScope: QuizTimerScope?,
    val timerSeconds: Int?,
)

@Composable
fun QuizStartRoute(
    quizSessionId: String?,
    onBackClick: () -> Unit,
    onStartClick: (QuizStartConfig) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizStartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(quizSessionId) {
        viewModel.onIntent(QuizStartIntent.Load(quizSessionId))
    }

    QuizStartScreen(
        state = state,
        onBackClick = onBackClick,
        onStartClick = onStartClick,
        modifier = modifier,
    )
}

@Composable
fun QuizStartScreen(
    state: QuizStartState = QuizStartState(summary = quizStartPreviewSummary()),
    onBackClick: () -> Unit,
    onStartClick: (QuizStartConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPlayMode by rememberSaveable { mutableStateOf(QuizStartPlayMode.OneByOne) }
    var selectedTimerOption by rememberSaveable { mutableStateOf(QuizStartTimerOption.None) }
    var playModeTooltipVisible by rememberSaveable { mutableStateOf(false) }
    var timerDialogVisible by rememberSaveable { mutableStateOf(false) }
    var timerDialogMode by rememberSaveable { mutableStateOf(QuizStartTimerDialogMode.PerQuestion) }
    var timerDialogValue by rememberSaveable { mutableStateOf(QuizStartTimerDialogMode.PerQuestion.defaultValue) }
    var timerDialogUnit by rememberSaveable { mutableStateOf(QuiketTimerInputUnit.Seconds) }
    var timerOptionBeforeDialog by rememberSaveable { mutableStateOf(QuizStartTimerOption.None) }
    var timerOptionSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
    var timerOptionScopeValue by rememberSaveable { mutableStateOf<String?>(null) }
    var playModeSectionOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val canConfigureQuiz = state.summary != null && !state.isLoading && state.errorMessage == null

    fun resetTimerOption() {
        selectedTimerOption = QuizStartTimerOption.None
        timerOptionSeconds = null
        timerOptionScopeValue = null
        timerDialogVisible = false
    }

    fun openTimerDialog(mode: QuizStartTimerDialogMode) {
        playModeTooltipVisible = false
        timerDialogMode = mode
        timerDialogValue = mode.defaultValue
        timerDialogUnit = mode.defaultUnit
        timerDialogVisible = true
    }

    fun dismissTimerDialog() {
        selectedTimerOption = timerOptionBeforeDialog
        timerDialogVisible = false
    }

    BackHandler(enabled = playModeTooltipVisible) {
        playModeTooltipVisible = false
    }

    LaunchedEffect(canConfigureQuiz) {
        if (!canConfigureQuiz) {
            playModeTooltipVisible = false
            resetTimerOption()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            CreateQuizTopBar(
                title = state.summary?.title ?: "퀴즈",
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                QuizStartOverview(
                    summary = state.summary,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                )

                if (canConfigureQuiz) {
                    PlayModeSection(
                        selectedPlayMode = selectedPlayMode,
                        onInfoClick = { playModeTooltipVisible = true },
                        onPlayModeClick = { playMode ->
                            if (selectedPlayMode != playMode) {
                                selectedPlayMode = playMode
                                resetTimerOption()
                            }
                        },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            playModeSectionOffset = coordinates.positionInRoot()
                        },
                    )

                    TimerSection(
                        selectedTimerOption = selectedTimerOption,
                        onTimerOptionClick = { timerOption ->
                            when (timerOption) {
                                QuizStartTimerOption.None -> resetTimerOption()
                                QuizStartTimerOption.Custom -> {
                                    timerOptionBeforeDialog = selectedTimerOption
                                    selectedTimerOption = QuizStartTimerOption.Custom
                                    openTimerDialog(
                                        mode = if (selectedPlayMode == QuizStartPlayMode.AllAtOnce) {
                                            QuizStartTimerDialogMode.Total
                                        } else {
                                            QuizStartTimerDialogMode.PerQuestion
                                        },
                                    )
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(108.dp))
            }
        }

        QuiketPrimaryButton(
            text = "퀴즈 시작하기",
            enabled = canConfigureQuiz,
            onClick = {
                val timerScope = timerOptionScopeValue.toQuizTimerScope()
                val effectiveTimerSeconds = timerOptionSeconds
                    .takeIf { selectedTimerOption == QuizStartTimerOption.Custom }
                onStartClick(
                    QuizStartConfig(
                        playMode = selectedPlayMode.toDomain(),
                        timerEnabled = effectiveTimerSeconds != null,
                        timerScope = timerScope.takeIf {
                            selectedTimerOption == QuizStartTimerOption.Custom &&
                                effectiveTimerSeconds != null
                        },
                        timerSeconds = effectiveTimerSeconds,
                    ),
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        )

        if (playModeTooltipVisible) {
            QuizStartPlayModeTooltip(
                onDismiss = { playModeTooltipVisible = false },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = with(density) { playModeSectionOffset.x.toDp() } + 61.dp,
                        y = with(density) { playModeSectionOffset.y.toDp() } + 43.dp,
                    )
                    .zIndex(2f),
            )
        }

        if (timerDialogVisible) {
            QuiketTimerInputDialog(
                title = timerDialogMode.title,
                value = timerDialogValue,
                onValueChange = { input ->
                    timerDialogValue = input.filter(Char::isDigit)
                },
                selectedUnit = timerDialogUnit,
                onUnitClick = { timerDialogUnit = it },
                onDismiss = ::dismissTimerDialog,
                onApply = {
                    val appliedSeconds = timerDialogValue.toTimerSeconds(timerDialogUnit)
                    selectedTimerOption = QuizStartTimerOption.Custom
                    timerOptionSeconds = appliedSeconds
                    timerOptionScopeValue = timerDialogMode.timerScope.wireValue
                    timerDialogVisible = false
                },
            )
        }
    }
}

@Composable
private fun QuizStartOverview(
    summary: QuizStartSummaryUiModel?,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .background(Yellow50),
        ) {
            Image(
                painter = painterResource(R.drawable.illust_quiz_solve),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (summary != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = summary.title,
                        color = Gray950,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 24.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        QuizStartDescriptionText(text = summary.quizTypeLabel)
                        summary.choiceLabel?.let { choiceLabel ->
                            QuizStartDescriptionText(text = "·")
                            QuizStartDescriptionText(text = choiceLabel)
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    QuizInfoTag(text = summary.questionCountLabel)
                    QuizInfoTag(text = summary.difficultyLabel)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    summary.scopeLabels.take(3).forEach { label ->
                        QuizScopeChip(text = label)
                    }
                }
            } else {
                Text(
                    text = when {
                        isLoading -> "퀴즈 정보를 불러오는 중이에요"
                        !errorMessage.isNullOrBlank() -> errorMessage
                        else -> "퀴즈 정보를 불러올 수 없어요"
                    },
                    color = Gray700,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
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
        color = Gray700,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = modifier,
    )
}

@Composable
private fun QuizInfoTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Yellow100)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Yellow500,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun QuizScopeChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(1000.dp))
            .background(Gray100)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Gray800,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun PlayModeSection(
    selectedPlayMode: QuizStartPlayMode,
    onInfoClick: () -> Unit,
    onPlayModeClick: (QuizStartPlayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(83.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuizStartSectionTitle(
                title = "풀기 방식",
                onInfoClick = onInfoClick,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CreateQuizSelectButton(
                    text = QuizStartPlayMode.OneByOne.title,
                    selected = selectedPlayMode == QuizStartPlayMode.OneByOne,
                    onClick = { onPlayModeClick(QuizStartPlayMode.OneByOne) },
                    modifier = Modifier.weight(1f),
                )
                CreateQuizSelectButton(
                    text = QuizStartPlayMode.AllAtOnce.title,
                    selected = selectedPlayMode == QuizStartPlayMode.AllAtOnce,
                    onClick = { onPlayModeClick(QuizStartPlayMode.AllAtOnce) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TimerSection(
    selectedTimerOption: QuizStartTimerOption,
    onTimerOptionClick: (QuizStartTimerOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizStartSectionTitle(title = "타이머 설정")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CreateQuizSelectButton(
                text = QuizStartTimerOption.None.title,
                selected = selectedTimerOption == QuizStartTimerOption.None,
                onClick = { onTimerOptionClick(QuizStartTimerOption.None) },
                modifier = Modifier.weight(1f),
            )
            CreateQuizSelectButton(
                text = QuizStartTimerOption.Custom.title,
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
    onInfoClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(27.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Gray950,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (onInfoClick != null) {
            QuizStartInfoButton(onClick = onInfoClick)
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
            .size(24.dp)
            .clip(CircleShape)
            .background(Gray400)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = "풀기 방식 도움말"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "i",
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
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
                color = Tutorial,
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
                color = Tutorial,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 20.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "풀기 방식",
                    color = White,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "•  한 문제씩 풀기: 한 문제씩 풀면서 바로\n   정답을 확인할 수 있어요\n•  한번에 풀기: 전체 문제를 연이어 풀고\n   정답과 풀이도 한번에 확인해요",
                    color = Gray200,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
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
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = "도움말 닫기"
            },
    ) {
        val strokeWidth = 3.dp.toPx()
        drawLine(
            color = Gray200,
            start = Offset(size.width * 0.22f, size.height * 0.22f),
            end = Offset(size.width * 0.78f, size.height * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Gray200,
            start = Offset(size.width * 0.78f, size.height * 0.22f),
            end = Offset(size.width * 0.22f, size.height * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private enum class QuizStartPlayMode(
    val title: String,
) {
    OneByOne("한 문제씩 풀기"),
    AllAtOnce("한번에 풀기"),
}

private enum class QuizStartTimerOption(
    val title: String,
) {
    None("설정 안 함"),
    Custom("직접 입력"),
}

private enum class QuizStartTimerDialogMode(
    val title: String,
    val defaultValue: String,
    val defaultUnit: QuiketTimerInputUnit,
    val timerScope: QuizTimerScope,
) {
    PerQuestion(
        title = "문제당 타이머 설정",
        defaultValue = "30",
        defaultUnit = QuiketTimerInputUnit.Seconds,
        timerScope = QuizTimerScope.PerQuestion,
    ),
    Total(
        title = "전체 타이머 설정",
        defaultValue = "25",
        defaultUnit = QuiketTimerInputUnit.Minutes,
        timerScope = QuizTimerScope.Total,
    ),
}

private fun QuizStartPlayMode.toDomain(): QuizPlayMode =
    when (this) {
        QuizStartPlayMode.OneByOne -> QuizPlayMode.OneByOne
        QuizStartPlayMode.AllAtOnce -> QuizPlayMode.AllAtOnce
    }

private fun String?.toQuizTimerScope(): QuizTimerScope? =
    QuizTimerScope.entries.firstOrNull { scope -> scope.wireValue == this }

private fun String.toTimerSeconds(unit: QuiketTimerInputUnit): Int? {
    val value = toIntOrNull()?.takeIf { it > 0 } ?: return null
    return when (unit) {
        QuiketTimerInputUnit.Seconds -> value
        QuiketTimerInputUnit.Minutes -> value * 60
    }
}

private fun quizStartPreviewSummary(): QuizStartSummaryUiModel = QuizStartSummaryUiModel(
    title = "SQLD",
    quizTypeLabel = "객관식",
    choiceLabel = "4지선다",
    questionCountLabel = "5문제",
    difficultyLabel = "난이도: 보통",
    scopeLabels = listOf("챕터 1 / 파트 1,3", "챕터 2 / 전체"),
)

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun QuizStartScreenPreview() {
    QuiketTheme {
        QuizStartScreen(
            onBackClick = {},
            onStartClick = {},
        )
    }
}
