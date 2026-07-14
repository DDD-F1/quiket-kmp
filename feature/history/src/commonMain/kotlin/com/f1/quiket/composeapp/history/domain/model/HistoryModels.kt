package com.f1.quiket.composeapp.history.domain.model

class HistoryException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

data class RecentActivityPage(
    val activities: List<HistoryActivity>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class HistoryActivity(
    val activityId: String,
    val activityType: HistoryActivityType,
    val quizSessionId: String?,
    val clientSessionId: String?,
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

enum class HistoryActivityType(
    val wireValue: String,
) {
    QuizGenerating("quiz_generating"),
    QuizReady("quiz_ready"),
    QuizInProgress("quiz_in_progress"),
    QuizCompleted("quiz_completed"),
    LectureUploaded("lecture_uploaded"),
    Unknown("unknown"),
}
