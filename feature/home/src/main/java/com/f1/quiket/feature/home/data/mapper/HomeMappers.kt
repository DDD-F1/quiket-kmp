package com.f1.quiket.feature.home.data.mapper

import com.f1.quiket.feature.home.data.remote.HomeDataResponse
import com.f1.quiket.feature.home.data.remote.HomeHeroResponse
import com.f1.quiket.feature.home.data.remote.HomeUserSummaryResponse
import com.f1.quiket.feature.home.data.remote.RecentActivityResponse
import com.f1.quiket.feature.home.data.remote.SubjectExamScheduleResponse
import com.f1.quiket.feature.home.data.remote.SubjectSummaryResponse
import com.f1.quiket.feature.home.domain.model.HomeData
import com.f1.quiket.feature.home.domain.model.HomeHero
import com.f1.quiket.feature.home.domain.model.HomeUserSummary
import com.f1.quiket.feature.home.domain.model.QuizSubjectSummary
import com.f1.quiket.feature.home.domain.model.RecentActivity
import com.f1.quiket.feature.home.domain.model.SubjectExamSchedule

fun HomeDataResponse.toDomain(): HomeData = HomeData(
    user = user.toDomain(),
    hero = hero?.toDomain(),
    dDayCards = dDayCards.map { schedule -> schedule.toDomain() },
    subjects = subjects.map { subject -> subject.toDomain() },
    recentActivities = recentActivities.map { activity -> activity.toDomain() },
)

fun HomeHeroResponse.toDomain(): HomeHero = HomeHero(
    hasActiveQuiz = hasActiveQuiz,
    activeQuiz = activeQuiz?.toDomain(),
)

fun HomeUserSummaryResponse.toDomain(): HomeUserSummary = HomeUserSummary(
    nickname = nickname,
    dotoriBalance = dotoriBalance,
    xpTotal = xpTotal,
    currentLevel = currentLevel,
    levelName = levelName,
)

fun SubjectSummaryResponse.toDomain(): QuizSubjectSummary = QuizSubjectSummary(
    id = id,
    name = name,
    purpose = purpose,
    chapterCount = chapterCount,
    partCount = partCount,
    lastActivityAt = lastActivityAt,
    examSchedule = examSchedule?.toDomain(),
)

fun SubjectExamScheduleResponse.toDomain(): SubjectExamSchedule = SubjectExamSchedule(
    id = id,
    subjectId = subjectId,
    examName = examName,
    examDate = examDate,
    dDay = dDay,
)

fun RecentActivityResponse.toDomain(): RecentActivity = RecentActivity(
    activityId = activityId,
    activityType = activityType,
    quizSessionId = quizSessionId,
    playSessionId = playSessionId,
    resultId = resultId,
    title = title,
    subjectId = subjectId,
    subjectName = subjectName,
    status = status,
    progressPct = progressPct,
    scoreText = scoreText,
    createdAt = createdAt,
)
