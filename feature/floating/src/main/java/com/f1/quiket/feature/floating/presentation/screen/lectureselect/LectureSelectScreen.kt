package com.f1.quiket.feature.floating.presentation.screen.lectureselect

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.component.SubjectLongCard
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.presentation.viewmodel.LectureSelectViewModel

// ─── Model ─────────────────────────────────────────────────────────────────────

data class LectureSelectItem(
    val id: String,
    val category: String,
    val title: String,
    val chapterCount: Int,
)

// ─── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LectureSelectScreen(
    onBackClick: () -> Unit,
    onLectureSelected: (lectureId: String, lectureTitle: String, chapterCount: Int, category: String) -> Unit,
    onAddSubjectClick: () -> Unit = {},
    initialSelectedId: String? = null,
    viewModel: LectureSelectViewModel = hiltViewModel(),
) {
    val lectures by viewModel.subjects.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(initialSelectedId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
    ) {
        // TopBar
        LectureSelectTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "어떤 과목에 자료를 추가할까요?",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Brown950,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "업로드한 자료는 해당 과목 내의 챕터로 저장 돼요.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 새 과목 추가 버튼
            QuiketPrimaryButton(
                text = "새 과목 추가하기",
                onClick = onAddSubjectClick,
                containerColor = White,
                contentColor = Gray950,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Brown950, RoundedCornerShape(12.dp))
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 과목 목록
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(lectures) { lecture ->
                val isSelected = lecture.id == selectedId
                SubjectLongCard(
                    title = lecture.title,
                    chapter = lecture.category,
                    part = "챕터 ${lecture.chapterCount}개",
                    onClick = { selectedId = lecture.id },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Brown950 else Color.Transparent)
                                .border(1.5.dp, if (isSelected) Brown950 else Gray400, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(White),
                                )
                            }
                        }
                    },
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        // 다음 버튼
        LectureSelectNextButton(
            enabled = selectedId != null,
            onClick = {
                val selected = lectures.first { it.id == selectedId }
                onLectureSelected(selected.id, selected.title, selected.chapterCount, selected.category)
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
fun LectureSelectTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
                contentDescription = "뒤로가기",
                tint = Gray950,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "과목 선택",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray950,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_topbar_back),
            contentDescription = "Null Icon",
            tint = Color.Transparent,
        )
    }
}

// ─── 과목 카드 ──────────────────────────────────────────────────────────────────

@Composable
private fun LectureSelectCard(
    lecture: LectureSelectItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Brown950 else Gray400,
                shape = RoundedCornerShape(12.dp),
            )
            .background(White)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 카테고리 태그
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Gray100)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = lecture.category,
                    fontSize = 10.sp,
                    color = Gray600,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lecture.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Brown950,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "챕터 ${lecture.chapterCount}개",
                fontSize = 12.sp,
                color = Gray500,
            )
        }

        // 라디오 버튼
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 6.dp else 1.5.dp,
                    color = if (isSelected) Brown950 else Gray400,
                    shape = CircleShape,
                )
                .background(White),
        )
    }
}

// ─── 다음 버튼 ──────────────────────────────────────────────────────────────────

@Composable
private fun LectureSelectNextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Brown950 else Gray100)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "다음",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) White else Gray400,
        )
    }
}