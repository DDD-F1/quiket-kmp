package com.f1.quiket.composeapp.quiz

import org.koin.compose.koinInject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizOption
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizQuestion
import com.f1.quiket.composeapp.quiz.domain.model.ServerQuizType
import com.f1.quiket.composeapp.quiz.domain.model.defaultOptions
import com.f1.quiket.composeapp.quiz.domain.model.matchesAnswerValue
import com.f1.quiket.composeapp.quiz.presentation.QuizPlayStateHolder
import com.f1.quiket.composeapp.quiz.presentation.QuizPlayUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuizPlayRoute(
    launchConfig: QuizPlayLaunchConfig,
    onBackClick: () -> Unit,
    onResultReady: (String) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<QuizPlayStateHolder>()
    val state = stateHolder.state

    fun loadQuiz() {
        coroutineScope.launch {
            stateHolder.loadQuiz(
                launchConfig = launchConfig,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    fun updateReady(transform: (QuizPlayUiState.Ready) -> QuizPlayUiState.Ready) {
        stateHolder.updateReady(transform)
    }

    LaunchedEffect(launchConfig) {
        loadQuiz()
    }

    val timerReadyState = state as? QuizPlayUiState.Ready
    LaunchedEffect(
        timerReadyState?.quizSession?.id,
        timerReadyState?.currentQuestionIndex,
        timerReadyState?.playMode,
        timerReadyState?.timerEnabled,
        timerReadyState?.timerScope,
        timerReadyState?.timerSeconds,
        timerReadyState?.isCurrentQuestionChecked,
    ) {
        if (timerReadyState?.shouldTickTimer != true) return@LaunchedEffect
        while (true) {
            delay(1_000)
            stateHolder.tickTimer()
        }
    }

    fun submit() {
        coroutineScope.launch {
            stateHolder.submit(onSessionExpired = onSessionExpired)
                ?.let(onResultReady)
        }
    }

    QuizPlayScreen(
        state = state,
        onBackClick = onBackClick,
        onRetryClick = ::loadQuiz,
        onOptionClick = { questionId, optionId ->
            updateReady {
                if (it.isOneByOneMode && it.checkedQuestionIds.contains(questionId)) {
                    return@updateReady it
                }
                it.copy(selectedOptionIds = it.selectedOptionIds + (questionId to optionId))
            }
        },
        onPreviousClick = {
            updateReady {
                it.copy(currentQuestionIndex = (it.currentQuestionIndex - 1).coerceAtLeast(0))
            }
        },
        onNextClick = {
            updateReady {
                it.copy(
                    currentQuestionIndex = (it.currentQuestionIndex + 1)
                        .coerceAtMost((it.questions.size - 1).coerceAtLeast(0)),
                )
            }
        },
        onToggleBookmarkClick = {
            updateReady {
                val questionId = it.currentQuestion?.id ?: return@updateReady it
                val nextBookmarks = if (questionId in it.bookmarkedQuestionIds) {
                    it.bookmarkedQuestionIds - questionId
                } else {
                    it.bookmarkedQuestionIds + questionId
                }
                it.copy(bookmarkedQuestionIds = nextBookmarks)
            }
        },
        onOpenQuestionListClick = {
            updateReady { it.copy(isQuestionListVisible = true) }
        },
        onCloseQuestionListClick = {
            updateReady { it.copy(isQuestionListVisible = false) }
        },
        onQuestionClick = { index ->
            updateReady {
                it.copy(
                    currentQuestionIndex = index.coerceIn(
                        minimumValue = 0,
                        maximumValue = (it.questions.size - 1).coerceAtLeast(0),
                    ),
                    isQuestionListVisible = false,
                )
            }
        },
        onCheckAnswerClick = {
            updateReady {
                val questionId = it.currentQuestion?.id ?: return@updateReady it
                if (it.selectedOptionIds[questionId] == null) {
                    return@updateReady it
                }
                it.copy(checkedQuestionIds = it.checkedQuestionIds + questionId)
            }
        },
        onOpenSubmitConfirmClick = {
            updateReady { it.copy(isSubmitConfirmVisible = true) }
        },
        onCloseSubmitConfirmClick = {
            updateReady { it.copy(isSubmitConfirmVisible = false) }
        },
        onSubmitClick = ::submit,
        modifier = modifier,
    )
}

@Composable
private fun QuizPlayScreen(
    state: QuizPlayUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOptionClick: (questionId: String, optionId: String) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleBookmarkClick: () -> Unit,
    onOpenQuestionListClick: () -> Unit,
    onCloseQuestionListClick: () -> Unit,
    onQuestionClick: (Int) -> Unit,
    onCheckAnswerClick: () -> Unit,
    onOpenSubmitConfirmClick: () -> Unit,
    onCloseSubmitConfirmClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = QuiketWhite,
    ) {
        when (state) {
            QuizPlayUiState.Loading -> QuizPlayMessageScaffold(
                message = "퀴즈를 불러오는 중이에요",
                onBackClick = onBackClick,
            )

            is QuizPlayUiState.Error -> QuizPlayMessageScaffold(
                message = state.message,
                onBackClick = onBackClick,
                actionText = "다시 시도",
                onActionClick = onRetryClick,
            )

            is QuizPlayUiState.Ready -> QuizPlayReadyScreen(
                state = state,
                onBackClick = onBackClick,
                onOptionClick = onOptionClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onToggleBookmarkClick = onToggleBookmarkClick,
                onOpenQuestionListClick = onOpenQuestionListClick,
                onCloseQuestionListClick = onCloseQuestionListClick,
                onQuestionClick = onQuestionClick,
                onCheckAnswerClick = onCheckAnswerClick,
                onOpenSubmitConfirmClick = onOpenSubmitConfirmClick,
                onCloseSubmitConfirmClick = onCloseSubmitConfirmClick,
                onSubmitClick = onSubmitClick,
            )
        }
    }
}

@Composable
private fun QuizPlayReadyScreen(
    state: QuizPlayUiState.Ready,
    onBackClick: () -> Unit,
    onOptionClick: (questionId: String, optionId: String) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleBookmarkClick: () -> Unit,
    onOpenQuestionListClick: () -> Unit,
    onCloseQuestionListClick: () -> Unit,
    onQuestionClick: (Int) -> Unit,
    onCheckAnswerClick: () -> Unit,
    onOpenSubmitConfirmClick: () -> Unit,
    onCloseSubmitConfirmClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = state.currentQuestion

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizPlayTopBar(
                onListClick = onOpenQuestionListClick,
                onCloseClick = onBackClick,
            )

            if (question == null) {
                QuizPlayEmptyContent(
                    message = "문제를 불러올 수 없어요",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                QuizPlayProgress(
                    current = state.currentQuestionIndex + 1,
                    total = state.questions.size,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 28.dp, bottom = 128.dp),
                ) {
                    QuizQuestionHeader(
                        questionNumber = question.displayOrder.coerceAtLeast(state.currentQuestionIndex + 1),
                        timerText = state.timerText,
                        timerWarning = state.timerRemainingSeconds?.let { it <= 10 } == true,
                        bookmarked = state.isCurrentQuestionBookmarked,
                        onBookmarkClick = onToggleBookmarkClick,
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = question.body,
                        color = QuiketGray950,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 22.sp,
                            lineHeight = 31.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val selectedOptionId = state.selectedOptionIds[question.id]
                    if (state.isOneByOneMode && question.questionType == ServerQuizType.Ox) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            question.displayOptions().forEach { option ->
                                QuizOxOptionCard(
                                    option = option,
                                    answerState = option.toDisplayAnswerState(
                                        question = question,
                                        selectedOptionId = selectedOptionId,
                                        revealAnswer = state.isCurrentQuestionChecked,
                                    ),
                                    enabled = !state.isCurrentQuestionChecked,
                                    onClick = { onOptionClick(question.id, option.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            question.displayOptions().forEach { option ->
                                QuizOptionCard(
                                    option = option,
                                    answerState = option.toDisplayAnswerState(
                                        question = question,
                                        selectedOptionId = selectedOptionId,
                                        revealAnswer = state.isCurrentQuestionChecked,
                                    ),
                                    enabled = !(state.isOneByOneMode && state.isCurrentQuestionChecked),
                                    onClick = { onOptionClick(question.id, option.id) },
                                )
                            }
                        }
                    }

                    if (state.isCurrentQuestionChecked) {
                        QuizAnswerExplanationCard(
                            question = question,
                            selectedOptionId = selectedOptionId,
                        )
                    }

                    if (!state.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage,
                            color = QuiketOrange500,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        if (question != null) {
            if (state.isOneByOneMode) {
                QuizPlayOneByOneBottomBar(
                    text = when {
                        state.isSubmitting -> "제출 중..."
                        state.isCurrentQuestionChecked && state.isLastQuestion -> "결과 보기"
                        state.isCurrentQuestionChecked -> "다음"
                        else -> "정답 확인"
                    },
                    enabled = if (state.isCurrentQuestionChecked) {
                        !state.isSubmitting
                    } else {
                        state.selectedOptionId != null && !state.isSubmitting
                    },
                    onClick = {
                        when {
                            !state.isCurrentQuestionChecked -> onCheckAnswerClick()
                            state.isLastQuestion -> onSubmitClick()
                            else -> onNextClick()
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            } else {
                QuizPlayBottomBar(
                    canMovePrevious = state.currentQuestionIndex > 0,
                    canMoveNext = state.currentQuestionIndex < state.questions.lastIndex,
                    isLastQuestion = state.questions.isNotEmpty() &&
                        state.currentQuestionIndex == state.questions.lastIndex,
                    isSubmitting = state.isSubmitting,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onSubmitClick = onOpenSubmitConfirmClick,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        if (state.isQuestionListVisible) {
            QuizQuestionListSheet(
                state = state,
                onDismiss = onCloseQuestionListClick,
                onQuestionClick = onQuestionClick,
            )
        }

        if (state.isSubmitConfirmVisible) {
            QuizSubmitConfirmDialog(
                state = state,
                onContinueClick = onCloseSubmitConfirmClick,
                onSubmitClick = onSubmitClick,
            )
        }
    }
}

@Composable
private fun QuizPlayTopBar(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onListClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(QuiketWhite),
    ) {
        if (onListClick != null) {
            QuizPlayIconButton(
                contentDescription = "문제 목록 열기",
                onClick = onListClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 40.dp),
            ) {
                DrawListIcon(color = QuiketGray700)
            }
        }
        QuizPlayIconButton(
            contentDescription = "퀴즈 닫기",
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 40.dp),
        ) {
            DrawCloseIcon(color = QuiketGray700)
        }
    }
}

@Composable
private fun QuizPlayIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun QuizQuestionHeader(
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
            color = QuiketBrown950,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = questionNumber.toString(),
            color = QuiketBrown950,
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
private fun QuizBookmarkButton(
    bookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (bookmarked) QuizBookmarkBg else QuiketGray50)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = if (bookmarked) "찜 해제" else "찜 표시"
            },
        contentAlignment = Alignment.Center,
    ) {
        DrawBookmarkIcon(
            color = if (bookmarked) QuizBookmarkText else QuiketGray500,
            filled = bookmarked,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun QuizPlayProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val normalizedTotal = total.coerceAtLeast(1)
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
                .background(QuiketGray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((current.toFloat() / normalizedTotal).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(QuiketBrown950),
            )
        }
        Text(
            text = "$current/$total",
            color = QuiketGray900,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun QuizTimerChip(
    text: String,
    warning: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (warning) QuizNegativeBg else QuiketGray50)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = if (warning) QuiketNegative else QuiketGray900,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun QuizOptionCard(
    option: QuizOption,
    answerState: QuizOptionDisplayState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = when (answerState) {
        QuizOptionDisplayState.Default -> null
        QuizOptionDisplayState.Selected -> QuiketBrown950
        QuizOptionDisplayState.Correct -> QuizPositive
        QuizOptionDisplayState.Incorrect -> QuiketNegative
    }
    val backgroundColor = when (answerState) {
        QuizOptionDisplayState.Default -> QuiketGray50
        QuizOptionDisplayState.Selected -> QuiketWhite
        QuizOptionDisplayState.Correct -> QuizPositiveBg
        QuizOptionDisplayState.Incorrect -> QuizNegativeBg
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizChoiceOptionIcon(state = answerState)
        Text(
            text = option.content,
            color = QuiketGray950,
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
private fun QuizOxOptionCard(
    option: QuizOption,
    answerState: QuizOptionDisplayState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = when (answerState) {
        QuizOptionDisplayState.Default -> QuiketGray50
        QuizOptionDisplayState.Selected -> QuiketBrown50
        QuizOptionDisplayState.Correct -> QuizPositiveBg
        QuizOptionDisplayState.Incorrect -> QuizNegativeBg
    }
    val borderColor = when (answerState) {
        QuizOptionDisplayState.Default -> null
        QuizOptionDisplayState.Selected -> QuiketBrown950
        QuizOptionDisplayState.Correct -> QuizPositive
        QuizOptionDisplayState.Incorrect -> QuiketNegative
    }
    val iconBackgroundColor = when (answerState) {
        QuizOptionDisplayState.Default -> QuiketGray100
        QuizOptionDisplayState.Selected -> QuiketBrown50
        QuizOptionDisplayState.Correct -> QuizPositiveBg
        QuizOptionDisplayState.Incorrect -> QuizNegativeBg
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
                color = QuiketBrown950,
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
            color = QuiketGray950,
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
private fun QuizChoiceOptionIcon(
    state: QuizOptionDisplayState,
    modifier: Modifier = Modifier,
) {
    val iconColor = when (state) {
        QuizOptionDisplayState.Default -> QuiketGray100
        QuizOptionDisplayState.Selected -> QuiketBrown950
        QuizOptionDisplayState.Correct -> QuizPositive
        QuizOptionDisplayState.Incorrect -> QuiketNegative
    }

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            QuizOptionDisplayState.Default -> Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(QuiketGray100)
                    .border(1.dp, QuiketGray300, CircleShape),
            )

            QuizOptionDisplayState.Selected,
            QuizOptionDisplayState.Correct,
            -> Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center,
            ) {
                DrawCheckIcon(color = QuiketWhite, modifier = Modifier.size(16.dp))
            }

            QuizOptionDisplayState.Incorrect -> Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center,
            ) {
                DrawSmallCloseIcon(color = QuiketWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuizAnswerExplanationCard(
    question: QuizQuestion,
    selectedOptionId: String?,
    modifier: Modifier = Modifier,
) {
    val selectedCorrect = question.isSelectedAnswerCorrect(selectedOptionId)
    val title = if (selectedCorrect == true) "정답" else "오답"
    val description = if (selectedCorrect == true) {
        question.correctExplanation
    } else {
        question.incorrectExplanation ?: question.correctExplanation
    }.orEmpty().ifBlank {
        if (selectedCorrect == true) {
            "정답이에요."
        } else {
            "선택한 답을 다시 확인해보세요."
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selectedCorrect == true) Color(0xFFE9F7EF) else Color(0xFFFFF1EC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = if (selectedCorrect == true) Color(0xFF1E8E3E) else QuiketOrange500,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = description,
            color = QuiketGray700,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun QuizPlayBottomBar(
    canMovePrevious: Boolean,
    canMoveNext: Boolean,
    isLastQuestion: Boolean,
    isSubmitting: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(QuiketWhite)
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = if (isLastQuestion) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizPlayCircleButton(
            direction = QuizPlayMoveDirection.Previous,
            enabled = canMovePrevious && !isSubmitting,
            onClick = onPreviousClick,
        )

        if (isLastQuestion) {
            QuiketPrimaryButton(
                text = if (isSubmitting) "제출 중..." else "제출하기",
                enabled = !isSubmitting,
                onClick = onSubmitClick,
                modifier = Modifier.weight(1f),
            )
        }

        QuizPlayCircleButton(
            direction = QuizPlayMoveDirection.Next,
            enabled = canMoveNext && !isSubmitting,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun QuizPlayCircleButton(
    direction: QuizPlayMoveDirection,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (enabled) QuiketBrown950 else QuiketGray100
    val iconColor = if (enabled) QuiketWhite else QuiketGray300
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
                    QuizPlayMoveDirection.Previous -> "이전 문제"
                    QuizPlayMoveDirection.Next -> "다음 문제"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 3.dp.toPx()
            val startX = if (direction == QuizPlayMoveDirection.Previous) 0.62f else 0.38f
            val endX = if (direction == QuizPlayMoveDirection.Previous) 0.38f else 0.62f
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
private fun QuizPlayOneByOneBottomBar(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(QuiketWhite)
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
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
private fun QuizPlayMessageScaffold(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        QuizPlayTopBar(
            onCloseClick = onBackClick,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = message,
                    color = QuiketGray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                if (actionText != null && onActionClick != null) {
                    QuiketPrimaryButton(
                        text = actionText,
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizPlayEmptyContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun QuizQuestionListSheet(
    state: QuizPlayUiState.Ready,
    onDismiss: () -> Unit,
    onQuestionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(role = Role.Button, onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(QuiketWhite)
                .clickable(onClick = {})
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 42.dp, height = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(QuiketGray300),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "문제 목록",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "닫기",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
            Text(
                text = "푼 문제 ${state.solvedQuestionCount} | 안 푼 문제 ${state.unsolvedQuestionCount} | 다시 볼 문제 ${state.bookmarkedQuestionCount}",
                color = QuiketGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )

            state.questions.chunked(7).forEachIndexed { rowIndex, rowQuestions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowQuestions.forEachIndexed { columnIndex, question ->
                        val questionIndex = rowIndex * 7 + columnIndex
                        QuizQuestionNumberChip(
                            number = question.displayOrder.coerceAtLeast(questionIndex + 1),
                            selected = questionIndex == state.currentQuestionIndex,
                            solved = question.id in state.selectedOptionIds,
                            bookmarked = question.id in state.bookmarkedQuestionIds,
                            onClick = { onQuestionClick(questionIndex) },
                        )
                    }
                }
            }
            QuizListLegend()
        }
    }
}

@Composable
private fun QuizQuestionNumberChip(
    number: Int,
    selected: Boolean,
    solved: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        selected -> QuiketBrown950
        bookmarked -> Color(0xFFFEF7EE)
        solved -> QuiketBrown50
        else -> QuiketGray100
    }
    val textColor = if (selected) {
        QuiketWhite
    } else if (bookmarked) {
        QuiketOrange500
    } else {
        QuiketGray700
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (bookmarked && !selected) 1.dp else 0.dp,
                color = if (bookmarked && !selected) QuiketOrange500 else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
        if (bookmarked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(QuiketOrange500),
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
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizLegendItem(color = QuiketBrown950, text = "현재")
        QuizLegendItem(color = QuiketBrown50, text = "푼 문제")
        QuizLegendItem(color = QuiketGray100, text = "미완료")
        QuizLegendItem(color = Color(0xFFFEF7EE), text = "다시 볼 문제")
    }
}

@Composable
private fun QuizLegendItem(
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
            color = QuiketGray700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun QuizSubmitConfirmDialog(
    state: QuizPlayUiState.Ready,
    onContinueClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(QuiketWhite)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "제출하기 전에 확인해주세요",
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            QuizSubmitCheckSection(
                title = "안 푼 문제",
                questions = state.unsolvedQuestions,
                accentColor = Color(0xFFEF4444),
                backgroundColor = Color(0xFFFFECEC),
                questionNumber = state::questionNumber,
                bookmarkedQuestionIds = state.bookmarkedQuestionIds,
                showBookmarkOnFirstTag = state.unsolvedQuestions.firstOrNull()
                    ?.let { question -> question.id in state.bookmarkedQuestionIds } == true,
            )
            QuizSubmitCheckSection(
                title = "나중에 다시 볼 문제",
                questions = state.bookmarkedQuestions,
                accentColor = QuiketOrange500,
                backgroundColor = Color(0xFFFEF7EE),
                questionNumber = state::questionNumber,
                bookmarkedQuestionIds = state.bookmarkedQuestionIds,
            )
            Text(
                text = "풀지 않은 문제는 오답으로 처리돼요.\n나중에 다시 보려고 표시한 문제는 제출 후에는 초기화 돼요. 지금 마지막으로 검토해야 해요.",
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuiketPrimaryButton(
                    text = "계속 풀기",
                    onClick = onContinueClick,
                    containerColor = QuiketGray100,
                    contentColor = QuiketGray700,
                    modifier = Modifier.weight(1f),
                )
                QuiketPrimaryButton(
                    text = if (state.isSubmitting) "제출 중..." else "제출하기",
                    enabled = !state.isSubmitting,
                    onClick = onSubmitClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuizSubmitCheckSection(
    title: String,
    questions: List<QuizQuestion>,
    accentColor: Color,
    backgroundColor: Color,
    questionNumber: (QuizQuestion) -> Int,
    bookmarkedQuestionIds: Set<String>,
    modifier: Modifier = Modifier,
    showBookmarkOnFirstTag: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = QuiketGray700,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = questions.size.toString(),
                color = accentColor,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            questions.take(4).forEachIndexed { index, question ->
                QuizSubmitTag(
                    text = "${questionNumber(question)}번",
                    accentColor = accentColor,
                    backgroundColor = backgroundColor,
                    showBookmark = (showBookmarkOnFirstTag && index == 0) ||
                        (title != "안 푼 문제" && question.id in bookmarkedQuestionIds),
                )
            }
        }
    }
}

@Composable
private fun QuizSubmitTag(
    text: String,
    accentColor: Color,
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
            color = accentColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        )
        if (showBookmark) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
        }
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
private fun DrawCheckIcon(
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
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.18f)
            lineTo(size.width * 0.7f, size.height * 0.18f)
            lineTo(size.width * 0.7f, size.height * 0.82f)
            lineTo(size.width * 0.5f, size.height * 0.66f)
            lineTo(size.width * 0.3f, size.height * 0.82f)
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

private val QuizBookmarkBg = Color(0xFFFEF7EE)
private val QuizBookmarkText = Color(0xFFEE7D36)
private val QuizPositive = Color(0xFF1E8E3E)
private val QuizPositiveBg = Color(0xFFE9F7EF)
private val QuizNegativeBg = Color(0xFFFFECEC)

private enum class QuizOptionDisplayState {
    Default,
    Selected,
    Correct,
    Incorrect,
}

private enum class QuizPlayMoveDirection {
    Previous,
    Next,
}

private fun QuizQuestion.displayOptions(): List<QuizOption> =
    options
        .sortedBy { option -> option.optionNumber }
        .ifEmpty { questionType.defaultOptions(id) }

private fun QuizOption.toDisplayAnswerState(
    question: QuizQuestion,
    selectedOptionId: String?,
    revealAnswer: Boolean,
): QuizOptionDisplayState {
    if (!revealAnswer) {
        return if (id == selectedOptionId) {
            QuizOptionDisplayState.Selected
        } else {
            QuizOptionDisplayState.Default
        }
    }
    val correct = matchesAnswerValue(question.answerValue) == true
    val selected = id == selectedOptionId
    return when {
        correct -> QuizOptionDisplayState.Correct
        selected -> QuizOptionDisplayState.Incorrect
        else -> QuizOptionDisplayState.Default
    }
}

private fun QuizOption.oxSymbol(): String =
    when {
        (value ?: content).equals("O", ignoreCase = true) -> "O"
        (value ?: content).equals("X", ignoreCase = true) -> "X"
        content == "그렇다" || content == "맞다" -> "O"
        content == "아니다" || content == "틀리다" -> "X"
        optionNumber == 1 -> "O"
        optionNumber == 2 -> "X"
        else -> content.take(1)
    }

private fun QuizOption.oxLabel(): String =
    when (oxSymbol()) {
        "O" -> "그렇다"
        "X" -> "아니다"
        else -> content
    }

private fun QuizQuestion.isSelectedAnswerCorrect(selectedOptionId: String?): Boolean? =
    displayOptions().firstOrNull { option -> option.id == selectedOptionId }
        ?.matchesAnswerValue(answerValue)
