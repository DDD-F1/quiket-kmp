package com.f1.quiket.composeapp.home.presentation

import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal sealed interface ExamScheduleUiState {
    data object Loading : ExamScheduleUiState
    data class Success(
        val exams: List<ExamScheduleItem>,
        val subjects: List<SubjectSummary>,
    ) : ExamScheduleUiState
    data class Error(val message: String) : ExamScheduleUiState
}

internal sealed interface ExamEditorMode {
    data object Add : ExamEditorMode
    data class Edit(val exam: ExamScheduleItem) : ExamEditorMode
}

internal data class ExamScheduleItem(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)

internal fun List<SubjectSummary>.toExamScheduleItems(): List<ExamScheduleItem> =
    mapNotNull { subject ->
        subject.examSchedule?.let { schedule ->
            ExamScheduleItem(
                id = schedule.id,
                subjectId = schedule.subjectId,
                subjectName = subject.name,
                examName = schedule.examName,
                examDate = schedule.examDate,
                dDay = schedule.dDay,
            )
        }
    }
        .filter { (it.resolvedDDay() ?: 0) >= 0 }
        .distinctBy { it.subjectId }
        .sortedBy { it.resolvedDDay() ?: Int.MAX_VALUE }

internal fun ExamScheduleItem.parsedDate(): LocalDate? =
    parseExamDate(examDate)

internal fun ExamScheduleItem.resolvedDDay(): Int? =
    dDay ?: parsedDate()?.let { (it.toEpochDays() - currentLocalDate().toEpochDays()).toInt() }

internal fun ExamScheduleItem.formattedDate(): String {
    val date = parsedDate() ?: return examDate
    return "${date.year}.${date.monthNumberValue().twoDigits()}." +
        "${date.day.twoDigits()} ${date.koreanDayOfWeek()}"
}

internal fun parseExamDate(raw: String): LocalDate? {
    val parts = raw.split("-", ".")
    if (parts.size < 3) return null
    return runCatching {
        LocalDate(
            year = parts[0].toInt(),
            month = parts[1].toInt().toMonth(),
            day = parts[2].toInt(),
        )
    }.getOrNull()
}

internal fun firstDayOffset(year: Int, month: Int): Int =
    LocalDate(year, month.toMonth(), 1).dayOfWeek.ordinal

internal fun daysInMonth(year: Int, month: Int): Int {
    val start = LocalDate(year, month.toMonth(), 1)
    val next = if (month == 12) {
        LocalDate(year + 1, Month.JANUARY, 1)
    } else {
        LocalDate(year, (month + 1).toMonth(), 1)
    }
    return (next.toEpochDays() - start.toEpochDays()).toInt()
}

internal fun LocalDate.koreanDayOfWeek(): String =
    listOf("월", "화", "수", "목", "금", "토", "일")[dayOfWeek.ordinal]

internal fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()

internal fun LocalDate.toIsoDate(): String =
    "${year}-${monthNumberValue().twoDigits()}-${day.twoDigits()}"

internal fun LocalDate.monthNumberValue(): Int =
    month.ordinal + 1

internal fun Int.toMonth(): Month =
    Month.entries[this - 1]

@OptIn(ExperimentalTime::class)
internal fun currentLocalDate(): LocalDate =
    Clock.System.todayIn(TimeZone.currentSystemDefault())
