package com.f1.quiket.composeapp.result

import org.koin.compose.koinInject
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray900
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayLaunchConfig
import com.f1.quiket.composeapp.quiz.domain.model.QuizOption
import com.f1.quiket.composeapp.quiz.domain.model.matchesAnswerValue
import com.f1.quiket.composeapp.result.domain.model.QuestionOption
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.domain.model.QuizReviewItem
import com.f1.quiket.composeapp.result.domain.model.ResultPartSummary
import com.f1.quiket.composeapp.result.presentation.QuizResultStateHolder
import com.f1.quiket.composeapp.result.presentation.QuizResultUiState
import kotlinx.coroutines.launch

@Composable
internal fun QuizResultRoute(
    resultId: String,
    onBackClick: () -> Unit,
    onRetryReady: (QuizPlayLaunchConfig) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val stateHolder = koinInject<QuizResultStateHolder>()
    val state = stateHolder.state
    val isRetrying = stateHolder.isRetrying
    val retryMessage = stateHolder.retryMessage

    LaunchedEffect(resultId) {
        stateHolder.loadResult(
            resultId = resultId,
            onSessionExpired = onSessionExpired,
        )
    }

    fun retry(wrongOnly: Boolean) {
        coroutineScope.launch {
            stateHolder.retry(
                wrongOnly = wrongOnly,
                onSessionExpired = onSessionExpired,
            )?.let(onRetryReady)
        }
    }

    QuizResultScreen(
        state = state,
        isRetrying = isRetrying,
        retryMessage = retryMessage,
        onBackClick = onBackClick,
        onRetryAllClick = { retry(wrongOnly = false) },
        onRetryWrongClick = { retry(wrongOnly = true) },
        modifier = modifier,
    )
}

@Composable
private fun QuizResultScreen(
    state: QuizResultUiState,
    isRetrying: Boolean,
    retryMessage: String?,
    onBackClick: () -> Unit,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reviewMode by remember((state as? QuizResultUiState.Success)?.result?.resultId) {
        mutableStateOf(QuizResultReviewMode.Summary)
    }
    var selectedReviewIndex by remember((state as? QuizResultUiState.Success)?.result?.resultId) {
        mutableStateOf(0)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = QuiketWhite,
    ) {
        when (state) {
            QuizResultUiState.Loading -> QuizResultMessageScaffold(
                text = "결과를 불러오는 중이에요",
                onBackClick = onBackClick,
            )

            is QuizResultUiState.Error -> QuizResultMessageScaffold(
                text = state.message,
                onBackClick = onBackClick,
            )

            is QuizResultUiState.Success -> {
                val allReviewItems = state.result.reviewItems.sortedBy { it.displayOrder }
                val currentReviewItems = when (reviewMode) {
                    QuizResultReviewMode.WrongList,
                    QuizResultReviewMode.WrongDetail,
                    -> allReviewItems.filterNot { it.correctServer }

                    QuizResultReviewMode.Summary,
                    QuizResultReviewMode.AllList,
                    QuizResultReviewMode.AllDetail,
                    -> allReviewItems
                }
                val detailIndex = selectedReviewIndex.coerceIn(
                    minimumValue = 0,
                    maximumValue = currentReviewItems.lastIndex.coerceAtLeast(0),
                )

                when (reviewMode) {
                    QuizResultReviewMode.Summary -> QuizResultSummary(
                        result = state.result,
                        isRetrying = isRetrying,
                        retryMessage = retryMessage,
                        onBackClick = onBackClick,
                        onAllReviewClick = {
                            selectedReviewIndex = 0
                            reviewMode = QuizResultReviewMode.AllList
                        },
                        onWrongReviewClick = {
                            selectedReviewIndex = 0
                            reviewMode = QuizResultReviewMode.WrongList
                        },
                        onRetryAllClick = onRetryAllClick,
                        onRetryWrongClick = onRetryWrongClick,
                    )

                    QuizResultReviewMode.AllList -> QuizResultReviewList(
                        title = "전체 문제 보기",
                        result = state.result,
                        reviewItems = currentReviewItems,
                        onBackClick = { reviewMode = QuizResultReviewMode.Summary },
                        onItemClick = { index ->
                            selectedReviewIndex = index
                            reviewMode = QuizResultReviewMode.AllDetail
                        },
                    )

                    QuizResultReviewMode.WrongList -> QuizResultReviewList(
                        title = "오답만 보기",
                        result = state.result,
                        reviewItems = currentReviewItems,
                        onBackClick = { reviewMode = QuizResultReviewMode.Summary },
                        onItemClick = { index ->
                            selectedReviewIndex = index
                            reviewMode = QuizResultReviewMode.WrongDetail
                        },
                    )

                    QuizResultReviewMode.AllDetail,
                    QuizResultReviewMode.WrongDetail,
                    -> {
                        val reviewItem = currentReviewItems.getOrNull(detailIndex)
                        if (reviewItem == null) {
                            QuizResultReviewList(
                                title = "전체 문제 보기",
                                result = state.result,
                                reviewItems = currentReviewItems,
                                onBackClick = { reviewMode = QuizResultReviewMode.Summary },
                                onItemClick = {},
                            )
                        } else {
                            QuizResultReviewDetail(
                                reviewItem = reviewItem,
                                currentIndex = detailIndex,
                                totalCount = currentReviewItems.size,
                                onCloseClick = {
                                    reviewMode = if (reviewMode == QuizResultReviewMode.AllDetail) {
                                        QuizResultReviewMode.AllList
                                    } else {
                                        QuizResultReviewMode.WrongList
                                    }
                                },
                                onPreviousClick = {
                                    selectedReviewIndex = (detailIndex - 1).coerceAtLeast(0)
                                },
                                onNextClick = {
                                    selectedReviewIndex = (detailIndex + 1)
                                        .coerceAtMost(currentReviewItems.lastIndex)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultSummary(
    result: QuizResult,
    isRetrying: Boolean,
    retryMessage: String?,
    onBackClick: () -> Unit,
    onAllReviewClick: () -> Unit,
    onWrongReviewClick: () -> Unit,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizResultHero(
                result = result,
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                QuizResultReviewSection(
                    result = result,
                    onAllReviewClick = onAllReviewClick,
                    onWrongReviewClick = onWrongReviewClick,
                )
                QuizResultRetrySection(
                    result = result,
                    isRetrying = isRetrying,
                    message = retryMessage,
                    onRetryAllClick = onRetryAllClick,
                    onRetryWrongClick = onRetryWrongClick,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(92.dp)
                .background(QuiketWhite)
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            QuiketPrimaryButton(
                text = "홈으로",
                enabled = true,
                onClick = onBackClick,
            )
        }
    }
}

@Composable
private fun QuizResultHero(
    result: QuizResult,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(QuiketBrown50),
    ) {
        QuizResultTopBar(
            title = "퀴즈 결과",
            trailingText = "×",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(QuiketGray50),
                    contentAlignment = Alignment.Center,
                ) {
                    DrawCmpQuizResultIcon(color = QuiketBrown950)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = result.subjectName?.takeIf { it.isNotBlank() } ?: "퀴즈",
                        color = QuiketGray950,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = "총 ${result.elapsedText()} 소요",
                        color = QuiketGray700,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        result.scopeLabels().take(2).forEach { label ->
                            QuizResultChip(text = label, background = QuiketGray100, contentColor = QuiketGray700)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(QuiketGray300),
            )

            Text(
                text = result.encouragementText(),
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )

            QuizResultScoreCard(result = result)
        }
    }
}

@Composable
private fun QuizResultScoreCard(
    result: QuizResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(QuiketWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "${result.accuracyPct}%",
                color = QuiketBrown950,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${result.totalCount}문제 중 ${result.correctCount}문제 정답",
                color = QuiketGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(QuiketGray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((result.accuracyPct / 100f).coerceIn(0f, 1f))
                    .background(QuiketBrown950),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizResultChip(
                text = "${result.totalCount}문제",
                background = Color(0xFFFFF1C7),
                contentColor = QuiketOrange500,
            )
            QuizResultChip(
                text = "정답 ${result.correctCount}개",
                background = Color(0xFFFFF1C7),
                contentColor = QuiketOrange500,
            )
        }
    }
}

@Composable
private fun QuizResultReviewSection(
    result: QuizResult,
    onAllReviewClick: () -> Unit,
    onWrongReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizResultSectionTitle(text = "풀이 보기")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuizResultSelectBox(
                title = "전체 문제 보기",
                body = "${result.totalCount}문제",
                onClick = onAllReviewClick,
                modifier = Modifier.weight(1f),
            )
            QuizResultSelectBox(
                title = "오답만 보기",
                body = "${result.incorrectReviewCount}문제",
                onClick = onWrongReviewClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuizResultReviewList(
    title: String,
    result: QuizResult,
    reviewItems: List<QuizReviewItem>,
    onBackClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        QuizResultTopBar(
            title = title,
            leadingText = "‹",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "총 ${reviewItems.size}문제",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                QuizResultCountChip(
                    count = reviewItems.count { it.correctServer },
                    positive = true,
                )
                QuizResultCountChip(
                    count = reviewItems.count { !it.correctServer },
                    positive = false,
                )
            }

            if (reviewItems.isEmpty()) {
                Text(
                    text = "표시할 문제가 없어요",
                    color = QuiketGray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reviewItems.forEachIndexed { index, item ->
                        QuizResultReviewItem(
                            item = item,
                            onClick = { onItemClick(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultReviewItem(
    item: QuizReviewItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (item.correctServer) QuiketGray50 else Color(0xFFFFE9E9))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Q ${item.displayOrder}.",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = item.reviewTitle(),
            color = QuiketGray950,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (item.correctServer) Positive else QuiketNegative),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (item.correctServer) "✓" else "×",
                color = QuiketWhite,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun QuizResultReviewDetail(
    reviewItem: QuizReviewItem,
    currentIndex: Int,
    totalCount: Int,
    onCloseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizResultReviewDetailTopBar(onCloseClick = onCloseClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Q ${reviewItem.displayOrder}",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = reviewItem.body,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reviewItem.options.sortedBy { it.optionNumber }.forEach { option ->
                        QuizResultReviewOptionRow(
                            option = option,
                            state = reviewItem.optionState(option),
                        )
                    }
                }

                QuizResultReviewExplanationCard(reviewItem = reviewItem)
            }
        }

        QuizResultReviewDetailBottomBar(
            current = currentIndex + 1,
            total = totalCount,
            canMovePrevious = currentIndex > 0,
            canMoveNext = currentIndex < totalCount - 1,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun QuizResultReviewDetailTopBar(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(QuiketWhite),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 32.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onCloseClick,
                )
                .semantics { contentDescription = "닫기" },
            contentAlignment = Alignment.Center,
        ) {
            DrawCmpQuizResultCloseIcon(color = QuiketGray700)
        }
    }
}

@Composable
private fun QuizResultReviewOptionRow(
    option: QuestionOption,
    state: QuizResultReviewOptionState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = when (state) {
        QuizResultReviewOptionState.Correct -> PositiveBg
        QuizResultReviewOptionState.Incorrect -> NegativeBg
        QuizResultReviewOptionState.Default -> QuiketGray50
    }
    val borderColor = when (state) {
        QuizResultReviewOptionState.Correct -> Positive
        QuizResultReviewOptionState.Incorrect -> QuiketNegative
        QuizResultReviewOptionState.Default -> null
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
            .clickable(enabled = false, role = Role.RadioButton, onClick = {})
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizResultChoiceOptionIcon(state = state)
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
private fun QuizResultChoiceOptionIcon(
    state: QuizResultReviewOptionState,
    modifier: Modifier = Modifier,
) {
    val iconColor = when (state) {
        QuizResultReviewOptionState.Default -> QuiketGray100
        QuizResultReviewOptionState.Correct -> Positive
        QuizResultReviewOptionState.Incorrect -> QuiketNegative
    }

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            QuizResultReviewOptionState.Default -> Box(
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(QuiketGray100)
                    .border(1.dp, QuiketGray300, CircleShape),
            )

            QuizResultReviewOptionState.Correct -> Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center,
            ) {
                DrawCmpQuizResultCheckIcon(color = QuiketWhite, modifier = Modifier.size(16.dp))
            }

            QuizResultReviewOptionState.Incorrect -> Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center,
            ) {
                DrawCmpQuizResultCloseIcon(color = QuiketWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuizResultReviewExplanationCard(
    reviewItem: QuizReviewItem,
    modifier: Modifier = Modifier,
) {
    val correctOption = reviewItem.options.firstOrNull { it.matchesAnswerValue(reviewItem.answerValue) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketGray50)
            .border(1.dp, QuiketGray300, RoundedCornerShape(12.dp))
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
                    color = QuiketWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                )
            }
            Text(
                text = correctOption?.content.orEmpty().ifBlank { "정답 정보 없음" },
                color = QuiketGray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = reviewItem.correctExplanation.orEmpty().ifBlank { "정답 해설이 준비되지 않았어요." },
            color = QuiketGray900,
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
                .background(QuiketGray300),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오답 해설 보기",
                color = QuiketGray900,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            DrawCmpQuizResultChevronUpIcon(color = QuiketGray700, modifier = Modifier.size(16.dp))
        }
        Text(
            text = reviewItem.incorrectExplanation.orEmpty().ifBlank { "오답 해설이 준비되지 않았어요." },
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
private fun QuizResultReviewDetailBottomBar(
    current: Int,
    total: Int,
    canMovePrevious: Boolean,
    canMoveNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(QuiketWhite)
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizResultMoveButton(
            text = "‹",
            enabled = canMovePrevious,
            onClick = onPreviousClick,
        )
        Box(
            modifier = Modifier
                .height(29.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(QuiketBrown50)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "문제 $current/$total",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
        }
        QuizResultMoveButton(
            text = "›",
            enabled = canMoveNext,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun QuizResultMoveButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPrevious = text == "‹"
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
            .semantics { contentDescription = if (isPrevious) "이전 문제" else "다음 문제" },
        contentAlignment = Alignment.Center,
    ) {
        DrawCmpQuizResultMoveIcon(
            isPrevious = isPrevious,
            color = iconColor,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun QuizResultRetrySection(
    result: QuizResult,
    isRetrying: Boolean,
    message: String?,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val retryWrongCount = result.retryAvailable?.wrongCount ?: result.wrongCount
    val retryAllEnabled = result.totalCount > 0 && !isRetrying
    val retryWrongEnabled = retryWrongCount > 0 && !isRetrying
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizResultSectionTitle(text = "다시 풀기")
        QuizResultActionRow(
            title = "전체 다시 풀기",
            enabled = retryAllEnabled,
            onClick = onRetryAllClick,
        )
        QuizResultActionRow(
            title = "틀린 문제만 다시 풀기",
            enabled = retryWrongEnabled,
            onClick = onRetryWrongClick,
        )
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                color = if (isRetrying) QuiketGray600 else QuiketOrange500,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun QuizResultTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingText: String? = null,
    trailingText: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
    ) {
        if (leadingText != null) {
            QuizResultTopBarButton(
                icon = QuizResultTopBarIcon.Back,
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 50.dp),
            )
        }
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
        if (trailingText != null) {
            QuizResultTopBarButton(
                icon = QuizResultTopBarIcon.Close,
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 50.dp),
            )
        }
    }
}

@Composable
private fun QuizResultTopBarButton(
    icon: QuizResultTopBarIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = icon.contentDescription },
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = QuiketGray700,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = QuiketGray700,
        ),
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val strokeWidth = 4.dp.toPx()
            when (icon) {
                QuizResultTopBarIcon.Back -> {
                    drawLine(
                        color = QuiketGray700,
                        start = Offset(size.width * 0.62f, size.height * 0.18f),
                        end = Offset(size.width * 0.32f, size.height * 0.5f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = QuiketGray700,
                        start = Offset(size.width * 0.32f, size.height * 0.5f),
                        end = Offset(size.width * 0.62f, size.height * 0.82f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }

                QuizResultTopBarIcon.Close -> {
                    drawLine(
                        color = QuiketGray700,
                        start = Offset(size.width * 0.28f, size.height * 0.28f),
                        end = Offset(size.width * 0.72f, size.height * 0.72f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = QuiketGray700,
                        start = Offset(size.width * 0.72f, size.height * 0.28f),
                        end = Offset(size.width * 0.28f, size.height * 0.72f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultMessageScaffold(
    text: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        QuizResultTopBar(
            title = "퀴즈 결과",
            trailingText = "×",
            onBackClick = onBackClick,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = QuiketGray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun QuizResultSelectBox(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketGray50)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = body,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun QuizResultActionRow(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketGray50)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = if (enabled) QuiketGray950 else QuiketGray600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "→",
            color = if (enabled) QuiketBrown950 else QuiketGray600,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun QuizResultSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
    )
}

@Composable
private fun QuizResultChip(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun QuizResultCountChip(
    count: Int,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (positive) Color(0xFFE9F8EF) else Color(0xFFFFE9E9))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${if (positive) "✓" else "×"} $count",
            color = QuiketGray950,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun DrawCmpQuizResultIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        val left = size.width * 0.28f
        val top = size.height * 0.18f
        val right = size.width * 0.72f
        val bottom = size.height * 0.82f

        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth),
        )
        listOf(0.34f, 0.5f, 0.66f).forEach { yFraction ->
            drawCircle(
                color = color,
                radius = 1.4.dp.toPx(),
                center = Offset(size.width * 0.38f, size.height * yFraction),
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.48f, size.height * yFraction),
                end = Offset(size.width * 0.64f, size.height * yFraction),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun DrawCmpQuizResultCheckIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
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
            end = Offset(size.width * 0.78f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawCmpQuizResultCloseIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.28f),
            end = Offset(size.width * 0.72f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.28f),
            end = Offset(size.width * 0.28f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawCmpQuizResultChevronUpIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.62f),
            end = Offset(size.width * 0.5f, size.height * 0.32f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.32f),
            end = Offset(size.width * 0.82f, size.height * 0.62f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawCmpQuizResultMoveIcon(
    isPrevious: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val startX = if (isPrevious) 0.62f else 0.38f
        val endX = if (isPrevious) 0.38f else 0.62f
        drawLine(
            color = color,
            start = Offset(size.width * startX, size.height * 0.2f),
            end = Offset(size.width * endX, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * endX, size.height * 0.5f),
            end = Offset(size.width * startX, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private val QuizResult.incorrectReviewCount: Int
    get() = if (reviewItems.isNotEmpty()) {
        reviewItems.count { !it.correctServer }
    } else {
        wrongCount + skipCount
    }

private enum class QuizResultReviewMode {
    Summary,
    AllList,
    WrongList,
    AllDetail,
    WrongDetail,
}

private enum class QuizResultReviewOptionState {
    Default,
    Correct,
    Incorrect,
}

private enum class QuizResultTopBarIcon(
    val contentDescription: String,
) {
    Back(contentDescription = "뒤로가기"),
    Close(contentDescription = "닫기"),
}

private val Positive = Color(0xFF22C55E)
private val PositiveBg = Color(0x3322C55E)
private val NegativeBg = Color(0x33EF4444)

private fun QuizResult.encouragementText(): String =
    when {
        accuracyPct <= 20 -> "한 번 더 도전해볼까요?"
        accuracyPct <= 40 -> "조금씩 감을 잡는 중이에요"
        accuracyPct <= 60 -> "조금만 더 하면 완벽해요!"
        accuracyPct <= 80 -> "너무 잘했어요!"
        else -> "훌륭해요, 완전히 이해했네요!"
    }

private fun QuizResult.elapsedText(): String {
    val totalSeconds = (elapsedMs / 1_000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> "${minutes}분 ${seconds}초"
        minutes > 0 -> "${minutes}분"
        else -> "${seconds}초"
    }
}

private fun QuizResult.scopeLabels(): List<String> {
    val groupedParts = reviewItems
        .mapNotNull { it.sourcePart }
        .distinctBy { it.id }
        .groupBy { it.chapterId }

    if (groupedParts.isEmpty()) return emptyList()

    return groupedParts.entries
        .sortedBy { it.key }
        .mapIndexed { index, entry ->
            "챕터 ${index + 1} / ${entry.value.partText()}"
        }
}

private fun List<ResultPartSummary>.partText(): String {
    val partNumbers = map { it.partNumber }
        .distinct()
        .sorted()

    return if (partNumbers.isEmpty()) {
        "전체"
    } else {
        "파트 ${partNumbers.joinToString(",")}"
    }
}

private fun QuizReviewItem.reviewTitle(): String =
    summary?.takeIf { it.isNotBlank() } ?: body

private fun QuizReviewItem.optionState(option: QuestionOption): QuizResultReviewOptionState {
    val correct = option.matchesAnswerValue(answerValue)
    val selected = option.id == selectedOptionId || option.matchesAnswerValue(selectedValue)

    return when {
        correct -> QuizResultReviewOptionState.Correct
        selected -> QuizResultReviewOptionState.Incorrect
        else -> QuizResultReviewOptionState.Default
    }
}

private fun QuestionOption.matchesAnswerValue(answerValue: String?): Boolean =
    QuizOption(
        id = id,
        optionNumber = optionNumber,
        content = content,
        value = content,
    ).matchesAnswerValue(answerValue) == true
