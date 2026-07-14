package com.f1.quiket.composeapp.home.presentation

import com.f1.quiket.composeapp.home.domain.model.HomeData
import com.f1.quiket.composeapp.home.domain.model.HomeUserSummary
import com.f1.quiket.composeapp.home.domain.model.SubjectExamSchedule
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeExamPresentationTest {
    @Test
    fun dDayLabelDistinguishesFutureTodayAndPast() {
        assertEquals("D-3", dDayLabel(3))
        assertEquals("D-Day", dDayLabel(0))
        assertEquals("D+2", dDayLabel(-2))
    }

    @Test
    fun homeExamsSortUpcomingBeforeRecentPastAndDropExpiredSchedules() {
        val home = homeData(
            dDayCards = listOf(
                schedule(id = "future-3", dDay = 3),
                schedule(id = "past-1", dDay = -1),
                schedule(id = "expired", dDay = -8),
            ),
            subjectSchedules = listOf(
                schedule(id = "today", dDay = 0),
                schedule(id = "future-1", dDay = 1),
                schedule(id = "past-7", dDay = -7),
            ),
        )

        val exams = home.toHomeExams()

        assertEquals(
            listOf("today", "future-1", "future-3", "past-1", "past-7"),
            exams.map { it.id },
        )
        assertEquals(5, exams.size)
        assertTrue(exams.first().isUrgent)
        assertFalse(exams.last().isUrgent)
    }

    @Test
    fun homeExamsDeduplicateSchedulesUsingDdayCardAsAuthoritativeSource() {
        val home = homeData(
            dDayCards = listOf(
                schedule(id = "shared", name = "디데이 카드", dDay = 5),
            ),
            subjectSchedules = listOf(
                schedule(id = "shared", name = "과목 일정", dDay = 1),
            ),
        )

        val exam = home.toHomeExams().single()

        assertEquals("디데이 카드", exam.name)
        assertEquals(5, exam.dDay)
    }

    @Test
    fun homeExamDateIsNormalizedForDisplay() {
        val home = homeData(
            dDayCards = listOf(
                schedule(id = "date", date = "2026.7.9", dDay = 1),
            ),
        )

        assertEquals("2026-07-09", home.toHomeExams().single().date)
    }
}

private fun homeData(
    dDayCards: List<SubjectExamSchedule>,
    subjectSchedules: List<SubjectExamSchedule> = emptyList(),
): HomeData = HomeData(
    user = HomeUserSummary(
        nickname = "테스터",
        dotoriBalance = 0,
        xpTotal = 0,
        currentLevel = 1,
        levelName = null,
    ),
    hero = null,
    dDayCards = dDayCards,
    subjects = subjectSchedules.map { examSchedule ->
        SubjectSummary(
            id = examSchedule.subjectId,
            name = examSchedule.examName,
            purpose = "exam",
            chapterCount = 0,
            partCount = 0,
            lastActivityAt = null,
            examSchedule = examSchedule,
        )
    },
    recentActivities = emptyList(),
)

private fun schedule(
    id: String,
    name: String = id,
    date: String = "2026-07-20",
    dDay: Int?,
): SubjectExamSchedule = SubjectExamSchedule(
    id = id,
    subjectId = "subject-$id",
    examName = name,
    examDate = date,
    dDay = dDay,
)
