package com.f1.quiket.feature.floating.presentation.screen.subjectdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.f1.quiket.core.designsystem.component.BaseTextField
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import java.util.Calendar

@Composable
internal fun AddTestCalendarDialog(
    subjectName: String,
    hasExistingSchedule: Boolean = false,
    initialExamName: String = "",
    initialDate: String = "",
    onDismiss: () -> Unit,
    onApply: (examName: String, date: String) -> Unit,
    onDelete: () -> Unit = {},
) {
    var examName by remember { mutableStateOf(initialExamName) }
    var showCalendar by remember { mutableStateOf(false) }

    val now = remember { Calendar.getInstance() }
    val parsedDate = remember(initialDate) {
        runCatching {
            val parts = initialDate.split("-")
            Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull()
    }
    var calYear by remember { mutableIntStateOf(parsedDate?.first ?: now.get(Calendar.YEAR)) }
    var calMonth by remember { mutableIntStateOf(parsedDate?.second ?: (now.get(Calendar.MONTH) + 1)) }
    var selectedDay by remember { mutableStateOf(parsedDate?.third) }
    var confirmedDateStr by remember { mutableStateOf(initialDate) }

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
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (!showCalendar) {
                    DialogFieldLabel(text = "과목명 *")
                    Spacer(modifier = Modifier.height(6.dp))
                    BaseTextField(
                        value = subjectName,
                        onValueChange = {},
                        hint = "",
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DialogFieldLabel(text = "시험명")
                    Spacer(modifier = Modifier.height(6.dp))
                    BaseTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        hint = "시험명을 입력해주세요",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    DialogFieldLabel(text = "시험 날짜 *")
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BaseTextField(
                            value = confirmedDateStr,
                            onValueChange = {},
                            hint = "YYYY.MM.DD",
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_detail_date),
                                    contentDescription = "캘린더",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { showCalendar = true },
                                )
                        )
                    }

                    if (hasExistingSchedule) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                            Text("일정 삭제", style = MaterialTheme.typography.bodySmall.copy(color = Negative))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogButtons(
                        isApplyEnabled = confirmedDateStr.isNotBlank(),
                        onDismiss = onDismiss,
                        onApply = { onApply(examName, confirmedDateStr) },
                    )
                } else {
                    CalendarView(
                        year = calYear,
                        month = calMonth,
                        selectedDay = selectedDay,
                        onPrevMonth = {
                            if (calMonth == 1) { calYear--; calMonth = 12 } else calMonth--
                            selectedDay = null
                        },
                        onNextMonth = {
                            if (calMonth == 12) { calYear++; calMonth = 1 } else calMonth++
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
                            Text("초기화", style = MaterialTheme.typography.bodySmall.copy(color = Gray500))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showCalendar = false }) {
                            Text("취소", style = MaterialTheme.typography.bodySmall.copy(color = Gray500))
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

@Composable
internal fun EditSubjectNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "과목명 수정",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold, color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                DialogFieldLabel(text = "과목명 *")
                Spacer(modifier = Modifier.height(6.dp))
                BaseTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = "과목명을 입력해주세요",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                DialogButtons(
                    isApplyEnabled = name.isNotBlank(),
                    onDismiss = onDismiss,
                    onApply = { onApply(name) },
                )
            }
        }
    }
}

@Composable
internal fun EditChapterNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "챕터명 수정",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold, color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                DialogFieldLabel(text = "챕터명 *")
                Spacer(modifier = Modifier.height(6.dp))
                BaseTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = "챕터명을 입력해주세요",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                DialogButtons(
                    isApplyEnabled = name.isNotBlank(),
                    onDismiss = onDismiss,
                    onApply = { onApply(name) },
                )
            }
        }
    }
}

@Composable
internal fun DeleteChapterDialog(
    chapterNumber: Int,
    chapterName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "'챕터 $chapterNumber $chapterName'을 삭제할까요?",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold, color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "챕터가 영구적으로 삭제돼요.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Gray700),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                        border = BorderStroke(2.dp, Brown950),
                    ) {
                        Text("닫기", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Brown950)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown950, contentColor = White),
                    ) {
                        Text("삭제하기", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeleteSubjectDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "과목 '$subjectName'을 삭제할까요?",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold, color = Gray950,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "과목이 영구적으로 삭제돼요.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Gray700),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                        border = BorderStroke(2.dp, Brown950),
                    ) {
                        Text("닫기", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Brown950)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown950, contentColor = White),
                    ) {
                        Text("삭제하기", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogFieldLabel(text: String) {
    val annotatedText = buildAnnotatedString {
        append(text.replace("*", ""))
        if (text.contains("*")) {
            withStyle(style = SpanStyle(color = Negative)) { append("*") }
        }
    }
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Brown950),
    )
}

@Composable
private fun DialogButtons(
    isApplyEnabled: Boolean,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
            border = BorderStroke(2.dp, Brown950),
        ) {
            Text("취소", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Brown950)
        }
        Button(
            onClick = onApply,
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
            Text("적용", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun CalendarView(
    year: Int,
    month: Int,
    selectedDay: Int?,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelect: (Int) -> Unit,
) {
    val now = remember { Calendar.getInstance() }
    val todayYear = now.get(Calendar.YEAR)
    val todayMonth = now.get(Calendar.MONTH) + 1
    val todayDay = now.get(Calendar.DAY_OF_MONTH)

    val cal = remember(year, month) {
        Calendar.getInstance().apply { set(year, month - 1, 1) }
    }
    val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val offset = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${year}년 ${month}월",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Gray950,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_common_back),
                    contentDescription = "이전 달",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPrevMonth() },
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_common_next),
                    contentDescription = "다음 달",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNextMonth() },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray400,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = ((offset + totalDays + 6) / 7) * 7
        Column {
            (0 until totalCells / 7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    (0 until 7).forEach { col ->
                        val day = week * 7 + col - offset + 1
                        val isToday = day == todayDay && year == todayYear && month == todayMonth
                        val isSelected = day == selectedDay

                        Column(
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
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
                                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDaySelect(day) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) White else Gray950,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Spacer(modifier = Modifier.size(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}