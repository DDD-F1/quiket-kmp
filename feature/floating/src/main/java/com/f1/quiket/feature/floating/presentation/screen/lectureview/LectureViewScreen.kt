package com.f1.quiket.feature.floating.presentation.screen.lectureview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.R as DesignSystemR
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.component.QuiketToast
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Dimmed
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.domain.model.Chapter
import com.f1.quiket.feature.floating.domain.model.LectureItem
import com.f1.quiket.feature.floating.presentation.component.LectureViewBottomBar
import com.f1.quiket.feature.floating.presentation.component.LectureViewTopBar
import com.f1.quiket.feature.floating.presentation.component.TocSidePanel
import com.f1.quiket.feature.floating.presentation.viewmodel.LectureViewEvent
import com.f1.quiket.feature.floating.presentation.viewmodel.LectureViewViewModel
import kotlinx.coroutines.delay


@Composable
fun LectureViewScreen(
    subjectId: String,
    chapter: Chapter,
    onBackClick: () -> Unit,
    viewModel: LectureViewViewModel = hiltViewModel(),
) {
    val tocChapters by viewModel.tocChapters.collectAsStateWithLifecycle()
    val allPartIds by viewModel.allPartIds.collectAsStateWithLifecycle()
    val currentPartId by viewModel.currentPartId.collectAsStateWithLifecycle()
    val currentPartName by viewModel.currentPartName.collectAsStateWithLifecycle()
    val currentPartContent by viewModel.currentPartContent.collectAsStateWithLifecycle()
    val isUpdatingPartName by viewModel.isUpdatingPartName.collectAsStateWithLifecycle()

    var showTocSidebar by remember { mutableStateOf(false) }
    var showEditPartNameDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }
    var visibleToastMessage by remember { mutableStateOf<String?>(null) }
    var visibleToastIcon by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(subjectId, chapter.id) {
        if (subjectId.isNotEmpty() && chapter.id.isNotEmpty()) {
            viewModel.loadSubject(subjectId, chapter.id)
        }
    }

    // 파트 전환 시 편집 모드 해제
    LaunchedEffect(currentPartId) { isEditMode = false }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LectureViewEvent.PartNameUpdated -> {
                    showEditPartNameDialog = false
                    visibleToastMessage = "파트명이 수정됐어요."
                    visibleToastIcon = DesignSystemR.drawable.ic_upload_ok
                }
                is LectureViewEvent.ShowMessage -> {
                    visibleToastMessage = event.message
                    visibleToastIcon = DesignSystemR.drawable.ic_upload_fail
                }
            }
            delay(LECTURE_VIEW_TOAST_DURATION_MILLIS)
            visibleToastMessage = null
            visibleToastIcon = null
        }
    }

    val currentPartIndex = remember(currentPartId, allPartIds) {
        allPartIds.indexOfFirst { it == currentPartId }.coerceAtLeast(0)
    }

    val displayItems = remember(currentPartContent, currentPartName) {
        val content = currentPartContent
        val name = currentPartName ?: ""
        if (content.isNullOrBlank()) emptyList()
        else listOf(LectureItem(id = 1, number = "", title = name, content = content))
    }

    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Scaffold(
            containerColor = Gray100,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
            topBar = {
                LectureViewTopBar(
                    title = chapter.name,
                    onBackClick = onBackClick,
                    onTocClick = { showTocSidebar = true },
                    onEditPartNameClick = { showEditPartNameDialog = true },
                    onEditClick = {
                        editedContent = currentPartContent ?: ""
                        isEditMode = true
                    },
                    onDeletePartClick = {},
                )
            },
            bottomBar = {
                LectureViewBottomBar(
                    currentIndex = currentPartIndex,
                    totalCount = allPartIds.size.coerceAtLeast(1),
                    onPrevious = {
                        if (currentPartIndex > 0) viewModel.selectPart(allPartIds[currentPartIndex - 1])
                    },
                    onNext = {
                        if (currentPartIndex < allPartIds.size - 1) viewModel.selectPart(allPartIds[currentPartIndex + 1])
                    },
                    isEditMode = isEditMode,
                    onCancelEdit = {
                        isEditMode = false
                        editedContent = ""
                    },
                    onSaveEdit = {
                        val partId = currentPartId
                        if (partId != null) viewModel.updatePartContent(partId, editedContent)
                        isEditMode = false
                    },
                )
            },
        ) { innerPadding ->
            LectureList(
                items = displayItems,
                chapterNumber = chapter.number,
                partNumber = currentPartIndex + 1,
                partTitle = currentPartName ?: "",
                isEditMode = isEditMode,
                editedContent = editedContent,
                onContentChange = { editedContent = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }

        if (showTocSidebar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dimmed)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { showTocSidebar = false },
                    )
                    .zIndex(1f),
            )
        }

        AnimatedVisibility(
            visible = showTocSidebar,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .zIndex(2f),
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
        ) {
            TocSidePanel(
                chapters = tocChapters,
                selectedPartId = currentPartId,
                onPartClick = { partId ->
                    viewModel.selectPart(partId)
                    showTocSidebar = false
                },
                onClose = { showTocSidebar = false },
            )
        }

        visibleToastMessage?.let { message ->
            QuiketToast(
                message = message,
                icon = visibleToastIcon,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .zIndex(3f),
            )
        }
    }

    if (showEditPartNameDialog) {
        EditPartNameDialog(
            currentName = currentPartName ?: "",
            onDismiss = { showEditPartNameDialog = false },
            onApply = { newName ->
                currentPartId?.let { viewModel.updatePartName(it, newName) }
            },
            isApplying = isUpdatingPartName,
        )
    }
}

@Composable
private fun LectureList(
    items: List<LectureItem>,
    chapterNumber: Int,
    partNumber: Int,
    partTitle: String,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    editedContent: String = "",
    onContentChange: (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray50)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "챕터 $chapterNumber",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Gray500,
            )
            Text(text = ">", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Gray500)
            Text(
                text = "파트 $partNumber",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Gray500,
            )
            Text(
                text = partTitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Gray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        if (isEditMode) {
            TextField(
                value = editedContent,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brown50)
                    .verticalScroll(rememberScrollState()),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Gray950,
                    lineHeight = 20.sp,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Brown50,
                    unfocusedContainerColor = Brown50,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brown50)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                items(items, key = { it.id }) { item ->
                    LectureTextItem(item = item)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun LectureTextItem(
    item: LectureItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        }
        Text(
            text = item.content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Gray950,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun EditPartNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    isApplying: Boolean = false,
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = { if (!isApplying) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "파트명 수정",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "파트명",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray700,
                    ),
                )
                Spacer(modifier = Modifier.height(6.dp))
                BaseTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = "파트명을 입력해주세요",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isApplying,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                        border = BorderStroke(2.dp, Brown950),
                    ) {
                        Text(
                            "취소", style = MaterialTheme.typography.bodySmall,
                            color = Brown950
                        )
                    }
                    Button(
                        onClick = { onApply(name.trim()) },
                        enabled = name.trim().isNotBlank() && !isApplying,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown950,
                            contentColor = White,
                            disabledContainerColor = Gray300,
                            disabledContentColor = White,
                        ),
                    ) {
                        Text("적용", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private const val LECTURE_VIEW_TOAST_DURATION_MILLIS = 2_500L

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LectureViewScreenPreview() {
    QuiketTheme {
        LectureViewScreen(
            subjectId = "",
            chapter = Chapter(number = 1, name = "SQLD", partCount = 3),
            onBackClick = {},
        )
    }
}
