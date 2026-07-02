package com.f1.quiket.feature.floating.presentation.screen.subjectdetail

import com.f1.quiket.feature.floating.domain.model.ChineseTestType
import com.f1.quiket.feature.floating.domain.model.CivilServantGrade
import com.f1.quiket.feature.floating.domain.model.CivilServantSeries
import com.f1.quiket.feature.floating.domain.model.CourseType
import com.f1.quiket.feature.floating.domain.model.EnglishTestType
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.FamiliarityLevel
import com.f1.quiket.feature.floating.domain.model.JapaneseTestType
import com.f1.quiket.feature.floating.domain.model.LanguageType
import com.f1.quiket.feature.floating.domain.model.MiddleHighCurriculum
import com.f1.quiket.feature.floating.domain.model.MiddleHighSubjectType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.UsagePurpose
import com.f1.quiket.feature.floating.domain.model.examTypeFromBackendValue
import java.util.Calendar
import java.util.concurrent.TimeUnit

internal data class SubjectLabels(val h1: String, val h2: String, val h3: String)

internal fun calcDDay(dateStr: String): String =
    runCatching {
        val parts = dateStr.take(10).split(".", "-")
        val exam = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val diff = TimeUnit.MILLISECONDS.toDays(exam.timeInMillis - today.timeInMillis)
        when {
            diff > 0L -> "D-$diff"
            diff == 0L -> "D-Day"
            else -> "D+${-diff}"
        }
    }.getOrDefault("D-?")

private fun purposeToKorean(purpose: String): String = when (purpose.lowercase()) {
    "exam" -> StudyPurpose.EXAM.title
    "review", "self_study" -> StudyPurpose.SELF_STUDY.title
    "other" -> StudyPurpose.OTHER.title
    else -> StudyPurpose.entries.find { it.name.equals(purpose, ignoreCase = true) }?.title ?: purpose
}

private fun <T : Enum<T>> Iterable<T>.findByName(value: String): T? =
    find { it.name.equals(value, ignoreCase = true) }

internal fun SubjectDetail.toLabels(): SubjectLabels {
    val h1 = purposeToKorean(purpose)
    return when (purpose.lowercase()) {
        "exam" -> {
            val e = examDetail
            val examType = e?.examType?.let { examTypeFromBackendValue(it) }
            val h2 = examType?.label ?: ""
            val h3: String = if (e == null) "" else when (examType) {
                ExamType.UNIVERSITY -> listOfNotNull(
                    e.univMajorName,
                    e.univCourseType?.let { CourseType.entries.findByName(it)?.label },
                ).joinToString(" · ")

                ExamType.MIDDLE_HIGH -> listOfNotNull(
                    e.mhGrade?.let { MiddleHighCurriculum.entries.findByName(it)?.label ?: it },
                    e.mhSubjectType?.let { s ->
                        MiddleHighSubjectType.entries.findByName(s)?.label?.takeIf { it != "직접 입력" } ?: s
                    },
                ).joinToString(" · ")

                ExamType.CERTIFICATE -> e.certificateName ?: ""

                ExamType.CIVIL_SERVICE -> listOfNotNull(
                    e.civilSeries?.let { CivilServantSeries.entries.findByName(it)?.label ?: it },
                    e.civilRank?.let { CivilServantGrade.entries.findByName(it)?.label ?: it },
                ).joinToString(" · ")

                ExamType.LANGUAGE -> listOfNotNull(
                    e.langType?.let { LanguageType.entries.findByName(it)?.label ?: it },
                    e.langExamName?.let { name ->
                        val lower = name.lowercase()
                        (EnglishTestType.entries.find { it.name.lowercase() == lower }?.label
                            ?: JapaneseTestType.entries.find { it.name.lowercase() == lower }?.label
                            ?: ChineseTestType.entries.find { it.name.lowercase() == lower }?.label
                            ?: name).takeIf { it != "직접 입력" }
                    },
                ).joinToString(" · ")

                ExamType.OTHER_EXAM -> e.otherExamName ?: ""
                null -> ""
            }
            SubjectLabels(h1, h2, h3)
        }

        "review", "self_study" -> {
            val r = reviewDetail
            SubjectLabels(
                h1 = h1,
                h2 = r?.field?.let { StudyField.entries.findByName(it)?.label ?: it } ?: "",
                h3 = r?.studyLevel?.let { FamiliarityLevel.entries.findByName(it)?.label ?: it } ?: "",
            )
        }

        "other" -> SubjectLabels(
            h1 = h1,
            h2 = otherDetail?.usagePurpose?.let { UsagePurpose.entries.findByName(it)?.title ?: it } ?: "",
            h3 = "",
        )

        else -> SubjectLabels(h1, "", "")
    }
}