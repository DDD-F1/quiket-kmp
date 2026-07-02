package com.f1.quiket.feature.home.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
)

@Serializable
data class HomeDataResponse(
    val user: HomeUserSummaryResponse,
    val hero: HomeHeroResponse? = null,
    val dDayCards: List<SubjectExamScheduleResponse> = emptyList(),
    val subjects: List<SubjectSummaryResponse> = emptyList(),
    val recentActivities: List<RecentActivityResponse> = emptyList(),
)

@Serializable
data class HomeHeroResponse(
    val hasActiveQuiz: Boolean = false,
    val activeQuiz: RecentActivityResponse? = null,
)

@Serializable
data class HomeUserSummaryResponse(
    val nickname: String,
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val levelName: String? = null,
)

@Serializable
data class SubjectSummaryResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
    val lastActivityAt: String? = null,
    val examSchedule: SubjectExamScheduleResponse? = null,
)

@Serializable
data class SubjectExamScheduleResponse(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int? = null,
)

@Serializable
data class RecentActivityResponse(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String? = null,
    val playSessionId: String? = null,
    val resultId: String? = null,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String? = null,
    val progressPct: Int? = null,
    val scoreText: String? = null,
    val createdAt: String,
)
