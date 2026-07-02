package com.f1.quiket.feature.home.domain.model

data class HomeData(
    val user: HomeUserSummary,
    val hero: HomeHero?,
    val dDayCards: List<SubjectExamSchedule>,
    val subjects: List<QuizSubjectSummary>,
    val recentActivities: List<RecentActivity>,
)

data class HomeHero(
    val hasActiveQuiz: Boolean,
    val activeQuiz: RecentActivity?,
)

data class HomeUserSummary(
    val nickname: String,
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val levelName: String?,
)

data class QuizSubjectSummary(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
    val lastActivityAt: String?,
    val examSchedule: SubjectExamSchedule?,
)

data class SubjectExamSchedule(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)

data class RecentActivity(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String?,
    val playSessionId: String?,
    val resultId: String?,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String?,
    val progressPct: Int?,
    val scoreText: String?,
    val createdAt: String,
)
