package com.f1.quiket.composeapp.home.presentation

import com.f1.quiket.composeapp.home.HomeData
import com.f1.quiket.composeapp.home.SubjectExamSchedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal data class HomeExamUiModel(
    val id: String,
    val name: String,
    val date: String,
    val dDay: Int,
) {
    val dDayLabel: String
        get() = dDayLabel(dDay)

    val isUrgent: Boolean
        get() = dDay in 0..7
}

internal fun dDayLabel(dDay: Int): String = when {
    dDay > 0 -> "D-$dDay"
    dDay == 0 -> "D-Day"
    else -> "D+${-dDay}"
}

internal fun HomeData?.toHomeExams(): List<HomeExamUiModel> {
    val schedules = this?.dDayCards.orEmpty() + this?.subjects.orEmpty().mapNotNull { it.examSchedule }
    return schedules
        .distinctBy { schedule -> schedule.id }
        .mapNotNull { schedule ->
            val resolvedDDay = schedule.resolvedDDay() ?: return@mapNotNull null
            HomeExamUiModel(
                id = schedule.id,
                name = schedule.examName,
                date = schedule.examDate.toHomeDateLabel(),
                dDay = resolvedDDay,
            )
        }
        .filter { exam -> exam.dDay >= -7 }
        .withIndex()
        .sortedWith(
            Comparator { first, second ->
                val firstDay = first.value.dDay
                val secondDay = second.value.dDay
                val dayOrder = when {
                    firstDay >= 0 && secondDay >= 0 -> firstDay.compareTo(secondDay)
                    firstDay < 0 && secondDay < 0 -> secondDay.compareTo(firstDay)
                    firstDay >= 0 -> -1
                    else -> 1
                }
                if (dayOrder != 0) dayOrder else second.index - first.index
            },
        )
        .map { it.value }
        .take(5)
}

private fun SubjectExamSchedule.resolvedDDay(): Int? =
    dDay ?: parseHomeDate(examDate)
        ?.let { date -> date.toEpochDays() - currentHomeDate().toEpochDays() }
        ?.toInt()

private fun String.toHomeDateLabel(): String {
    val parsedDate = parseHomeDate(this) ?: return this
    return "${parsedDate.year}-${parsedDate.monthNumberValue().twoDigits()}-${parsedDate.day.twoDigits()}"
}

private fun parseHomeDate(raw: String): LocalDate? {
    val parts = raw.take(10).split("-", ".")
    if (parts.size < 3) return null
    return runCatching {
        LocalDate(
            year = parts[0].toInt(),
            month = parts[1].toInt().toHomeMonth(),
            day = parts[2].toInt(),
        )
    }.getOrNull()
}

private fun Int.toHomeMonth(): Month =
    Month.entries[this - 1]

private fun LocalDate.monthNumberValue(): Int =
    month.ordinal + 1

private fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()

@OptIn(ExperimentalTime::class)
private fun currentHomeDate(): LocalDate =
    Clock.System.todayIn(TimeZone.currentSystemDefault())
