package com.f1.quiket.feature.floating.presentation.screen.materialcheck

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.R as DesignR
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.domain.model.PartSummary
import com.f1.quiket.feature.floating.presentation.viewmodel.MaterialCheckViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.f1.quiket.core.designsystem.theme.Gray800
import com.f1.quiket.core.designsystem.theme.Orange50
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.feature.floating.R

private val previewParts = listOf(
    PartSummary(
        id = "1",
        chapterId = "c1",
        name = "개념 정리 및 핵심 요약 내용",
        partNumber = 1,
        contentPreview = null
    ),
    PartSummary(
        id = "2",
        chapterId = "c1",
        name = "예제 문제 풀이",
        partNumber = 2,
        contentPreview = null
    ),
    PartSummary(
        id = "3",
        chapterId = "c1",
        name = "실전 모의고사 대비 전략",
        partNumber = 3,
        contentPreview = null
    ),
)

@Composable
fun MaterialCheckScreen(
    lectureUploadId: String,
    @Suppress("UNUSED_PARAMETER") chapterNumber: Int,
    onBackClick: () -> Unit = {},
    onComplete: (subjectId: String, chapterId: String, chapterName: String, partCount: Int) -> Unit = { _, _, _, _ -> },
    viewModel: MaterialCheckViewModel = hiltViewModel(),
) {
    val chapterName by viewModel.chapterName.collectAsStateWithLifecycle()
    val parts by viewModel.parts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(lectureUploadId) {
        viewModel.load(lectureUploadId)
    }

    MaterialCheckContent(
        chapterName = chapterName,
        parts = parts,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onComplete = { name, count ->
            onComplete(viewModel.subjectId, viewModel.chapterId, name, count)
        },
        onChapterNameChange = { viewModel.updateChapterName(it) },
        onPartNameChange = { partId, name -> viewModel.updatePartName(partId, name) },
    )
}

@Composable
private fun MaterialCheckContent(
    chapterName: String,
    parts: List<PartSummary>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onComplete: (chapterName: String, partCount: Int) -> Unit,
    onChapterNameChange: (String) -> Unit,
    onPartNameChange: (partId: String, name: String) -> Unit,
) {
    var editingChapterName by remember { mutableStateOf(false) }
    var editingPart by remember { mutableStateOf<PartSummary?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── TopBar ───────────────────────────────────────
            MaterialCheckTopBar(onBackClick = onBackClick)

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Brown950)
                }
            } else {
                // ── 스크롤 영역 ──────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // 헤더
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ai_check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "자료를 보기 쉽게 정리해봤어요",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Gray950,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "AI가 자료를 읽고 주제별 챕터로 나눴어요.\n마음에 들지 않는 부분은 직접 수정할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "챕터명",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Gray600,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // ── 챕터 카드 ────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Orange50)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = chapterName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Gray950,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { editingChapterName = true },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    painter = painterResource(DesignR.drawable.ic_detail_edit),
                                    contentDescription = "챕터명 수정",
                                    tint = Orange500,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── 파트 섹션 ────────────────────────────
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Gray700)) { append("파트 ") }
                            withStyle(SpanStyle(color = Orange500)) { append("${parts.size}") }
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        parts.forEach { part ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(White)
                                    .border(1.dp, Gray100, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(1000.dp))
                                        .background(Gray100)
                                        .size(24.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(
                                        text = "${part.partNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = Gray800,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = part.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = Gray950,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { editingPart = part },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(DesignR.drawable.ic_detail_edit),
                                        contentDescription = "파트명 수정",
                                        tint = Gray500,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ── 완료 버튼 (하단 고정) ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isLoading) Brown950 else Gray100)
                    .clickable(enabled = !isLoading) {
                        onComplete(chapterName, parts.size)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isLoading) White else Gray400,
                )
            }
        }

        // ── 챕터명 수정 다이얼로그 ────────────────────────
        if (editingChapterName) {
            NameEditDialog(
                title = "챕터명 수정",
                label = "챕터명",
                initialValue = chapterName,
                onDismiss = { editingChapterName = false },
                onApply = { newName ->
                    onChapterNameChange(newName)
                    editingChapterName = false
                },
            )
        }

        // ── 파트명 수정 다이얼로그 ────────────────────────
        editingPart?.let { part ->
            NameEditDialog(
                title = "파트명 수정",
                label = "파트명",
                initialValue = part.name,
                onDismiss = { editingPart = null },
                onApply = { newName ->
                    onPartNameChange(part.id, newName)
                    editingPart = null
                },
            )
        }
    }
}

// ── TopBar ──────────────────────────────────────────────────────────────────

@Composable
private fun MaterialCheckTopBar(
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
                painter = painterResource(DesignR.drawable.ic_topbar_back),
                contentDescription = "뒤로가기",
                tint = Gray950,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "자료 확인",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray950,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(DesignR.drawable.ic_topbar_back),
            contentDescription = null,
            tint = Color.Transparent,
            modifier = Modifier.size(48.dp)
        )
    }
}

// ── 이름 수정 다이얼로그 ─────────────────────────────────────────────────────

@Composable
private fun NameEditDialog(
    title: String,
    label: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Gray950,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Gray950,
                )
                BaseTextField(
                    value = value,
                    onValueChange = { value = it },
                    hint = "",
                    modifier = Modifier.fillMaxWidth(),
                    focusedBorderColor = Color.Unspecified
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Brown950, RoundedCornerShape(12.dp))
                        .background(White)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "취소",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray700,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brown950)
                        .clickable(enabled = value.isNotBlank()) { onApply(value.trim()) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "적용",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "자료 확인 — 파트 있음")
@Composable
private fun MaterialCheckPreview() {
    QuiketTheme {
        MaterialCheckContent(
            chapterName = "SQLD 핵심 개념 정리 및 예제 풀이 (두 줄 넘어가면 이렇게 보임)",
            parts = previewParts,
            isLoading = false,
            onBackClick = {},
            onComplete = { _, _ -> },
            onChapterNameChange = {},
            onPartNameChange = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, name = "다이얼로그 — 챕터명 수정")
@Composable
private fun ChapterNameEditDialogPreview() {
    QuiketTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NameEditDialog(
                title = "챕터명 수정",
                label = "챕터명",
                initialValue = "SQLD 핵심 개념 정리",
                onDismiss = {},
                onApply = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "다이얼로그 — 파트명 수정")
@Composable
private fun PartNameEditDialogPreview() {
    QuiketTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NameEditDialog(
                title = "파트명 수정",
                label = "파트명",
                initialValue = "개념 정리 및 핵심 요약 내용",
                onDismiss = {},
                onApply = {},
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "자료 확인 — 로딩 중")
@Composable
private fun MaterialCheckLoadingPreview() {
    QuiketTheme {
        MaterialCheckContent(
            chapterName = "",
            parts = emptyList(),
            isLoading = true,
            onBackClick = {},
            onComplete = { _, _ -> },
            onChapterNameChange = {},
            onPartNameChange = { _, _ -> },
        )
    }
}