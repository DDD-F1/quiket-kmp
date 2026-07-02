package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.subject.domain.model.*

import org.koin.compose.koinInject
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.subject.presentation.MaterialCheckStateHolder
import com.f1.quiket.composeapp.subject.presentation.MaterialCheckUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_ai_check
import quiket.composeapp.generated.resources.ic_detail_edit
import quiket.composeapp.generated.resources.ic_topbar_back

@Composable
internal fun MaterialCheckRoute(
    lectureUploadId: String,
    onBackClick: () -> Unit,
    onComplete: (firstPartId: String?) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<MaterialCheckStateHolder>()

    fun loadUploadStatus() {
        coroutineScope.launch {
            stateHolder.loadUploadStatus(
                lectureUploadId = lectureUploadId,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    fun updateChapterName(name: String) {
        coroutineScope.launch {
            stateHolder.updateChapterName(
                name = name,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    fun updatePartName(partId: String, name: String) {
        coroutineScope.launch {
            stateHolder.updatePartName(
                partId = partId,
                name = name,
                onSessionExpired = onSessionExpired,
            )
        }
    }

    LaunchedEffect(lectureUploadId) {
        loadUploadStatus()
    }

    MaterialCheckScreen(
        state = stateHolder.state,
        isSaving = stateHolder.isSaving,
        feedbackMessage = stateHolder.feedbackMessage,
        onBackClick = onBackClick,
        onRetryClick = ::loadUploadStatus,
        onCompleteClick = {
            val progress = (stateHolder.state as? MaterialCheckUiState.Success)?.progress
            onComplete(progress?.parts?.minByOrNull { it.partNumber }?.id)
        },
        onChapterNameChange = ::updateChapterName,
        onPartNameChange = ::updatePartName,
        modifier = modifier,
    )
}

@Composable
private fun MaterialCheckScreen(
    state: MaterialCheckUiState,
    isSaving: Boolean,
    feedbackMessage: String?,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onChapterNameChange: (String) -> Unit,
    onPartNameChange: (partId: String, name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isChapterEditing by remember { mutableStateOf(false) }
    var editingPart by remember { mutableStateOf<PartSummary?>(null) }
    val success = state as? MaterialCheckUiState.Success

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketWhite),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MaterialCheckTopBar(onBackClick = onBackClick)

            when (state) {
                MaterialCheckUiState.Loading -> MaterialCheckLoading(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                is MaterialCheckUiState.Error -> MaterialCheckError(
                    message = state.message,
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                is MaterialCheckUiState.Success -> MaterialCheckContent(
                    progress = state.progress,
                    feedbackMessage = feedbackMessage,
                    isSaving = isSaving,
                    onChapterEditClick = { isChapterEditing = true },
                    onPartEditClick = { editingPart = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (success != null && !isSaving) QuiketBrown950 else QuiketGray100)
                    .clickable(enabled = success != null && !isSaving, onClick = onCompleteClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (success != null && !isSaving) QuiketWhite else QuiketGray400,
                )
            }
        }
    }

    if (isChapterEditing && success != null) {
        MaterialNameEditDialog(
            title = "챕터명 수정",
            label = "챕터명",
            initialValue = success.progress.chapterName.orEmpty(),
            isSaving = isSaving,
            onDismiss = { if (!isSaving) isChapterEditing = false },
            onApply = { name ->
                onChapterNameChange(name)
                isChapterEditing = false
            },
        )
    }

    editingPart?.let { part ->
        MaterialNameEditDialog(
            title = "파트명 수정",
            label = "파트명",
            initialValue = part.name,
            isSaving = isSaving,
            onDismiss = { if (!isSaving) editingPart = null },
            onApply = { name ->
                onPartNameChange(part.id, name)
                editingPart = null
            },
        )
    }
}

@Composable
private fun MaterialCheckTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "뒤로" }
                .clickable(role = Role.Button, onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_topbar_back),
                contentDescription = null,
                tint = QuiketGray950,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "자료 확인",
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(Res.drawable.ic_topbar_back),
            contentDescription = null,
            tint = Color.Transparent,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun MaterialCheckLoading(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = QuiketBrown950)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "자료를 정리하고 있어요",
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "잠시만 기다려주세요.",
            color = QuiketGray600,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MaterialCheckError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "자료를 불러오지 못했어요",
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = QuiketGray600,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = QuiketWhite,
            border = BorderStroke(1.dp, QuiketGray300),
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onRetryClick),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "다시 시도",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun MaterialCheckContent(
    progress: LectureUploadProgress,
    feedbackMessage: String?,
    isSaving: Boolean,
    onChapterEditClick: () -> Unit,
    onPartEditClick: (PartSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_ai_check),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "자료를 보기 쉽게 정리해봤어요",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "AI가 자료를 읽고 주제별 챕터로 나눴어요.\n마음에 들지 않는 부분은 직접 수정할 수 있어요.",
            color = QuiketGray400,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "챕터명",
                color = QuiketGray600,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
            MaterialChapterCard(
                name = progress.chapterName.orEmpty().ifBlank { "새 챕터" },
                isSaving = isSaving,
                onEditClick = onChapterEditClick,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "파트",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${progress.parts.size}",
                    color = QuiketOrange500,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                )
            }

            if (progress.parts.isEmpty()) {
                MaterialEmptyPartCard()
            } else {
                progress.parts.sortedBy { it.partNumber }.forEach { part ->
                    MaterialPartRow(
                        part = part,
                        isSaving = isSaving,
                        onEditClick = { onPartEditClick(part) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!feedbackMessage.isNullOrBlank()) {
            Text(
                text = feedbackMessage,
                color = if (feedbackMessage.contains("못") || feedbackMessage.contains("입력")) {
                    QuiketNegative
                } else {
                    QuiketGray600
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MaterialChapterCard(
    name: String,
    isSaving: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialOrange50)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        MaterialEditIconButton(
            tint = QuiketOrange500,
            enabled = !isSaving,
            onClick = onEditClick,
        )
    }
}

@Composable
private fun MaterialPartRow(
    part: PartSummary,
    isSaving: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketWhite)
            .border(1.dp, QuiketGray100, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(1000.dp))
                .background(QuiketGray100)
                .size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${part.partNumber}",
                color = MaterialGray800,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = part.name,
            color = QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        MaterialEditIconButton(
            tint = QuiketGray600,
            enabled = !isSaving,
            onClick = onEditClick,
        )
    }
}

@Composable
private fun MaterialEditIconButton(
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_detail_edit),
            contentDescription = "이름 수정",
            tint = if (enabled) tint else QuiketGray300,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun MaterialEmptyPartCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(QuiketWhite)
            .border(1.dp, QuiketGray100, RoundedCornerShape(14.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "정리된 파트가 아직 없어요.",
            color = QuiketGray600,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun MaterialNameEditDialog(
    title: String,
    label: String,
    initialValue: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue.take(MaterialNameMaxLength)) }
    val trimmedValue = value.trim()

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(QuiketWhite)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = label,
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
                QuiketTextField(
                    value = value,
                    onValueChange = { value = it.take(MaterialNameMaxLength) },
                    hint = "",
                    enabled = !isSaving,
                    focusedBorderColor = Color.Unspecified,
                    modifier = Modifier.fillMaxWidth(),
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
                        .border(2.dp, QuiketBrown950, RoundedCornerShape(12.dp))
                        .background(QuiketWhite)
                        .clickable(enabled = !isSaving, onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "취소",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = QuiketGray700,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (trimmedValue.isNotBlank() && !isSaving) {
                                QuiketBrown950
                            } else {
                                QuiketGray100
                            },
                        )
                        .clickable(enabled = trimmedValue.isNotBlank() && !isSaving) {
                            onApply(trimmedValue)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isSaving) "저장 중" else "적용",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trimmedValue.isNotBlank() && !isSaving) QuiketWhite else QuiketGray400,
                    )
                }
            }
        }
    }
}

private const val MaterialNameMaxLength = 30
private val MaterialOrange50 = Color(0xFFFFF7ED)
private val MaterialGray800 = Color(0xFF656668)
