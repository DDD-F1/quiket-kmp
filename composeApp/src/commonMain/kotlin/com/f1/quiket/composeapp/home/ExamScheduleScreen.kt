package com.f1.quiket.composeapp.home

import org.koin.compose.koinInject
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray500
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import com.f1.quiket.composeapp.home.presentation.ExamEditorMode
import com.f1.quiket.composeapp.home.presentation.ExamScheduleItem
import com.f1.quiket.composeapp.home.presentation.ExamScheduleStateHolder
import com.f1.quiket.composeapp.home.presentation.ExamScheduleUiState
import com.f1.quiket.composeapp.home.presentation.currentLocalDate
import com.f1.quiket.composeapp.home.presentation.daysInMonth
import com.f1.quiket.composeapp.home.presentation.firstDayOffset
import com.f1.quiket.composeapp.home.presentation.formattedDate
import com.f1.quiket.composeapp.home.presentation.koreanDayOfWeek
import com.f1.quiket.composeapp.home.presentation.monthNumberValue
import com.f1.quiket.composeapp.home.presentation.parsedDate
import com.f1.quiket.composeapp.home.presentation.resolvedDDay
import com.f1.quiket.composeapp.home.presentation.toIsoDate
import com.f1.quiket.composeapp.home.presentation.toMonth
import com.f1.quiket.composeapp.util.hidePlatformKeyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_detail_edit
import quiket.composeapp.generated.resources.ic_detail_etc
import quiket.composeapp.generated.resources.ic_detail_remove

@Composable
internal fun ExamScheduleRoute(
    onBackClick: () -> Unit,
    onQuizClick: (subjectId: String) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reloadKey by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = koinInject<ExamScheduleStateHolder>()

    LaunchedEffect(stateHolder, reloadKey) {
        stateHolder.loadSubjects(onSessionExpired = onSessionExpired)
    }

    ExamScheduleScreen(
        state = stateHolder.state,
        onBackClick = onBackClick,
        onRetryClick = {
            reloadKey += 1
        },
        onAddExamClick = stateHolder::requestAddExam,
        onEditExamClick = stateHolder::requestEditExam,
        onDeleteExamClick = stateHolder::requestDeleteExam,
        onQuizClick = { exam ->
            onQuizClick(exam.subjectId)
        },
        feedbackMessage = stateHolder.feedbackMessage,
        modifier = modifier,
    )

    val successState = stateHolder.state as? ExamScheduleUiState.Success
    val currentEditorMode = stateHolder.editorMode
    if (successState != null && currentEditorMode != null) {
        ExamEditorDialog(
            mode = currentEditorMode,
            subjects = successState.subjects,
            registeredSubjectIds = successState.exams.map { it.subjectId }.toSet(),
            isSaving = stateHolder.isMutating,
            onSave = { subjectId, examName, examDate ->
                coroutineScope.launch {
                    stateHolder.saveExam(
                        subjectId = subjectId,
                        examName = examName,
                        examDate = examDate,
                        onSessionExpired = onSessionExpired,
                    )
                }
            },
            onDismiss = stateHolder::dismissEditor,
        )
    }

    stateHolder.deleteTarget?.let { exam ->
        DeleteExamDialog(
            exam = exam,
            isDeleting = stateHolder.isMutating,
            onConfirm = {
                coroutineScope.launch {
                    stateHolder.deleteExam(
                        exam = exam,
                        onSessionExpired = onSessionExpired,
                    )
                }
            },
            onDismiss = stateHolder::dismissDeleteDialog,
        )
    }
}

@Composable
private fun ExamScheduleScreen(
    state: ExamScheduleUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddExamClick: () -> Unit,
    onEditExamClick: (ExamScheduleItem) -> Unit,
    onDeleteExamClick: (ExamScheduleItem) -> Unit,
    onQuizClick: (ExamScheduleItem) -> Unit,
    feedbackMessage: String?,
    modifier: Modifier = Modifier,
) {
    val today = remember { currentLocalDate() }
    var displayedYear by remember { mutableIntStateOf(today.year) }
    var displayedMonth by remember { mutableIntStateOf(today.monthNumberValue()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    val exams = (state as? ExamScheduleUiState.Success)?.exams.orEmpty()
    val examDatesInMonth = remember(exams, displayedYear, displayedMonth) {
        exams.mapNotNull { it.parsedDate() }
            .filter { it.year == displayedYear && it.monthNumberValue() == displayedMonth }
            .map { it.day }
            .toSet()
    }
    val filteredExams = remember(exams, selectedDate) {
        selectedDate?.let { selected ->
            exams.filter { it.parsedDate() == selected }
        } ?: exams
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuiketBrown50)
            .verticalScroll(rememberScrollState()),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = QuiketWhite,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 56.dp, bottom = 24.dp),
            ) {
                ExamTopBar(
                    title = "시험 일정",
                    onBackClick = onBackClick,
                    onEditModeClick = {
                        isEditMode = true
                        isDeleteMode = false
                    },
                    onDeleteModeClick = {
                        isDeleteMode = true
                        isEditMode = false
                    },
                )
                Spacer(modifier = Modifier.height(26.dp))
                MonthHeader(
                    year = displayedYear,
                    month = displayedMonth,
                    onPreviousClick = {
                        if (displayedMonth == 1) {
                            displayedYear -= 1
                            displayedMonth = 12
                        } else {
                            displayedMonth -= 1
                        }
                        selectedDate = null
                    },
                    onNextClick = {
                        if (displayedMonth == 12) {
                            displayedYear += 1
                            displayedMonth = 1
                        } else {
                            displayedMonth += 1
                        }
                        selectedDate = null
                    },
                )
                Spacer(modifier = Modifier.height(14.dp))
                ExamCalendar(
                    year = displayedYear,
                    month = displayedMonth,
                    today = today,
                    selectedDate = selectedDate,
                    examDays = examDatesInMonth,
                    onDateClick = { date ->
                        selectedDate = if (selectedDate == date) null else date
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = when {
                isEditMode -> "수정하고 싶은 시험을 선택해주세요"
                isDeleteMode -> "삭제하고 싶은 시험을 선택해주세요"
                else -> "내 시험"
            },
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (!feedbackMessage.isNullOrBlank()) {
            ExamFeedbackCard(
                message = feedbackMessage,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        when (state) {
            ExamScheduleUiState.Loading -> ExamMessageCard(
                message = "시험 일정을 불러오는 중이에요",
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            is ExamScheduleUiState.Error -> ExamMessageCard(
                message = state.message,
                buttonText = "다시 시도",
                onButtonClick = onRetryClick,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            is ExamScheduleUiState.Success -> {
                val isActionMode = isEditMode || isDeleteMode
                QuiketPrimaryButton(
                    text = "새 시험 일정 등록하기",
                    enabled = !isActionMode,
                    onClick = onAddExamClick,
                    containerColor = QuiketWhite,
                    contentColor = QuiketGray950,
                    disabledContainerColor = QuiketWhite,
                    disabledContentColor = QuiketGray300,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .then(
                            if (!isActionMode) {
                                Modifier.border(2.dp, QuiketBrown950, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            },
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (filteredExams.isEmpty()) {
                    ExamMessageCard(
                        message = if (selectedDate == null) {
                            "등록된 시험 일정이 없어요"
                        } else {
                            "이 날은 시험이 없어요"
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        filteredExams.forEach { exam ->
                            ExamScheduleCard(
                                exam = exam,
                                isEditMode = isEditMode,
                                isDeleteMode = isDeleteMode,
                                onEditClick = {
                                    isEditMode = false
                                    onEditExamClick(exam)
                                },
                                onDeleteClick = {
                                    isDeleteMode = false
                                    onDeleteExamClick(exam)
                                },
                                onQuizClick = { onQuizClick(exam) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun ExamTopBar(
    title: String,
    onBackClick: () -> Unit,
    onEditModeClick: () -> Unit,
    onDeleteModeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

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
                .clearAndSetSemantics { contentDescription = "뒤로" }
                .clickable(role = Role.Button, onClick = onBackClick),
        )
        Text(
            text = title,
            color = QuiketGray950,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center),
        )
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                painter = painterResource(Res.drawable.ic_detail_etc),
                contentDescription = "더보기",
                tint = QuiketGray700,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { showMoreMenu = true }
                    .padding(10.dp),
            )
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                containerColor = QuiketWhite,
                shape = RoundedCornerShape(12.dp),
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_detail_edit),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            text = "시험 수정",
                            color = QuiketGray950,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onEditModeClick()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_detail_remove),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            text = "시험 삭제",
                            color = QuiketNegative,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onDeleteModeClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    year: Int,
    month: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${year}년 ${month}월",
            color = QuiketGray950,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarArrow(text = "‹", contentDescription = "이전 달", onClick = onPreviousClick)
            CalendarArrow(text = "›", contentDescription = "다음 달", onClick = onNextClick)
        }
    }
}

@Composable
private fun CalendarArrow(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = QuiketGray700,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .clearAndSetSemantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun ExamCalendar(
    year: Int,
    month: Int,
    today: LocalDate,
    selectedDate: LocalDate?,
    examDays: Set<Int>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    val offset = firstDayOffset(year, month)
    val totalDays = daysInMonth(year, month)
    val totalCells = ((offset + totalDays + 6) / 7) * 7

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    color = QuiketGray400,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        repeat(totalCells / 7) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNumber = week * 7 + col - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..totalDays) {
                            val date = LocalDate(year, month.toMonth(), dayNumber)
                            val isSelected = selectedDate == date
                            val isToday = today == date
                            val hasExam = dayNumber in examDays
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> QuiketBrown950
                                                isToday -> QuiketBrown50
                                                else -> Color.Transparent
                                            },
                                        )
                                        .clickable(role = Role.Button) { onDateClick(date) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isSelected) QuiketWhite else QuiketGray950,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (hasExam) QuiketOrange500 else Color.Transparent),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamScheduleCard(
    exam: ExamScheduleItem,
    isEditMode: Boolean,
    isDeleteMode: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dDay = exam.resolvedDDay()
    val dDayText = dDay?.let {
        when {
            it > 0 -> "D-$it"
            it == 0 -> "D-Day"
            else -> "D+${-it}"
        }
    }.orEmpty()
    val urgent = dDay != null && dDay in 0..7

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                when {
                    isEditMode -> Modifier.clickable(role = Role.Button, onClick = onEditClick)
                    isDeleteMode -> Modifier.clickable(role = Role.Button, onClick = onDeleteClick)
                    else -> Modifier
                },
            ),
        color = QuiketWhite,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = exam.formattedDate(),
                    color = QuiketGray500,
                    style = MaterialTheme.typography.labelSmall,
                )
                when {
                    isEditMode -> Icon(
                        painter = painterResource(Res.drawable.ic_detail_edit),
                        contentDescription = "수정",
                        tint = QuiketGray600,
                        modifier = Modifier.size(24.dp),
                    )

                    isDeleteMode -> Icon(
                        painter = painterResource(Res.drawable.ic_detail_remove),
                        contentDescription = "삭제",
                        tint = QuiketGray600,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = exam.examName.ifBlank { exam.subjectName },
                    color = QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                if (dDayText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (urgent) QuiketNegative else QuiketBrown950)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = dDayText,
                            color = QuiketWhite,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(QuiketGray100),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onQuizClick)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "퀴즈 풀러 가기",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "›",
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun ExamFeedbackCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketWhite,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = message,
            color = QuiketGray700,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun ExamMessageCard(
    message: String,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketWhite,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                color = QuiketGray700,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
            if (buttonText != null) {
                QuiketPrimaryButton(
                    text = buttonText,
                    enabled = true,
                    onClick = onButtonClick,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun ExamEditorDialog(
    mode: ExamEditorMode,
    subjects: List<SubjectSummary>,
    registeredSubjectIds: Set<String>,
    isSaving: Boolean,
    onSave: (subjectId: String, examName: String, examDate: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val editingExam = (mode as? ExamEditorMode.Edit)?.exam
    val today = remember { currentLocalDate() }
    val initialDate = remember(mode) { editingExam?.parsedDate() }
    var selectedSubjectId by remember(mode) { mutableStateOf(editingExam?.subjectId) }
    var subjectExpanded by remember(mode) { mutableStateOf(editingExam == null) }
    var examName by remember(mode) { mutableStateOf(editingExam?.examName.orEmpty()) }
    var selectedDate by remember(mode) { mutableStateOf(initialDate) }
    var calendarYear by remember(mode) { mutableIntStateOf((initialDate ?: today).year) }
    var calendarMonth by remember(mode) { mutableIntStateOf((initialDate ?: today).monthNumberValue()) }
    var showCalendar by remember(mode) { mutableStateOf(false) }
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }
    val canSave = selectedSubjectId != null && selectedDate != null && !isSaving
    val isExamNameInputSuspended = subjectExpanded || showCalendar || isSaving
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dialogScope = rememberCoroutineScope()

    fun hideKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        hidePlatformKeyboard()
    }

    fun afterKeyboardDismiss(action: () -> Unit) {
        dialogScope.launch {
            hideKeyboard()
            delay(160L)
            hidePlatformKeyboard()
            action()
        }
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp),
            color = QuiketWhite,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (editingExam == null) "시험 일정 등록" else "시험 일정 수정",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ExamDialogLabel(text = "과목명", required = editingExam == null)
                    if (editingExam == null) {
                        ReadOnlyField(
                            text = selectedSubject?.name ?: "과목 선택하기",
                            isPlaceholder = selectedSubject == null,
                            onClick = {
                                afterKeyboardDismiss {
                                    subjectExpanded = !subjectExpanded
                                }
                            },
                        )
                        if (subjectExpanded) {
                            SubjectPickerList(
                                subjects = subjects,
                                selectedSubjectId = selectedSubjectId,
                                registeredSubjectIds = registeredSubjectIds,
                                onSelect = { subject ->
                                    selectedSubjectId = subject.id
                                    subjectExpanded = false
                                },
                            )
                        }
                    } else {
                        ReadOnlyField(
                            text = editingExam.subjectName,
                            isPlaceholder = false,
                            onClick = {},
                            enabled = false,
                        )
                    }

                    ExamDialogLabel(text = "시험명")
                    if (isExamNameInputSuspended) {
                        ReadOnlyField(
                            text = examName.ifBlank { "시험명을 입력해주세요" },
                            isPlaceholder = examName.isBlank(),
                            onClick = {},
                            enabled = false,
                        )
                    } else {
                        QuiketTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            hint = "시험명을 입력해주세요",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { hideKeyboard() }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    ExamDialogLabel(text = "시험 날짜", required = true)
                    ReadOnlyField(
                        text = selectedDate?.toIsoDate() ?: "날짜를 선택해주세요",
                        isPlaceholder = selectedDate == null,
                        onClick = {
                            afterKeyboardDismiss {
                                showCalendar = !showCalendar
                            }
                        },
                    )
                    if (showCalendar) {
                        DialogCalendar(
                            year = calendarYear,
                            month = calendarMonth,
                            selectedDate = selectedDate,
                            onPreviousClick = {
                                if (calendarMonth == 1) {
                                    calendarYear -= 1
                                    calendarMonth = 12
                                } else {
                                    calendarMonth -= 1
                                }
                            },
                            onNextClick = {
                                if (calendarMonth == 12) {
                                    calendarYear += 1
                                    calendarMonth = 1
                                } else {
                                    calendarMonth += 1
                                }
                            },
                            onDateClick = { date ->
                                selectedDate = date
                                showCalendar = false
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DialogActionButton(
                        text = "취소",
                        primary = false,
                        enabled = !isSaving,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    DialogActionButton(
                        text = if (isSaving) "저장 중" else "적용",
                        primary = true,
                        enabled = canSave,
                        onClick = {
                            val subjectId = selectedSubjectId ?: return@DialogActionButton
                            val examDate = selectedDate?.toIsoDate() ?: return@DialogActionButton
                            onSave(subjectId, examName, examDate)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteExamDialog(
    exam: ExamScheduleItem,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!isDeleting) onDismiss() }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = QuiketWhite,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "이 시험 일정을 삭제할까요?",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "${exam.subjectName}에 등록한 시험 정보가 삭제돼요.",
                    color = QuiketGray700,
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DialogActionButton(
                        text = "취소",
                        primary = false,
                        enabled = !isDeleting,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    DialogActionButton(
                        text = if (isDeleting) "삭제 중" else "삭제하기",
                        primary = true,
                        enabled = !isDeleting,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectPickerList(
    subjects: List<SubjectSummary>,
    selectedSubjectId: String?,
    registeredSubjectIds: Set<String>,
    onSelect: (SubjectSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (subjects.isEmpty()) {
            Text(
                text = "등록된 과목이 없어요",
                color = QuiketGray400,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
        } else {
            subjects.sortedBy { it.id in registeredSubjectIds }.forEach { subject ->
                val isRegistered = subject.id in registeredSubjectIds
                val isSelected = subject.id == selectedSubjectId
                SubjectPickerRow(
                    subject = subject,
                    selected = isSelected,
                    disabled = isRegistered,
                    onClick = { onSelect(subject) },
                )
            }
        }
    }
}

@Composable
private fun SubjectPickerRow(
    subject: SubjectSummary,
    selected: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) QuiketBrown950 else Color.Transparent
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (disabled) {
                    Modifier
                } else {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                },
            ),
        color = if (selected) QuiketBrown50 else QuiketGray50,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    color = if (disabled) QuiketGray400 else QuiketGray950,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
                if (disabled) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "이미 시험 등록됨",
                        color = QuiketGray400,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Text(
                text = if (selected) "선택됨" else "선택",
                color = if (disabled) QuiketGray300 else QuiketBrown950,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun DialogCalendar(
    year: Int,
    month: Int,
    selectedDate: LocalDate?,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            MonthHeader(
                year = year,
                month = month,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExamCalendar(
                year = year,
                month = month,
                today = currentLocalDate(),
                selectedDate = selectedDate,
                examDays = emptySet(),
                onDateClick = onDateClick,
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    text: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = QuiketGray50,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            color = if (isPlaceholder) QuiketGray400 else QuiketGray950,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun ExamDialogLabel(
    text: String,
    required: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (required) "$text *" else text,
        color = QuiketGray950,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
    )
}

@Composable
private fun DialogActionButton(
    text: String,
    primary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        primary && enabled -> QuiketBrown950
        primary -> QuiketGray100
        else -> QuiketWhite
    }
    val textColor = when {
        primary && enabled -> QuiketWhite
        primary -> QuiketGray300
        else -> QuiketBrown950
    }
    val borderColor = if (primary) Color.Transparent else QuiketBrown950

    Surface(
        modifier = modifier
            .height(48.dp)
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = textColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
