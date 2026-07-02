package com.f1.quiket.feature.home.presentation.screen

import com.f1.quiket.feature.home.presentation.contract.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.component.QuiketPrimaryButton
import com.f1.quiket.core.designsystem.theme.Black
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray600
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.Orange500
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.core.designsystem.R as DesignR
import com.f1.quiket.feature.home.domain.model.SubjectExamSchedule
import java.util.Calendar

private val koreanDays = listOf("월", "화", "수", "목", "금", "토", "일")

/** "2026-06-18" or "2026.06.18" → Triple(2026, 6, 18) */
private fun parseExamDate(examDate: String): Triple<Int, Int, Int>? = runCatching {
    val p = examDate.split("-", ".")
    Triple(p[0].toInt(), p[1].toInt(), p[2].toInt())
}.getOrNull()

/** DayOfWeek offset: 0=월 … 6=일 (Calendar.SUNDAY=1 → 6, MONDAY=2 → 0) */
private fun firstDayOffset(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
}

private fun daysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun koreanDayOfWeek(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, day)
    return koreanDays[(cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7]
}

@Composable
fun ExamScheduleScreen(
    uiState: ExamScheduleState,
    onBackClick: () -> Unit,
    onAddExamClick: () -> Unit,
    onEditExamItemClick: (SubjectExamSchedule) -> Unit = {},
    onDeleteExamItemClick: (SubjectExamSchedule) -> Unit = {},
    onQuizClick: (subjectId: String) -> Unit,
) {
    var isEditMode by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    val now = remember { Calendar.getInstance() }
    val todayYear = now.get(Calendar.YEAR)
    val todayMonth = now.get(Calendar.MONTH) + 1
    val todayDay = now.get(Calendar.DAY_OF_MONTH)

    var displayedYear by remember { mutableIntStateOf(todayYear) }
    var displayedMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedYear by remember { mutableIntStateOf(-1) }
    var selectedMonth by remember { mutableIntStateOf(-1) }
    var selectedDay by remember { mutableIntStateOf(-1) }

    val examDaysInMonth = remember(uiState.exams, displayedYear, displayedMonth) {
        uiState.exams
            .mapNotNull { parseExamDate(it.examDate) }
            .filter { it.first == displayedYear && it.second == displayedMonth }
            .map { it.third }
            .toSet()
    }

    val filteredExams = remember(uiState.exams, selectedYear, selectedMonth, selectedDay) {
        if (selectedDay == -1) {
            uiState.exams
        } else {
            uiState.exams.filter { exam ->
                parseExamDate(exam.examDate) == Triple(selectedYear, selectedMonth, selectedDay)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brown50)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── White header card (status bar + topbar + calendar) ─────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        ) {
            Column {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_topbar_back),
                            contentDescription = "뒤로가기",
                            tint = Gray700,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "시험 일정",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Gray950,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        var showMoreMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_detail_etc),
                                contentDescription = "더보기",
                                tint = Gray700,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            containerColor = White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(4.dp),
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(DesignR.drawable.ic_detail_edit),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                    )
                                },
                                text = {
                                    Text(
                                        text = "시험 수정",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        color = Gray950,
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    isEditMode = true
                                },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(DesignR.drawable.ic_detail_remove),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                    )
                                },
                                text = {
                                    Text(
                                        text = "시험 삭제",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Normal,
                                        ),
                                        color = Negative,
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    isDeleteMode = true
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // Month navigation: title left, arrows right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${displayedYear}년 ${displayedMonth}월",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Gray950,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_common_back),
                                contentDescription = "이전 달",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        if (displayedMonth == 1) {
                                            displayedYear--
                                            displayedMonth = 12
                                        } else {
                                            displayedMonth--
                                        }
                                    },
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_common_next),
                                contentDescription = "다음 달",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        if (displayedMonth == 12) {
                                            displayedYear++
                                            displayedMonth = 1
                                        } else {
                                            displayedMonth++
                                        }
                                    },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Day-of-week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        koreanDays.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray400,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar grid
                    val offset = firstDayOffset(displayedYear, displayedMonth)
                    val totalDays = daysInMonth(displayedYear, displayedMonth)
                    val totalCells = ((offset + totalDays + 6) / 7) * 7

                    Column {
                        (0 until totalCells / 7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                (0 until 7).forEach { col ->
                                    val day = week * 7 + col - offset + 1
                                    val isToday = day == todayDay &&
                                            displayedYear == todayYear &&
                                            displayedMonth == todayMonth
                                    val isSelected = day == selectedDay &&
                                            displayedYear == selectedYear &&
                                            displayedMonth == selectedMonth
                                    val hasExam = day in examDaysInMonth

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        if (day in 1..totalDays) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> Brown950
                                                            isToday -> Brown50
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() },
                                                    ) {
                                                        if (selectedDay == day && selectedYear == displayedYear && selectedMonth == displayedMonth) {
                                                            selectedYear = -1
                                                            selectedMonth = -1
                                                            selectedDay = -1
                                                        } else {
                                                            selectedYear = displayedYear
                                                            selectedMonth = displayedMonth
                                                            selectedDay = day
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) White else Black,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            if (hasExam) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Orange500)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(6.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ── Brown50 area below ────────────────────────────────────────
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when {
                isEditMode -> "수정하고 싶은 시험을 선택해주세요"
                isDeleteMode -> "삭제하고 싶은 시험을 선택해주세요"
                else -> "내 시험"
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Gray950,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedDay != -1 && filteredExams.isEmpty()) {
            NoExamCard(
                onAddExamClick = onAddExamClick,
                modifier = Modifier.padding(12.dp),
            )
        } else {
            // Add exam button
            val isActionMode = isEditMode || isDeleteMode
            QuiketPrimaryButton(
                text = "새 시험 일정 등록하기",
                enabled = !isActionMode,
                onClick = onAddExamClick,
                containerColor = White,
                contentColor = Gray950,
                modifier = Modifier
                    .padding(12.dp)
                    .then(
                        if (!isActionMode) Modifier.border(2.dp, Brown950, RoundedCornerShape(12.dp))
                        else Modifier
                    )
            )

            filteredExams.forEach { exam ->
                ExamScheduleListItem(
                    exam = exam,
                    isEditMode = isEditMode,
                    isDeleteMode = isDeleteMode,
                    onEditClick = {
                        isEditMode = false
                        onEditExamItemClick(exam)
                    },
                    onDeleteClick = {
                        isDeleteMode = false
                        onDeleteExamItemClick(exam)
                    },
                    onQuizClick = { onQuizClick(exam.subjectId) },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NoExamCard(
    onAddExamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "이 날은 시험이 없어요.\n새로운 시험 일정이 있다면 등록해 보아요",
            style = MaterialTheme.typography.bodySmall,
            color = Gray700,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(DesignR.drawable.ic_no_exam),
            contentDescription = null,
            modifier = Modifier.size(296.dp, 190.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        QuiketPrimaryButton(
            text = "새 시험 일정 등록하기",
            enabled = true,
            onClick = onAddExamClick,
            containerColor = White,
            contentColor = Gray950,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Brown950, RoundedCornerShape(12.dp))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun NoExamCardPreview() {
    QuiketTheme {
        NoExamCard(
            onAddExamClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun ExamScheduleListItem(
    exam: SubjectExamSchedule,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    isDeleteMode: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    val formattedDate = remember(exam.examDate) {
        runCatching {
            val (y, m, d) = parseExamDate(exam.examDate)!!
            val dotDate = "%04d.%02d.%02d".format(y, m, d)
            "$dotDate ${koreanDayOfWeek(y, m, d)}"
        }.getOrElse { exam.examDate }
    }

    val dDayInt = remember(exam.dDay, exam.examDate) {
        exam.dDay ?: runCatching {
            val (y, m, d) = parseExamDate(exam.examDate)!!
            val exam2 = Calendar.getInstance().apply {
                set(y, m - 1, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            java.util.concurrent.TimeUnit.MILLISECONDS
                .toDays(exam2.timeInMillis - today.timeInMillis).toInt()
        }.getOrNull()
    }
    val dDayText = dDayInt?.let { d ->
        when {
            d > 0 -> "D-$d"
            d == 0 -> "D-Day"
            else -> "D+${-d}"
        }
    }.orEmpty()
    val isUrgent = (dDayInt ?: Int.MAX_VALUE) in 0..7

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .then(
                when {
                    isEditMode -> Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onEditClick() }
                    isDeleteMode -> Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onDeleteClick() }
                    else -> Modifier
                }
            )
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                )
                if (isEditMode) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_detail_edit),
                        contentDescription = "수정",
                        tint = Gray600,
                        modifier = Modifier.size(24.dp),
                    )
                } else if (isDeleteMode) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_detail_remove),
                        contentDescription = "삭제",
                        tint = Gray600,
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
                    text = exam.examName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Gray950,
                    modifier = Modifier.weight(1f),
                )
                if (dDayText.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isUrgent) Negative else Brown950)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = dDayText,
                            style = MaterialTheme.typography.labelMedium,
                            color = White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Gray100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "퀴즈 풀러 가기",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Gray600,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onQuizClick() },
                )
                Icon(
                    painterResource(DesignR.drawable.ic_common_next),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Gray600
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamScheduleScreenPreview() {
    QuiketTheme {
        ExamScheduleScreen(
            uiState = ExamScheduleState(
                isLoading = false,
                exams = listOf(
                    SubjectExamSchedule(
                        id = "1",
                        subjectId = "s1",
                        examName = "SQLD 자격증 시험",
                        examDate = "2026.06.18",
                        dDay = 20,
                    ),
                    SubjectExamSchedule(
                        id = "2",
                        subjectId = "s2",
                        examName = "정보처리기사",
                        examDate = "2026.06.28",
                        dDay = 3,
                    ),
                ),
            ),
            onBackClick = {},
            onAddExamClick = {},
            onQuizClick = {},
        )
    }
}