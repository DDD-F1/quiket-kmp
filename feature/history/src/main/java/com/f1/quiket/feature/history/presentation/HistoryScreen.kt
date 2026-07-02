package com.f1.quiket.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.component.QuiketTopBar
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.core.designsystem.theme.Yellow100
import com.f1.quiket.core.designsystem.theme.Yellow500
import com.f1.quiket.feature.history.domain.model.RecentActivity
import com.f1.quiket.feature.history.domain.model.RecentActivityType

@Composable
fun HistoryScreen(
    state: HistoryState,
    onIntent: (HistoryIntent) -> Unit,
    onActivityClick: (RecentActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Brown50,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            QuiketTopBar(onNoteIconClick = {})
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = "퀴즈 기록",
                color = Gray950,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp),
            )

            when {
                state.isLoading && state.activities.isEmpty() -> {
                    HistoryMessage(
                        text = "기록을 불러오는 중이에요",
                        modifier = Modifier.weight(1f),
                    )
                }

                state.activities.isEmpty() -> {
                    HistoryEmpty(
                        message = state.errorMessage ?: "아직 퀴즈 기록이 없어요",
                        onRetryClick = { onIntent(HistoryIntent.Refresh) },
                        retryVisible = state.errorMessage != null,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.activities,
                            key = { activity -> activity.activityId },
                        ) { activity ->
                            HistoryActivityCard(
                                activity = activity,
                                onClick = { onActivityClick(activity) },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }

                        item {
                            when {
                                state.isLoadingMore -> HistoryMessage(
                                    text = "더 불러오는 중이에요",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                )

                                state.hasNext -> HistoryLoadMoreButton(
                                    onClick = { onIntent(HistoryIntent.LoadMore) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryActivityCard(
    activity: RecentActivity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickable = activity.isClickable

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .clickable(
                enabled = clickable,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HistoryStatusChip(activity = activity)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = activity.createdAt.toDateLabel(),
                color = Gray600,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        Text(
            text = activity.title,
            color = Gray950,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = activity.subjectName,
                color = Gray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            activity.scoreText?.takeIf { score -> score.isNotBlank() }?.let { score ->
                Text(
                    text = score,
                    color = Brown950,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            activity.progressPct?.let { progress ->
                Text(
                    text = "$progress%",
                    color = Gray700,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HistoryStatusChip(
    activity: RecentActivity,
    modifier: Modifier = Modifier,
) {
    val label = when (activity.activityType) {
        RecentActivityType.QuizGenerating -> "생성 중"
        RecentActivityType.QuizReady -> "풀기 전"
        RecentActivityType.QuizInProgress -> "진행 중"
        RecentActivityType.QuizCompleted -> "완료"
        RecentActivityType.LectureUploaded -> "업로드"
        RecentActivityType.Unknown -> activity.status ?: "기록"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(1000.dp))
            .background(if (activity.activityType == RecentActivityType.QuizCompleted) Yellow100 else Gray100)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (activity.activityType == RecentActivityType.QuizCompleted) Yellow500 else Gray700,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun HistoryLoadMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "더 보기",
            color = Brown950,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun HistoryEmpty(
    message: String,
    onRetryClick: () -> Unit,
    retryVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = Gray600,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (retryVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            QuiketPrimaryButton(
                text = "다시 불러오기",
                onClick = onRetryClick,
                enabled = true,
            )
        }
    }
}

@Composable
private fun HistoryMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Gray400,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

private val RecentActivity.isClickable: Boolean
    get() = when (activityType) {
        RecentActivityType.QuizCompleted -> resultId != null || playSessionId != null
        RecentActivityType.QuizReady,
        RecentActivityType.QuizInProgress,
        -> quizSessionId != null
        RecentActivityType.QuizGenerating,
        RecentActivityType.LectureUploaded,
        RecentActivityType.Unknown,
        -> false
    }

private fun String.toDateLabel(): String =
    substringBefore('T').takeIf { date -> date.isNotBlank() } ?: this

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun HistoryScreenPreview() {
    QuiketTheme {
        HistoryScreen(
            state = HistoryState(
                activities = listOf(
                    RecentActivity(
                        activityId = "activity-1",
                        activityType = RecentActivityType.QuizCompleted,
                        quizSessionId = "quiz-1",
                        playSessionId = "play-1",
                        resultId = "result-1",
                        title = "데이터베이스 객관식 10문제",
                        subjectId = "subject-1",
                        subjectName = "SQLD",
                        status = "completed",
                        progressPct = null,
                        scoreText = "8/10",
                        createdAt = "2026-05-28T20:00:00+09:00",
                    ),
                    RecentActivity(
                        activityId = "activity-2",
                        activityType = RecentActivityType.QuizReady,
                        quizSessionId = "quiz-2",
                        playSessionId = null,
                        resultId = null,
                        title = "SQL 활용 O/X 5문제",
                        subjectId = "subject-1",
                        subjectName = "SQLD",
                        status = "ready",
                        progressPct = null,
                        scoreText = null,
                        createdAt = "2026-05-27T20:00:00+09:00",
                    ),
                ),
            ),
            onIntent = {},
            onActivityClick = {},
        )
    }
}
