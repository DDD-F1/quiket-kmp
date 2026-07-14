package com.f1.quiket.composeapp.home.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary

private val MainBottomContentPadding = 112.dp

@Composable
fun HomeUploadSubjectPickerScreen(
    homeState: HomeUiState,
    starredSubjectIds: Set<String>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (SubjectSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subjects = (homeState as? HomeUiState.Success)
        ?.data
        ?.subjects
        .orEmpty()
        .sortedForDisplay(starredSubjectIds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = MainBottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HomeUploadPickerTopBar(onBackClick = onBackClick)
        Text(
            text = "자료를 추가할 과목을 선택해주세요",
            color = QuiketGray700,
            style = MaterialTheme.typography.bodyMedium,
        )
        QuiketPrimaryButton(
            text = "새 과목 추가하기",
            enabled = true,
            onClick = onAddSubjectClick,
            containerColor = QuiketWhite,
            contentColor = QuiketGray950,
            modifier = Modifier.border(2.dp, QuiketBrown950, RoundedCornerShape(12.dp)),
        )

        when (homeState) {
            HomeUiState.Loading -> HomeSummaryCard(
                title = "과목을 불러오는 중",
                description = "업로드할 과목 목록을 확인하고 있어요.",
            )

            is HomeUiState.Error -> HomeErrorCard(
                title = "과목 목록을 불러오지 못했어요",
                message = homeState.message,
                onRetry = onRetryClick,
            )

            is HomeUiState.Success -> {
                if (subjects.isEmpty()) {
                    HomeSummaryCard(
                        title = "아직 과목이 없어요",
                        description = "자료를 업로드하려면 먼저 과목을 추가해주세요.",
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        subjects.forEach { subject ->
                            HomeUploadSubjectPickerCard(
                                subject = subject,
                                isStarred = subject.id in starredSubjectIds,
                                onClick = { onSubjectClick(subject) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeUploadPickerTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "‹",
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .semantics { contentDescription = "뒤로" }
                .clickable(role = Role.Button, onClick = onBackClick),
        )
        Text(
            text = "자료 업로드",
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun HomeUploadSubjectPickerCard(
    subject: SubjectSummary,
    isStarred: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "자료 업로드 과목 ${subject.name}"
            },
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(QuiketBrown50),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = subject.name.take(1),
                    color = QuiketBrown950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subject.name,
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (isStarred) {
                        Text(
                            text = "★",
                            color = QuiketOrange500,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                Text(
                    text = "${subject.chapterCount}챕터 · ${subject.partCount}파트",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = ">",
                color = QuiketGray400,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

private fun List<SubjectSummary>.sortedForDisplay(
    starredSubjectIds: Set<String>,
): List<SubjectSummary> =
    filter { subject -> subject.id in starredSubjectIds }.sortedBy { it.name } +
        filterNot { subject -> subject.id in starredSubjectIds }
