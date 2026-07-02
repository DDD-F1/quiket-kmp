package com.f1.quiket.feature.floating.presentation.screen.subjectdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.AddSubjectCard
import com.f1.quiket.core.designsystem.component.SubjectLongCard
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Green800
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import com.f1.quiket.feature.floating.domain.model.Chapter

@Composable
internal fun SubjectHeaderSection(
    modifier: Modifier,
    studyPurposeLabel: String,
    examTypeLabel: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Green800)
            .padding(start = 20.dp, top = 4.dp),
    ) {
        Column(modifier = Modifier.padding(end = 220.dp)) {
            Text(
                text = studyPurposeLabel.ifBlank { "학습 목적을 입력해주세요." },
                style = MaterialTheme.typography.labelSmall.copy(color = Gray100),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = examTypeLabel.ifBlank { "과목 유형을 선택해주세요." },
                style = MaterialTheme.typography.labelSmall.copy(color = Gray100),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_detail_quiket),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(220f / 120f)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
internal fun MySubjectSection(
    chapters: List<Chapter>,
    isEditMode: Boolean = false,
    isDeleteMode: Boolean = false,
    onChapterClick: (Chapter) -> Unit,
    onChapterEditClick: (Chapter) -> Unit = {},
    onChapterDeleteClick: (Chapter) -> Unit = {},
    onChapterAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    isDeleteMode -> "삭제할 챕터를 선택해주세요"
                    isEditMode -> "수정할 챕터를 선택해주세요"
                    else -> "내 자료"
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
        ) {
            chapters.forEachIndexed { index, chapter ->
                SubjectLongCard(
                    title = chapter.name,
                    chapter = "챕터 ${chapter.number}",
                    part = "파트 ${chapter.partCount}개",
                    onClick = {
                        when {
                            isDeleteMode -> onChapterDeleteClick(chapter)
                            isEditMode -> onChapterEditClick(chapter)
                            else -> onChapterClick(chapter)
                        }
                    },
                    trailingIconRes = when {
                        isDeleteMode -> R.drawable.ic_detail_remove
                        isEditMode -> R.drawable.ic_detail_edit
                        else -> com.f1.quiket.core.designsystem.R.drawable.ic_subject_card_next
                    },
                )
                if (index < chapters.lastIndex) {
                    HorizontalDivider(color = White, thickness = 16.dp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(White, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .clickable { onChapterAddClick() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AddSubjectCard(
                title = "챕터 추가",
                onClick = onChapterAddClick,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}