package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizTimerScope
import com.f1.quiket.feature.home.domain.model.ServerQuizType

@Composable
fun QuizPlayAllRoute(
    quizSessionId: String? = null,
    playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    timerEnabled: Boolean = false,
    timerScope: QuizTimerScope? = null,
    timerSeconds: Int? = null,
    clientSessionId: String? = null,
    playSessionId: String? = null,
    playType: QuizPlayType = QuizPlayType.First,
    onCloseClick: () -> Unit,
    onResultReady: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: QuizPlayAllViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        quizSessionId,
        playMode,
        timerEnabled,
        timerScope,
        timerSeconds,
        clientSessionId,
        playSessionId,
        playType,
    ) {
        val hasExistingPlaySession = !clientSessionId.isNullOrBlank() && !playSessionId.isNullOrBlank()
        if (!hasExistingPlaySession) {
            viewModel.onIntent(
                QuizPlayAllIntent.ConfigurePlay(
                    playMode = playMode,
                    timerEnabled = timerEnabled,
                    timerScope = timerScope,
                    timerSeconds = timerSeconds,
                ),
            )
        }
        if (!quizSessionId.isNullOrBlank()) {
            viewModel.onIntent(
                QuizPlayAllIntent.LoadQuizSession(
                    quizSessionId = quizSessionId,
                    clientSessionId = clientSessionId,
                    playSessionId = playSessionId,
                    playType = playType,
                ),
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuizPlayAllEffect.NavigateToResult -> onResultReady(effect.resultId)
            }
        }
    }

    QuizPlayAllScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onCloseClick = onCloseClick,
        modifier = modifier,
    )
}

@Composable
fun QuizPlayAllScreen(
    state: QuizPlayAllState,
    onIntent: (QuizPlayAllIntent) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentQuestion = state.currentQuestion

    BackHandler(
        enabled = state.showTutorial || state.isQuestionListVisible || state.isSubmitConfirmVisible,
    ) {
        when {
            state.showTutorial -> onIntent(QuizPlayAllIntent.DismissTutorial)
            state.isSubmitConfirmVisible -> onIntent(QuizPlayAllIntent.CloseSubmitConfirm)
            state.isQuestionListVisible -> onIntent(QuizPlayAllIntent.CloseQuestionList)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizPlayAllTopBar(
                onListClick = { onIntent(QuizPlayAllIntent.OpenQuestionList) },
                onCloseClick = onCloseClick,
            )

            QuizPlayAllProgress(
                current = state.currentQuestionNumber.coerceAtMost(state.totalQuestionCount),
                total = state.totalQuestionCount,
                progress = state.progressFraction,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            if (currentQuestion == null) {
                QuizPlayAllEmptyContent(
                    message = when {
                        state.isLoading -> "퀴즈를 불러오는 중이에요"
                        !state.errorMessage.isNullOrBlank() -> state.errorMessage
                        else -> "문제를 불러올 수 없어요"
                    },
                    showRetry = !state.isLoading &&
                        !state.errorMessage.isNullOrBlank() &&
                        !state.quizSessionId.isNullOrBlank(),
                    onRetryClick = { onIntent(QuizPlayAllIntent.RetryLoadQuizSession) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                QuizPlayAllContent(
                    question = currentQuestion,
                    selectedOptionId = state.selectedOptionId,
                    timerText = state.playTimerText(),
                    timerWarning = state.playTimerRemainingSeconds()?.let { it <= 10 } == true,
                    isOneByOneMode = state.isOneByOneMode,
                    isAnswerChecked = state.isCurrentQuestionChecked,
                    bookmarked = state.isCurrentQuestionBookmarked,
                    onOptionClick = { optionId -> onIntent(QuizPlayAllIntent.SelectOption(optionId)) },
                    onBookmarkClick = { onIntent(QuizPlayAllIntent.ToggleBookmark) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                )
            }
        }

        if (currentQuestion != null) {
            if (state.isOneByOneMode) {
                QuizPlayOneByOneBottomBar(
                    text = when {
                        state.isCurrentQuestionChecked && state.isLastQuestion -> "결과 보기"
                        state.isCurrentQuestionChecked -> "다음"
                        else -> "정답 확인"
                    },
                    enabled = if (state.isCurrentQuestionChecked) {
                        !state.isSubmitting
                    } else {
                        state.canCheckCurrentQuestion && !state.isSubmitting
                    },
                    onClick = {
                        when {
                            !state.isCurrentQuestionChecked -> {
                                onIntent(QuizPlayAllIntent.CheckCurrentAnswer)
                            }
                            state.isLastQuestion -> {
                                onIntent(QuizPlayAllIntent.Submit)
                            }
                            else -> {
                                onIntent(QuizPlayAllIntent.MoveNext)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            } else {
                QuizPlayAllBottomBar(
                    canMovePrevious = state.canMovePrevious,
                    canMoveNext = state.canMoveNext,
                    isLastQuestion = state.isLastQuestion,
                    onPreviousClick = { onIntent(QuizPlayAllIntent.MovePrevious) },
                    onNextClick = { onIntent(QuizPlayAllIntent.MoveNext) },
                    onSubmitClick = { onIntent(QuizPlayAllIntent.OpenSubmitConfirm) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        if (state.isQuestionListVisible) {
            QuizPlayAllQuestionListSheet(
                state = state,
                onDismiss = { onIntent(QuizPlayAllIntent.CloseQuestionList) },
                onQuestionClick = { index -> onIntent(QuizPlayAllIntent.SelectQuestion(index)) },
            )
        }

        if (state.isSubmitConfirmVisible) {
            QuizPlayAllSubmitConfirmDialog(
                unsolvedQuestions = state.unsolvedQuestions,
                bookmarkedQuestions = state.bookmarkedQuestions,
                onContinueClick = { onIntent(QuizPlayAllIntent.CloseSubmitConfirm) },
                onSubmitClick = { onIntent(QuizPlayAllIntent.Submit) },
            )
        }

        if (state.showTutorial && currentQuestion != null) {
            QuizPlayAllTutorialOverlay(
                onDismiss = { onIntent(QuizPlayAllIntent.DismissTutorial) },
            )
        }
    }
}

@Composable
private fun QuizPlayAllContent(
    question: QuizPlayAllQuestion,
    selectedOptionId: String?,
    timerText: String?,
    timerWarning: Boolean,
    isOneByOneMode: Boolean,
    isAnswerChecked: Boolean,
    bookmarked: Boolean,
    onOptionClick: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 28.dp, bottom = 116.dp),
    ) {
        QuizPlayAllQuestionHeader(
            questionNumber = question.number,
            timerText = timerText,
            timerWarning = timerWarning,
            bookmarked = bookmarked,
            onBookmarkClick = onBookmarkClick,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = question.body,
            color = Gray950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isOneByOneMode) {
            when (question.questionType) {
                ServerQuizType.Ox -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        question.options.forEach { option ->
                            QuizPlayOxOptionCard(
                                option = option,
                                state = question.oxOptionState(
                                    option = option,
                                    selectedOptionId = selectedOptionId,
                                    isAnswerChecked = isAnswerChecked,
                                ),
                                enabled = !isAnswerChecked,
                                onClick = { onOptionClick(option.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                ServerQuizType.MultipleChoice -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        question.options.forEach { option ->
                            QuizPlayChoiceOptionButton(
                                option = option,
                                state = question.choiceOptionState(
                                    option = option,
                                    selectedOptionId = selectedOptionId,
                                    isAnswerChecked = isAnswerChecked,
                                ),
                                enabled = !isAnswerChecked,
                                onClick = { onOptionClick(option.id) },
                            )
                        }
                    }
                }
            }

            if (isAnswerChecked) {
                Spacer(modifier = Modifier.height(16.dp))
                QuizPlayAnswerDescriptionCard(
                    question = question,
                    selectedOptionId = selectedOptionId,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (question.questionType) {
                    ServerQuizType.MultipleChoice -> {
                        question.options.forEach { option ->
                            QuizPlayChoiceOptionButton(
                                option = option,
                                state = if (selectedOptionId == option.id) {
                                    QuizPlayChoiceOptionState.Selected
                                } else {
                                    QuizPlayChoiceOptionState.Default
                                },
                                enabled = true,
                                onClick = { onOptionClick(option.id) },
                                selectedBackgroundColor = Brown50,
                            )
                        }
                    }

                    ServerQuizType.Ox -> {
                        question.options.forEach { option ->
                            QuizPlayAllOptionButton(
                                option = option,
                                selected = selectedOptionId == option.id,
                                onClick = { onOptionClick(option.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizPlayAllEmptyContent(
    message: String,
    showRetry: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                color = Gray700,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
            if (showRetry) {
                QuiketPrimaryButton(
                    text = "다시 불러오기",
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuizPlayAllBottomBar(
    canMovePrevious: Boolean,
    canMoveNext: Boolean,
    isLastQuestion: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(White)
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = if (isLastQuestion) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizPlayAllCircleButton(
            direction = QuizPlayAllMoveDirection.Previous,
            enabled = canMovePrevious,
            onClick = onPreviousClick,
        )

        if (isLastQuestion) {
            QuizPlayAllSubmitButton(
                onClick = onSubmitClick,
                modifier = Modifier.weight(1f),
            )
        }

        QuizPlayAllCircleButton(
            direction = QuizPlayAllMoveDirection.Next,
            enabled = canMoveNext,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun QuizPlayOneByOneBottomBar(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(White.copy(alpha = 0f), White, White),
                ),
            )
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        QuiketPrimaryButton(
            text = text,
            enabled = enabled,
            onClick = onClick,
        )
    }
}

@Composable
private fun QuizPlayAllSubmitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brown950)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "제출하기",
            color = White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private fun QuizPlayAllState.playTimerText(): String? =
    when {
        !timerEnabled -> null
        timerScope == QuizTimerScope.Total -> playTimerRemainingSeconds()?.formatPlayTimerText()
        isOneByOneMode -> playTimerRemainingSeconds()?.formatPlayTimerText()
        else -> currentQuestion?.timerText
    }

private fun QuizPlayAllState.playTimerRemainingSeconds(): Int? =
    when {
        !timerEnabled -> null
        timerScope == QuizTimerScope.Total -> remainingTotalSeconds ?: timerSeconds
        isOneByOneMode -> currentRemainingSeconds ?: timerSeconds
        else -> null
    }

private fun Int.formatPlayTimerText(): String =
    when {
        this < 60 -> "${this}초"
        this % 60 == 0 -> "${this / 60}분"
        else -> "%d:%02d".format(this / 60, this % 60)
    }

private fun QuizPlayAllQuestion.oxOptionState(
    option: QuizPlayAllOption,
    selectedOptionId: String?,
    isAnswerChecked: Boolean,
): QuizPlayOxOptionState {
    val selected = option.id == selectedOptionId
    if (!isAnswerChecked) {
        return if (selected) QuizPlayOxOptionState.Selected else QuizPlayOxOptionState.Default
    }

    val correct = option.matchesAnswerValue(answerValue) == true
    return when {
        correct -> QuizPlayOxOptionState.Correct
        selected -> QuizPlayOxOptionState.Incorrect
        else -> QuizPlayOxOptionState.Default
    }
}

private fun QuizPlayAllQuestion.choiceOptionState(
    option: QuizPlayAllOption,
    selectedOptionId: String?,
    isAnswerChecked: Boolean,
): QuizPlayChoiceOptionState {
    val selected = option.id == selectedOptionId
    if (!isAnswerChecked) {
        return if (selected) QuizPlayChoiceOptionState.Selected else QuizPlayChoiceOptionState.Default
    }

    val correct = option.matchesAnswerValue(answerValue) == true
    return when {
        correct -> QuizPlayChoiceOptionState.Correct
        selected -> QuizPlayChoiceOptionState.Incorrect
        else -> QuizPlayChoiceOptionState.Default
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun QuizPlayAllScreenPreview() {
    QuiketTheme {
        QuizPlayAllScreen(
            state = previewQuizPlayAllState,
            onIntent = {},
            onCloseClick = {},
        )
    }
}

private val previewQuizPlayAllState = QuizPlayAllState(
    questions = listOf(
        QuizPlayAllQuestion(
            id = "preview-1",
            number = 1,
            body = "SQL에서 두 테이블의 공통 컬럼을 기준으로 행을 결합하는 연산은 무엇인가요?",
            timerText = "00:30",
            options = listOf(
                QuizPlayAllOption("preview-1-1", 1, "SELECT"),
                QuizPlayAllOption("preview-1-2", 2, "JOIN"),
                QuizPlayAllOption("preview-1-3", 3, "WHERE"),
                QuizPlayAllOption("preview-1-4", 4, "GROUP BY"),
            ),
        ),
    ),
    selectedOptionIds = mapOf("preview-1" to "preview-1-2"),
    bookmarkedQuestionIds = setOf("preview-1"),
    showTutorial = false,
)
