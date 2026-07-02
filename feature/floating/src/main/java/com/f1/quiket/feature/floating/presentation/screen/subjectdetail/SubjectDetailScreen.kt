package com.f1.quiket.feature.floating.presentation.screen.subjectdetail

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.f1.quiket.core.designsystem.component.HomeActionButton
import com.f1.quiket.core.designsystem.component.HomeEmptyExamCard
import com.f1.quiket.core.designsystem.component.HomeExamCard
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Green800
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.domain.model.Chapter
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.presentation.component.SubjectDetailTopBar
import com.f1.quiket.feature.floating.presentation.screen.lectureview.LectureViewScreen
import com.f1.quiket.feature.floating.presentation.viewmodel.SubjectDetailViewModel

@Composable
fun SubjectDetailScreen(
    subjectId: String = "",
    subjectName: String,
    initialIsStarred: Boolean = false,
    studyPurposeLabel: String = "",
    examTypeLabel: String = "",
    refreshTrigger: Int = 0,
    onBackClick: () -> Unit = {},
    onChapterAddClick: (newChapterNumber: Int) -> Unit = {},
    onUploadClick: (newChapterNumber: Int) -> Unit = {},
    onEditSubjectType: (SubjectDetail?) -> Unit = {},
    onSubjectNameChanged: (String) -> Unit = {},
    onExamScheduleSaved: () -> Unit = {},
    onSubjectDeleted: () -> Unit = {},
    onCreateQuizClick: () -> Unit = {},
    onStarToggle: (Boolean) -> Unit = {},
    viewModel: SubjectDetailViewModel = hiltViewModel(),
) {
    val subjectDetail by viewModel.subjectDetail.collectAsState()
    val apiExamSchedule by viewModel.examSchedule.collectAsState()
    val apiSubjectName = subjectDetail?.name
    val apiLabels = remember(subjectDetail) { subjectDetail?.toLabels() }

    val chapters = remember(subjectDetail) {
        subjectDetail?.chapters?.map { chapter ->
            Chapter(
                id = chapter.id,
                number = chapter.displayOrder,
                name = chapter.name,
                partCount = chapter.parts.size,
            )
        } ?: emptyList()
    }

    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
        }
    }

    LaunchedEffect(viewModel) { viewModel.examScheduleSaved.collect { onExamScheduleSaved() } }
    LaunchedEffect(viewModel) { viewModel.deleteSubjectSuccess.collect { onSubjectDeleted() } }

    selectedChapter?.let { chapter ->
        BackHandler { selectedChapter = null }
        LectureViewScreen(
            subjectId = subjectId,
            chapter = chapter,
            onBackClick = { selectedChapter = null },
        )
        return
    }

    var displaySubjectName by rememberSaveable { mutableStateOf(subjectName) }
    var isStarred by rememberSaveable { mutableStateOf(initialIsStarred) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditSubjectNameDialog by remember { mutableStateOf(false) }
    var isChapterEditMode by remember { mutableStateOf(false) }
    var isChapterDeleteMode by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<Chapter?>(null) }
    var deletingChapter by remember { mutableStateOf<Chapter?>(null) }
    var confirmedExamName by rememberSaveable { mutableStateOf("") }
    var confirmedSchedule by rememberSaveable { mutableStateOf("") }
    var confirmedDDay by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.examScheduleUpdated.collect { schedule -> confirmedDDay = schedule.dDay }
    }
    LaunchedEffect(subjectId, refreshTrigger) {
        confirmedExamName = ""; confirmedSchedule = ""; confirmedDDay = null
        if (subjectId.isNotEmpty()) viewModel.loadSubject(subjectId)
    }
    LaunchedEffect(apiSubjectName) {
        if (!apiSubjectName.isNullOrBlank()) displaySubjectName = apiSubjectName
    }
    LaunchedEffect(apiExamSchedule) {
        val schedule = apiExamSchedule
        if (schedule != null) {
            confirmedExamName = schedule.examName
            confirmedSchedule = schedule.examDate
            confirmedDDay = schedule.dDay
        } else {
            confirmedExamName = ""; confirmedSchedule = ""; confirmedDDay = null
        }
    }

    Scaffold(containerColor = White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.background(Green800)) {
                SubjectDetailTopBar(
                    title = displaySubjectName,
                    isStarred = isStarred,
                    showMenu = showDropdownMenu,
                    onBackClick = onBackClick,
                    onStarClick = {
                        val newStarred = !isStarred
                        isStarred = newStarred
                        onStarToggle(newStarred)
                    },
                    onMenuClick = { showDropdownMenu = true },
                    onMenuDismiss = { showDropdownMenu = false },
                    onEditSubjectName = { showEditSubjectNameDialog = true },
                    onEditSubjectType = { onEditSubjectType(subjectDetail) },
                    onEditChapterName = { isChapterEditMode = true },
                    onDeleteSubjectClick = { showDeleteConfirmDialog = true },
                    onDeleteChapterClick = { isChapterDeleteMode = true },
                )
                SubjectHeaderSection(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    studyPurposeLabel = apiLabels?.h1?.takeIf { it.isNotBlank() } ?: studyPurposeLabel,
                    examTypeLabel = apiLabels?.h2?.takeIf { it.isNotBlank() } ?: examTypeLabel,
                )
            }

            Box(modifier = Modifier.fillMaxWidth().background(Brown50)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = White, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HomeActionButton(
                            text = "자료 업로드",
                            iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_upload,
                            backgroundColor = Gray100,
                            onClick = { onUploadClick(chapters.size) },
                            modifier = Modifier.height(103.dp).weight(1f),
                        )
                        HomeActionButton(
                            text = "퀴즈 만들기",
                            iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_make,
                            backgroundColor = Orange500,
                            onClick = onCreateQuizClick,
                            modifier = Modifier.height(103.dp).weight(1f),
                        )
                    }
                    if (confirmedSchedule.isNotBlank()) {
                        val dDayText = confirmedDDay?.let { d ->
                            when {
                                d > 0 -> "D-$d"
                                d == 0 -> "D-Day"
                                else -> "D+${-d}"
                            }
                        } ?: calcDDay(confirmedSchedule)
                        HomeExamCard(
                            examName = confirmedExamName.ifBlank { displaySubjectName },
                            date = confirmedSchedule,
                            dDay = dDayText,
                            onClick = { showScheduleDialog = true },
                            modifier = Modifier.padding(16.dp),
                        )
                    } else {
                        HomeEmptyExamCard({ showScheduleDialog = true }, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MySubjectSection(
                        chapters = chapters,
                        isEditMode = isChapterEditMode,
                        isDeleteMode = isChapterDeleteMode,
                        onChapterClick = { selectedChapter = it },
                        onChapterEditClick = { chapter -> editingChapter = chapter },
                        onChapterDeleteClick = { chapter -> deletingChapter = chapter },
                        onChapterAddClick = { onChapterAddClick(chapters.size) },
                    )
                }
            }
        }
    }

    if (showScheduleDialog) {
        AddTestCalendarDialog(
            subjectName = displaySubjectName,
            hasExistingSchedule = confirmedSchedule.isNotBlank(),
            initialExamName = confirmedExamName,
            initialDate = confirmedSchedule,
            onDismiss = { showScheduleDialog = false },
            onApply = { name, date ->
                confirmedExamName = name; confirmedSchedule = date; confirmedDDay = null
                if (subjectId.isNotEmpty()) viewModel.upsertExamSchedule(
                    subjectId, name.ifBlank { displaySubjectName }.ifBlank { null }, date,
                )
                showScheduleDialog = false
            },
            onDelete = {
                confirmedExamName = ""; confirmedSchedule = ""; confirmedDDay = null
                if (subjectId.isNotEmpty()) viewModel.deleteExamSchedule(subjectId)
                showScheduleDialog = false
            },
        )
    }

    if (showEditSubjectNameDialog) {
        EditSubjectNameDialog(
            currentName = displaySubjectName,
            onDismiss = { showEditSubjectNameDialog = false },
            onApply = { newName ->
                displaySubjectName = newName
                onSubjectNameChanged(newName)
                if (subjectId.isNotEmpty()) viewModel.updateSubjectName(subjectId, newName)
                showEditSubjectNameDialog = false
            },
        )
    }

    editingChapter?.let { chapter ->
        EditChapterNameDialog(
            currentName = chapter.name,
            onDismiss = { editingChapter = null; isChapterEditMode = false },
            onApply = { newName ->
                if (chapter.id.isNotEmpty()) viewModel.updateChapterName(chapter.id, newName)
                editingChapter = null; isChapterEditMode = false
            },
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteSubjectDialog(
            subjectName = displaySubjectName,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                if (subjectId.isNotEmpty()) viewModel.deleteSubject(subjectId)
            },
        )
    }

    deletingChapter?.let { chapter ->
        DeleteChapterDialog(
            chapterNumber = chapter.number,
            chapterName = chapter.name,
            onDismiss = { deletingChapter = null; isChapterDeleteMode = false },
            onConfirm = {
                if (chapter.id.isNotEmpty()) viewModel.deleteChapter(chapter.id)
                deletingChapter = null; isChapterDeleteMode = false
            },
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SubjectDetailScreen - 시험 일정 있음")
@Composable
private fun PreviewSubjectDetailScreenWithSchedule() {
    val sampleChapters = listOf(
        Chapter(id = "1", number = 1, name = "데이터 구조 기초", partCount = 3),
        Chapter(id = "2", number = 2, name = "알고리즘 분석", partCount = 5),
        Chapter(id = "3", number = 3, name = "정렬 알고리즘", partCount = 4),
    )
    QuiketTheme {
        Scaffold(containerColor = White) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(modifier = Modifier.background(Green800)) {
                    SubjectDetailTopBar(
                        title = "자료구조", isStarred = true, showMenu = false,
                        onBackClick = {}, onStarClick = {}, onMenuClick = {}, onMenuDismiss = {},
                        onEditSubjectName = {}, onEditSubjectType = {}, onEditChapterName = {}, onDeleteSubjectClick = {},
                    )
                    SubjectHeaderSection(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        studyPurposeLabel = "시험 준비",
                        examTypeLabel = "대학교",
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = White, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HomeActionButton(text = "자료 업로드", iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_upload, backgroundColor = Gray100, onClick = {}, modifier = Modifier.height(103.dp).weight(1f))
                    HomeActionButton(text = "퀴즈 만들기", iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_make, backgroundColor = Orange500, onClick = {}, modifier = Modifier.height(103.dp).weight(1f))
                }
                Box(modifier = Modifier.fillMaxWidth().background(Brown50)) {
                    HomeExamCard(examName = "기말고사", date = "2026.06.30", dDay = "D-16", onClick = {}, modifier = Modifier.padding(16.dp))
                }
                MySubjectSection(chapters = sampleChapters, onChapterClick = {}, onChapterAddClick = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "SubjectDetailScreen - 시험 일정 없음")
@Composable
private fun PreviewSubjectDetailScreenEmpty() {
    QuiketTheme {
        Scaffold(containerColor = White) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(modifier = Modifier.background(Green800)) {
                    SubjectDetailTopBar(
                        title = "영어 회화", isStarred = false, showMenu = false,
                        onBackClick = {}, onStarClick = {}, onMenuClick = {}, onMenuDismiss = {},
                        onEditSubjectName = {}, onEditSubjectType = {}, onEditChapterName = {}, onDeleteSubjectClick = {},
                    )
                    SubjectHeaderSection(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        studyPurposeLabel = "자기 개발",
                        examTypeLabel = "어학",
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = White, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HomeActionButton(text = "자료 업로드", iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_upload, backgroundColor = Gray100, onClick = {}, modifier = Modifier.height(103.dp).weight(1f))
                    HomeActionButton(text = "퀴즈 만들기", iconRes = com.f1.quiket.core.designsystem.R.drawable.ic_home_make, backgroundColor = Orange500, onClick = {}, modifier = Modifier.height(103.dp).weight(1f))
                }
                Box(modifier = Modifier.fillMaxWidth().background(Brown50)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HomeEmptyExamCard(onClick = {}, modifier = Modifier.padding(16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                MySubjectSection(chapters = emptyList(), onChapterClick = {}, onChapterAddClick = {})
            }
        }
    }
}