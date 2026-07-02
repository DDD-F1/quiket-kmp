package com.f1.quiket.feature.home.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class QuizScopeDataResponse(
    val subjectId: String,
    val subjectName: String,
    val chapters: List<ChapterWithPartsResponse> = emptyList(),
)

@Serializable
data class ChapterWithPartsResponse(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<PartSummaryResponse> = emptyList(),
)

@Serializable
data class PartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
)

@Serializable
data class QuizCreateRequest(
    val subjectId: String,
    val partIds: List<String>,
    val quizType: String,
    val choiceCount: Int? = null,
    val questionCount: Int,
    val playMode: String,
    val timerEnabled: Boolean = false,
    val timerScope: String? = null,
    val timerSeconds: Int? = null,
    val difficulty: String,
)

@Serializable
data class QuizGenerationAcceptedDataResponse(
    val quizSessionId: String,
    val jobId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
)

@Serializable
data class QuizGenerationStatusDataResponse(
    val quizSessionId: String,
    val jobId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
    val progressPct: Int? = null,
    val generatedCount: Int? = null,
    val failReason: String? = null,
)
