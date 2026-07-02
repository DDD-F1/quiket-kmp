package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray200
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.NegativeBg
import com.f1.quiket.core.designsystem.theme.Positive
import com.f1.quiket.core.designsystem.theme.PositiveBg
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.core.designsystem.theme.Yellow100
import com.f1.quiket.core.designsystem.theme.Yellow500
import com.f1.quiket.feature.home.domain.model.PartSummary
import com.f1.quiket.feature.home.domain.model.QuestionOption
import com.f1.quiket.feature.home.domain.model.QuizResult
import com.f1.quiket.feature.home.domain.model.QuizReviewItem
import com.f1.quiket.feature.home.domain.model.RewardSummary
import com.f1.quiket.feature.home.domain.model.ServerQuizType

@Composable
fun QuizResultRoute(
    resultId: String,
    onBackClick: () -> Unit,
    onRetryReady: (QuizRetryPlayConfig) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizResultViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(resultId) {
        viewModel.onIntent(QuizResultIntent.Load(resultId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuizResultEffect.NavigateToRetry -> onRetryReady(effect.config)
                is QuizResultEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    QuizResultScreen(
        state = state,
        onBackClick = onBackClick,
        onRetryAllClick = { viewModel.onIntent(QuizResultIntent.RetryAll) },
        onRetryWrongClick = { viewModel.onIntent(QuizResultIntent.RetryWrong) },
        modifier = modifier,
    )
}

@Composable
fun QuizResultScreen(
    state: QuizResultState,
    onBackClick: () -> Unit,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reviewMode by remember(state.result?.resultId) {
        mutableStateOf(QuizResultReviewMode.Summary)
    }
    var selectedReviewIndex by remember(state.result?.resultId) {
        mutableStateOf(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
    ) {
        when {
            state.isLoading -> QuizResultMessageScaffold(
                text = "결과를 불러오는 중이에요",
                onBackClick = onBackClick,
            )

            !state.errorMessage.isNullOrBlank() -> QuizResultMessageScaffold(
                text = state.errorMessage,
                onBackClick = onBackClick,
            )

            state.result != null -> QuizResultContent(
                result = state.result,
                onBackClick = onBackClick,
                onRetryAllClick = onRetryAllClick,
                onRetryWrongClick = onRetryWrongClick,
                reviewMode = reviewMode,
                selectedReviewIndex = selectedReviewIndex,
                onReviewModeChange = { mode -> reviewMode = mode },
                onReviewIndexChange = { index -> selectedReviewIndex = index },
            )

            else -> QuizResultMessageScaffold(
                text = "결과를 불러오는 중이에요",
                onBackClick = onBackClick,
            )
        }
    }
}

@Composable
private fun QuizResultContent(
    result: QuizResult,
    onBackClick: () -> Unit,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    reviewMode: QuizResultReviewMode,
    selectedReviewIndex: Int,
    onReviewModeChange: (QuizResultReviewMode) -> Unit,
    onReviewIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allReviewItems = result.reviewItems.sortedBy { item -> item.displayOrder }
    val currentReviewItems = when (reviewMode) {
        QuizResultReviewMode.WrongList,
        QuizResultReviewMode.WrongDetail -> allReviewItems.filterNot { item -> item.correctServer }
        QuizResultReviewMode.Summary,
        QuizResultReviewMode.AllList,
        QuizResultReviewMode.AllDetail -> allReviewItems
    }
    val detailIndex = selectedReviewIndex.coerceIn(
        minimumValue = 0,
        maximumValue = (currentReviewItems.lastIndex).coerceAtLeast(0),
    )

    BackHandler(enabled = reviewMode != QuizResultReviewMode.Summary) {
        when (reviewMode) {
            QuizResultReviewMode.AllList,
            QuizResultReviewMode.WrongList,
            -> onReviewModeChange(QuizResultReviewMode.Summary)
            QuizResultReviewMode.AllDetail -> onReviewModeChange(QuizResultReviewMode.AllList)
            QuizResultReviewMode.WrongDetail -> onReviewModeChange(QuizResultReviewMode.WrongList)
            QuizResultReviewMode.Summary -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (reviewMode) {
            QuizResultReviewMode.Summary -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    QuizResultHero(
                        result = result,
                        onCloseClick = onBackClick,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 116.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        QuizResultReviewSection(
                            result = result,
                            onAllReviewClick = {
                                onReviewIndexChange(0)
                                onReviewModeChange(QuizResultReviewMode.AllList)
                            },
                            onWrongReviewClick = {
                                onReviewIndexChange(0)
                                onReviewModeChange(QuizResultReviewMode.WrongList)
                            },
                        )
                        QuizResultRetrySection(
                            result = result,
                            onRetryAllClick = onRetryAllClick,
                            onRetryWrongClick = onRetryWrongClick,
                        )
                    }
                }

                QuizResultBottomBar(
                    onHomeClick = onBackClick,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            QuizResultReviewMode.AllList,
            QuizResultReviewMode.WrongList -> {
                QuizResultReviewListScreen(
                    title = if (reviewMode == QuizResultReviewMode.AllList) {
                        "전체 문제 보기"
                    } else {
                        "오답만 보기"
                    },
                    result = result,
                    reviewItems = currentReviewItems,
                    onBackClick = { onReviewModeChange(QuizResultReviewMode.Summary) },
                    onItemClick = { index ->
                        onReviewIndexChange(index)
                        onReviewModeChange(
                            if (reviewMode == QuizResultReviewMode.AllList) {
                                QuizResultReviewMode.AllDetail
                            } else {
                                QuizResultReviewMode.WrongDetail
                            },
                        )
                    },
                )
            }

            QuizResultReviewMode.AllDetail,
            QuizResultReviewMode.WrongDetail -> {
                val reviewItem = currentReviewItems.getOrNull(detailIndex)
                if (reviewItem == null) {
                    QuizResultReviewListScreen(
                        title = "전체 문제 보기",
                        result = result,
                        reviewItems = currentReviewItems,
                        onBackClick = { onReviewModeChange(QuizResultReviewMode.Summary) },
                        onItemClick = {},
                    )
                } else {
                    QuizResultReviewDetailScreen(
                        reviewItem = reviewItem,
                        currentIndex = detailIndex,
                        totalCount = currentReviewItems.size,
                        onCloseClick = {
                            onReviewModeChange(
                                if (reviewMode == QuizResultReviewMode.AllDetail) {
                                    QuizResultReviewMode.AllList
                                } else {
                                    QuizResultReviewMode.WrongList
                                },
                            )
                        },
                        onPreviousClick = { onReviewIndexChange((detailIndex - 1).coerceAtLeast(0)) },
                        onNextClick = {
                            onReviewIndexChange((detailIndex + 1).coerceAtMost(currentReviewItems.lastIndex))
                        },
                    )
                }
            }
        }
    }
}

private enum class QuizResultReviewMode {
    Summary,
    AllList,
    WrongList,
    AllDetail,
    WrongDetail,
}

@Composable
private fun QuizResultHero(
    result: QuizResult,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(Brown50),
    ) {
        QuizResultTopBar(onCloseClick = onCloseClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuizResultExamProfile(result = result)

            Text(
                text = result.encouragementText(),
                color = Gray950,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 24.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            QuizResultScoreSummaryCard(result = result)
        }
    }
}

@Composable
private fun QuizResultTopBar(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Brown50),
    ) {
        Text(
            text = "퀴즈 결과",
            color = Gray950,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 32.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onCloseClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            DrawResultCloseIcon(color = Gray700)
        }
    }
}

@Composable
private fun QuizResultExamProfile(
    result: QuizResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Gray50),
                contentAlignment = Alignment.Center,
            ) {
                DrawQuizResultIcon(color = Brown950)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = result.subjectName ?: "퀴즈",
                    color = Gray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "총 ${result.elapsedText()} 소요",
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    result.scopeLabels().take(2).forEach { label ->
                        QuizResultScopeChip(text = label)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Gray200),
        )
    }
}

@Composable
private fun QuizResultScoreSummaryCard(
    result: QuizResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "${result.accuracyPct}%",
                color = Brown950,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 50.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${result.totalCount}문제 중 ${result.correctCount}문제 정답",
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        QuizResultProgressBar(progress = result.accuracyPct / 100f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuizResultTag(text = "${result.totalCount}문제")
            QuizResultTag(text = "정답 ${result.correctCount}개")
        }
    }
}

@Composable
private fun QuizResultProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
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
                body = "${result.wrongCount}문제",
                onClick = onWrongReviewClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuizResultReviewListScreen(
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
            .background(White),
    ) {
        QuizResultReviewTopBar(
            title = title,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "총 ${reviewItems.size}문제",
                    color = Gray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.weight(1f),
                )
                QuizResultReviewCountChip(
                    count = result.correctCount,
                    positive = true,
                )
                QuizResultReviewCountChip(
                    count = result.wrongCount,
                    positive = false,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (reviewItems.isEmpty()) {
                    Text(
                        text = "표시할 문제가 없어요",
                        color = Gray700,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                    )
                } else {
                    reviewItems.forEachIndexed { index, item ->
                        QuizResultReviewListItem(
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
private fun QuizResultReviewTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(White),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 32.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onBackClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            DrawResultChevronLeftIcon(color = Gray700)
        }
        Text(
            text = title,
            color = Gray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp),
        )
    }
}

@Composable
private fun QuizResultReviewCountChip(
    count: Int,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(29.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (positive) PositiveBg else NegativeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (positive) {
            DrawResultCheckIcon(color = Positive, modifier = Modifier.size(16.dp))
        } else {
            DrawResultSmallCloseIcon(color = Negative, modifier = Modifier.size(16.dp))
        }
        Text(
            text = count.toString(),
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
private fun QuizResultReviewListItem(
    item: QuizReviewItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val correct = item.correctServer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (correct) Gray50 else NegativeBg)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Q ${item.displayOrder}.",
                color = Gray950,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text = item.reviewTitle(),
                color = Gray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        QuizResultAnswerStateIcon(correct = correct)
    }
}

@Composable
private fun QuizResultAnswerStateIcon(
    correct: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (correct) Positive else Negative),
        contentAlignment = Alignment.Center,
    ) {
        if (correct) {
            DrawResultCheckIcon(color = White, modifier = Modifier.size(16.dp))
        } else {
            DrawResultSmallCloseIcon(color = White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun QuizResultReviewDetailScreen(
    reviewItem: QuizReviewItem,
    currentIndex: Int,
    totalCount: Int,
    onCloseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = reviewItem.toReviewQuestion()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
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
                    color = Gray950,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = reviewItem.body,
                    color = Gray950,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    question.options.forEach { option ->
                        QuizPlayChoiceOptionButton(
                            option = option,
                            state = reviewItem.optionState(option),
                            enabled = false,
                            onClick = {},
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
            .background(White),
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
                ),
            contentAlignment = Alignment.Center,
        ) {
            DrawResultCloseIcon(color = Gray700)
        }
    }
}

@Composable
private fun QuizResultReviewExplanationCard(
    reviewItem: QuizReviewItem,
    modifier: Modifier = Modifier,
) {
    val correctOption = reviewItem.options.firstOrNull { option ->
        option.matchesAnswerValue(reviewItem.answerValue)
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
                text = correctOption?.content.orEmpty(),
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
            text = reviewItem.correctExplanation.orEmpty().ifBlank { "정답 해설이 준비되지 않았어요." },
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오답 해설 보기",
                color = Gray900,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            DrawResultChevronUpIcon(color = Gray700, modifier = Modifier.size(16.dp))
        }

        Text(
            text = reviewItem.incorrectExplanation.orEmpty().ifBlank { "오답 해설이 준비되지 않았어요." },
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
            .background(White)
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuizPlayAllCircleButton(
            direction = QuizPlayAllMoveDirection.Previous,
            enabled = canMovePrevious,
            onClick = onPreviousClick,
        )
        Box(
            modifier = Modifier
                .height(29.dp)
                .clip(RoundedCornerShape(1000.dp))
                .background(Brown50)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "문제 $current/$total",
                color = Gray950,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
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
private fun QuizResultRetrySection(
    result: QuizResult,
    onRetryAllClick: () -> Unit,
    onRetryWrongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val retryWrongCount = result.retryAvailable?.wrongCount ?: result.wrongCount
    val retryAllEnabled = result.totalCount > 0
    val retryWrongEnabled = retryWrongCount > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuizResultSectionTitle(text = "다시 풀기")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
        }
    }
}

@Composable
private fun QuizResultSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = Gray950,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold,
        ),
        modifier = modifier.fillMaxWidth(),
    )
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
            .height(69.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray50)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = Gray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = body,
            color = Gray600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizResultActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor = if (enabled) Gray950 else Gray600
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Gray50)
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
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.weight(1f),
        )
        DrawResultArrowRightIcon(color = if (enabled) Brown950 else Gray600)
    }
}

@Composable
private fun QuizResultScopeChip(
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

@Composable
private fun QuizResultTag(
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
private fun QuizResultBottomBar(
    onHomeClick: () -> Unit,
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
            text = "홈으로",
            onClick = onHomeClick,
        )
    }
}

@Composable
private fun QuizResultMessageScaffold(
    text: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        QuizResultTopBar(onCloseClick = onBackClick)
        QuizResultMessage(
            text = text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuizResultMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Gray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun DrawResultCloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.4.dp.toPx()
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
private fun DrawResultChevronLeftIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.8.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.65f, size.height * 0.18f),
            end = Offset(size.width * 0.35f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.5f),
            end = Offset(size.width * 0.65f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawResultChevronUpIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.62f),
            end = Offset(size.width * 0.5f, size.height * 0.34f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.34f),
            end = Offset(size.width * 0.82f, size.height * 0.62f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawResultCheckIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.52f),
            end = Offset(size.width * 0.42f, size.height * 0.74f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.74f),
            end = Offset(size.width * 0.82f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawResultSmallCloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val strokeWidth = 2.2.dp.toPx()
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
private fun DrawResultArrowRightIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.16f, size.height * 0.5f),
            end = Offset(size.width * 0.78f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.28f),
            end = Offset(size.width * 0.78f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.72f),
            end = Offset(size.width * 0.78f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun DrawQuizResultIcon(
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
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
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
        .mapNotNull { item -> item.sourcePart }
        .distinctBy { part -> part.id }
        .groupBy { part -> part.chapterId }

    if (groupedParts.isEmpty()) return emptyList()

    return groupedParts.entries
        .sortedBy { entry -> entry.key }
        .mapIndexed { index, entry ->
            val partText = entry.value.partText()
            "챕터 ${index + 1} / $partText"
        }
}

private fun List<PartSummary>.partText(): String {
    val partNumbers = map { part -> part.partNumber }
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

private fun QuizReviewItem.toReviewQuestion(): QuizPlayAllQuestion =
    QuizPlayAllQuestion(
        id = questionId,
        number = displayOrder,
        body = body,
        timerText = "00:00",
        questionType = ServerQuizType.MultipleChoice,
        answerValue = answerValue,
        correctExplanation = correctExplanation,
        incorrectExplanation = incorrectExplanation,
        options = options
            .sortedBy { option -> option.optionNumber }
            .map { option -> option.toReviewOption() },
    )

private fun QuestionOption.toReviewOption(): QuizPlayAllOption =
    QuizPlayAllOption(
        id = id,
        number = optionNumber,
        text = "$optionNumber. $content",
        value = content,
    )

private fun QuizReviewItem.optionState(option: QuizPlayAllOption): QuizPlayChoiceOptionState {
    val correct = option.matchesAnswerValue(answerValue) == true
    val selected = option.id == selectedOptionId ||
        option.matchesAnswerValue(selectedValue) == true

    return when {
        correct -> QuizPlayChoiceOptionState.Correct
        selected -> QuizPlayChoiceOptionState.Incorrect
        else -> QuizPlayChoiceOptionState.Default
    }
}

private fun QuestionOption.matchesAnswerValue(answerValue: String?): Boolean =
    QuizPlayAllOption(
        id = id,
        number = optionNumber,
        text = content,
        value = content,
    ).matchesAnswerValue(answerValue) == true

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun QuizResultScreenPreview() {
    QuiketTheme {
        QuizResultScreen(
            state = QuizResultState(
                result = QuizResult(
                    playSessionId = "play-1",
                    resultId = "result-1",
                    quizSessionId = "session-1",
                    subjectId = "subject-1",
                    subjectName = "SQLD",
                    totalCount = 5,
                    correctCount = 4,
                    wrongCount = 1,
                    skipCount = 0,
                    accuracyPct = 80,
                    elapsedMs = 270_000,
                    scoreMatched = true,
                    abuseFlagged = false,
                    rewards = RewardSummary(
                        dotoriEarned = 2,
                        xpEarned = 9,
                        leveledUp = false,
                        newLevel = null,
                        currentDotoriBalance = null,
                        currentXpTotal = null,
                    ),
                    reviewItems = emptyList(),
                    retryAvailable = null,
                    createdAt = null,
                ),
            ),
            onBackClick = {},
            onRetryAllClick = {},
            onRetryWrongClick = {},
        )
    }
}
