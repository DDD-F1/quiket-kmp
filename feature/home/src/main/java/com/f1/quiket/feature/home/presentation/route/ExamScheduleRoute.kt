package com.f1.quiket.feature.home.presentation.route

import com.f1.quiket.feature.home.presentation.contract.*
import com.f1.quiket.feature.home.presentation.viewmodel.*
import com.f1.quiket.feature.home.presentation.screen.*

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.SubjectExamSchedule
import java.util.Calendar
import com.f1.quiket.core.designsystem.R as DesignR

@Composable
fun ExamScheduleRoute(
    viewModel: ExamScheduleViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onQuizClick: (subjectId: String) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddExamDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<SubjectExamSchedule?>(null) }
    var deletingExam by remember { mutableStateOf<SubjectExamSchedule?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExamScheduleSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()

                ExamScheduleSideEffect.ShowAddExamDialog ->
                    showAddExamDialog = true

                ExamScheduleSideEffect.ExamRegistered -> {
                    showAddExamDialog = false
                    editingExam = null
                }

                ExamScheduleSideEffect.ExamDeleted ->
                    deletingExam = null
            }
        }
    }

    ExamScheduleScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddExamClick = { viewModel.onIntent(ExamScheduleIntent.AddExamClick) },
        onEditExamItemClick = { exam -> editingExam = exam },
        onDeleteExamItemClick = { exam -> deletingExam = exam },
        onQuizClick = onQuizClick,
    )

    if (showAddExamDialog) {
        val registeredSubjectIds = remember(uiState.exams) {
            uiState.exams.map { it.subjectId }.toSet()
        }
        AddExamFromScheduleDialog(
            subjects = uiState.subjects,
            registeredSubjectIds = registeredSubjectIds,
            onRegister = { subjectId, examName, examDate ->
                viewModel.onIntent(ExamScheduleIntent.RegisterExam(subjectId, examName, examDate))
            },
            onDismiss = { showAddExamDialog = false },
        )
    }

    editingExam?.let { exam ->
        val subjectName = remember(uiState.subjects, exam.subjectId) {
            uiState.subjects.firstOrNull { it.id == exam.subjectId }?.name ?: ""
        }
        EditExamFromScheduleDialog(
            subjectName = subjectName,
            initialExamName = exam.examName,
            initialExamDate = exam.examDate,
            onUpdate = { examName, examDate ->
                viewModel.onIntent(
                    ExamScheduleIntent.RegisterExam(
                        exam.subjectId,
                        examName,
                        examDate
                    )
                )
            },
            onDismiss = { editingExam = null },
        )
    }

    deletingExam?.let { exam ->
        DeleteExamConfirmDialog(
            onConfirm = { viewModel.onIntent(ExamScheduleIntent.DeleteExam(exam.subjectId)) },
            onDismiss = { deletingExam = null },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AddExamFromScheduleDialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExamFromScheduleDialog(
    subjects: List<QuizSubjectSummary>,
    registeredSubjectIds: Set<String>,
    onRegister: (subjectId: String, examName: String, examDate: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedSubject by remember { mutableStateOf<QuizSubjectSummary?>(null) }
    var examName by remember { mutableStateOf("") }
    var confirmedDateStr by remember { mutableStateOf("") }
    var showSubjectPicker by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }

    val now = remember { Calendar.getInstance() }
    var calYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var calMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val isApplyEnabled = selectedSubject != null && confirmedDateStr.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "시험 일정 등록",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (!showCalendar) {
                    // ── 과목 선택 ──────────────────────────────────────────
                    ExamDialogFieldLabel("과목명 *")
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedSubject == null) {
                        Button(
                            onClick = { showSubjectPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown950,
                                contentColor = White,
                            ),
                        ) {
                            Text(
                                "과목 선택하기",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            BaseTextField(
                                value = selectedSubject!!.name,
                                onValueChange = {},
                                hint = "",
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(DesignR.drawable.ic_detail_edit),
                                        contentDescription = "과목 변경",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { showSubjectPicker = true },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 시험명 ─────────────────────────────────────────────
                    ExamDialogFieldLabel("시험명")
                    Spacer(modifier = Modifier.height(6.dp))
                    BaseTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        hint = "시험명을 입력해주세요",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 시험 날짜 ──────────────────────────────────────────
                    ExamDialogFieldLabel("시험 날짜 *")
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BaseTextField(
                            value = confirmedDateStr,
                            onValueChange = {},
                            hint = "날짜를 선택해주세요",
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(DesignR.drawable.ic_detail_date),
                                    contentDescription = "캘린더",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { showCalendar = true },
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                            border = BorderStroke(2.dp, Brown950)
                        ) {
                            Text(
                                "취소", style = MaterialTheme.typography.bodySmall,
                                color = Brown950
                            )
                        }
                        Button(
                            onClick = {
                                onRegister(
                                    selectedSubject!!.id,
                                    examName,
                                    confirmedDateStr
                                )
                            },
                            enabled = isApplyEnabled,
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
                } else {
                    // ── 캘린더 뷰 ─────────────────────────────────────────
                    ExamCalendarView(
                        year = calYear,
                        month = calMonth,
                        selectedDay = selectedDay,
                        onPrevMonth = {
                            if (calMonth == 1) {
                                calYear--; calMonth = 12
                            } else calMonth--
                            selectedDay = null
                        },
                        onNextMonth = {
                            if (calMonth == 12) {
                                calYear++; calMonth = 1
                            } else calMonth++
                            selectedDay = null
                        },
                        onDaySelect = { selectedDay = it },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { selectedDay = null }) {
                            Text(
                                "초기화",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showCalendar = false }) {
                            Text(
                                "취소",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                selectedDay?.let { day ->
                                    confirmedDateStr = "%d-%02d-%02d".format(calYear, calMonth, day)
                                }
                                showCalendar = false
                            },
                            enabled = selectedDay != null,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown950,
                                contentColor = White,
                                disabledContainerColor = Gray300,
                                disabledContentColor = White,
                            ),
                        ) {
                            Text("확인", style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
    }

    // BottomSheet는 Dialog 밖에서 렌더링 (z-order 보장)
    if (showSubjectPicker) {
        SubjectPickerSheet(
            subjects = subjects,
            registeredSubjectIds = registeredSubjectIds,
            selectedSubjectId = selectedSubject?.id,
            onSelect = { subject ->
                selectedSubject = subject
                showSubjectPicker = false
            },
            onDismiss = { showSubjectPicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DeleteExamConfirmDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeleteExamConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "이 시험 일정을 삭제할까요?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Black,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "등록한 시험 정보가 삭제돼요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray700,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                        border = BorderStroke(2.dp, Brown950),
                    ) {
                        Text(
                            "취소", style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold),
                            color = Brown950
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown950,
                            contentColor = White,
                            disabledContainerColor = Gray300,
                            disabledContentColor = White,
                        ),
                    ) {
                        Text("삭제하기", style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EditExamFromScheduleDialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExamFromScheduleDialog(
    subjectName: String,
    initialExamName: String,
    initialExamDate: String,
    onUpdate: (examName: String, examDate: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var examName by remember { mutableStateOf(initialExamName) }
    var confirmedDateStr by remember { mutableStateOf(initialExamDate) }
    var showCalendar by remember { mutableStateOf(false) }

    val now = remember { Calendar.getInstance() }
    var calYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var calMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val isApplyEnabled = confirmedDateStr.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "시험 일정 수정",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (!showCalendar) {
                    // ── 과목명 (고정) ────────────────────────────────────────
                    ExamDialogFieldLabel("과목명")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gray100)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    ) {
                        Text(
                            text = subjectName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray950,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 시험명 ─────────────────────────────────────────────
                    ExamDialogFieldLabel("시험명")
                    Spacer(modifier = Modifier.height(6.dp))
                    BaseTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        hint = "시험명을 입력해주세요",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 시험 날짜 ──────────────────────────────────────────
                    ExamDialogFieldLabel("시험 날짜 *")
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BaseTextField(
                            value = confirmedDateStr,
                            onValueChange = {},
                            hint = "날짜를 선택해주세요",
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(DesignR.drawable.ic_detail_date),
                                    contentDescription = "캘린더",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { showCalendar = true },
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                            border = BorderStroke(2.dp, Brown950),
                        ) {
                            Text(
                                "취소", style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold),
                                color = Brown950
                            )
                        }
                        Button(
                            onClick = { onUpdate(examName, confirmedDateStr) },
                            enabled = isApplyEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown950,
                                contentColor = White,
                                disabledContainerColor = Gray300,
                                disabledContentColor = White,
                            ),
                        ) {
                            Text("적용", style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold))
                        }
                    }
                } else {
                    // ── 캘린더 뷰 ─────────────────────────────────────────
                    ExamCalendarView(
                        year = calYear,
                        month = calMonth,
                        selectedDay = selectedDay,
                        onPrevMonth = {
                            if (calMonth == 1) {
                                calYear--; calMonth = 12
                            } else calMonth--
                            selectedDay = null
                        },
                        onNextMonth = {
                            if (calMonth == 12) {
                                calYear++; calMonth = 1
                            } else calMonth++
                            selectedDay = null
                        },
                        onDaySelect = { selectedDay = it },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { selectedDay = null }) {
                            Text(
                                "초기화",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showCalendar = false }) {
                            Text(
                                "취소",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                selectedDay?.let { day ->
                                    confirmedDateStr = "%d-%02d-%02d".format(calYear, calMonth, day)
                                }
                                showCalendar = false
                            },
                            enabled = selectedDay != null,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brown950,
                                contentColor = White,
                                disabledContainerColor = Gray300,
                                disabledContentColor = White,
                            ),
                        ) {
                            Text("확인", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SubjectPickerSheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectPickerSheet(
    subjects: List<QuizSubjectSummary>,
    registeredSubjectIds: Set<String>,
    selectedSubjectId: String?,
    onSelect: (QuizSubjectSummary) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = "과목 선택하기",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Gray950,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "시험과 관련 있는 과목을 선택해주세요",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
            Spacer(modifier = Modifier.height(16.dp))

            val sortedSubjects = remember(subjects, registeredSubjectIds) {
                subjects.sortedBy { it.id in registeredSubjectIds }
            }

            if (sortedSubjects.isEmpty()) {
                Text(
                    text = "등록된 과목이 없어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray400,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                sortedSubjects.forEach { subject ->
                    val isRegistered = subject.id in registeredSubjectIds
                    val isSelected = subject.id == selectedSubjectId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !isRegistered,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onSelect(subject) }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (isRegistered) Gray400 else Gray950,
                            )
                            if (isRegistered) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "이미 시험 등록됨",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray400,
                                )
                            }
                        }
                        RadioButton(
                            selected = isSelected,
                            enabled = !isRegistered,
                            onClick = if (!isRegistered) {
                                { onSelect(subject) }
                            } else null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Brown950,
                                unselectedColor = Gray300,
                                disabledSelectedColor = Gray300,
                                disabledUnselectedColor = Gray300,
                            ),
                        )
                    }
                    HorizontalDivider(color = Gray100, thickness = 1.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CalendarView (다이얼로그용)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExamCalendarView(
    year: Int,
    month: Int,
    selectedDay: Int?,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelect: (Int) -> Unit,
) {
    val cal = remember(year, month) {
        Calendar.getInstance().apply { set(year, month - 1, 1) }
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayCol = cal.get(Calendar.DAY_OF_WEEK) - 1

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_common_back),
                    contentDescription = "이전 달",
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = "${year}년 ${month}월",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Gray950
                ),
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_common_next),
                    contentDescription = "다음 달",
                    tint = Color.Unspecified,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        val rows = ((firstDayCol + daysInMonth) + 6) / 7
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNumber = row * 7 + col - firstDayCol + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = dayNumber == selectedDay
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Brown950 else Color.Transparent)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { onDaySelect(dayNumber) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) White else Gray950,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    ),
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
private fun ExamDialogFieldLabel(text: String) {
    val annotated = buildAnnotatedString {
        append(text.replace(" *", "").replace("*", ""))
        if (text.contains("*")) {
            withStyle(SpanStyle(color = Negative)) { append(" *") }
        }
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Gray950,
        ),
    )
}