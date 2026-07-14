package com.f1.quiket.composeapp.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTopBar
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.history.domain.model.HistoryActivity
import com.f1.quiket.composeapp.history.domain.model.HistoryActivityType

@Composable
fun HistoryTab(
    state: HistoryUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onActivityClick: (HistoryActivity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50),
    ) {
        QuiketTopBar(onNoteIconClick = {})

        Text(
            text = "퀴즈 기록",
            color = QuiketGray950,
            style = MaterialTheme.typography.headlineMedium.copy(
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
                    retryVisible = state.errorMessage != null,
                    onRetryClick = onRefresh,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(
                        items = state.activities,
                        key = { _, activity -> activity.activityId },
                    ) { index, activity ->
                        HistoryActivityCard(
                            activity = activity,
                            position = index + 1,
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

                            state.hasNext -> QuiketPrimaryButton(
                                text = "더 보기",
                                enabled = true,
                                onClick = onLoadMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
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

@Composable
private fun HistoryActivityCard(
    activity: HistoryActivity,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = activity.isActionClickable,
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = activity.accessibilityLabel(position)
                if (activity.isActionClickable) {
                    role = Role.Button
                    onClick {
                        onClick()
                        true
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = QuiketWhite,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                )
            }

            Text(
                text = activity.title,
                color = QuiketGray950,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = activity.subjectName,
                    color = QuiketGray700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                activity.scoreText?.takeIf { it.isNotBlank() }?.let { score ->
                    Text(
                        text = score,
                        color = QuiketBrown950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                activity.progressPct?.let { progress ->
                    Text(
                        text = "$progress%",
                        color = QuiketGray700,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryStatusChip(
    activity: HistoryActivity,
    modifier: Modifier = Modifier,
) {
    val label = activity.statusLabel()
    val completed = activity.activityType == HistoryActivityType.QuizCompleted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (completed) QuiketBrown50 else QuiketGray100)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (completed) QuiketOrange500 else QuiketGray700,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun HistoryEmpty(
    message: String,
    retryVisible: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = QuiketGray600,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
        if (retryVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            QuiketPrimaryButton(
                text = "다시 불러오기",
                enabled = true,
                onClick = onRetryClick,
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
            color = QuiketGray400,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

private fun String.toDateLabel(): String =
    substringBefore('T').takeIf { it.isNotBlank() } ?: this

private val HistoryActivity.isActionClickable: Boolean
    get() = when (activityType) {
        HistoryActivityType.QuizCompleted -> resultId != null || playSessionId != null
        HistoryActivityType.QuizReady,
        HistoryActivityType.QuizInProgress -> quizSessionId != null
        HistoryActivityType.QuizGenerating,
        HistoryActivityType.LectureUploaded,
        HistoryActivityType.Unknown -> false
    }

private fun HistoryActivity.accessibilityLabel(position: Int): String =
    listOfNotNull(
        "기록 $position",
        title.takeIf { it.isNotBlank() },
        subjectName.takeIf { it.isNotBlank() },
        statusLabel(),
        scoreText?.takeIf { it.isNotBlank() },
        progressPct?.let { "$it%" },
    ).joinToString(", ")

private fun HistoryActivity.statusLabel(): String = when (activityType) {
    HistoryActivityType.QuizGenerating -> "생성 중"
    HistoryActivityType.QuizReady -> "풀기 전"
    HistoryActivityType.QuizInProgress -> "진행 중"
    HistoryActivityType.QuizCompleted -> "완료"
    HistoryActivityType.LectureUploaded -> "업로드"
    HistoryActivityType.Unknown -> status?.takeIf { it.isNotBlank() } ?: "기록"
}
