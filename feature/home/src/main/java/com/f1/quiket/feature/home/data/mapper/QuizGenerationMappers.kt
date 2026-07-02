package com.f1.quiket.feature.home.data.mapper

import com.f1.quiket.feature.home.data.remote.ChapterWithPartsResponse
import com.f1.quiket.feature.home.data.remote.PartSummaryResponse
import com.f1.quiket.feature.home.data.remote.QuizCreateRequest
import com.f1.quiket.feature.home.data.remote.QuizGenerationAcceptedDataResponse
import com.f1.quiket.feature.home.data.remote.QuizGenerationStatusDataResponse
import com.f1.quiket.feature.home.data.remote.QuizScopeDataResponse
import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizGenerationAccepted
import com.f1.quiket.feature.home.domain.model.QuizGenerationProgress
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizScope
import com.f1.quiket.feature.home.domain.model.QuizScopeChapter
import com.f1.quiket.feature.home.domain.model.QuizScopePart

fun QuizScopeDataResponse.toDomain(): QuizScope = QuizScope(
    subjectId = subjectId,
    subjectName = subjectName,
    chapters = chapters.map { chapter -> chapter.toDomain() },
)

fun ChapterWithPartsResponse.toDomain(): QuizScopeChapter = QuizScopeChapter(
    id = id,
    subjectId = subjectId,
    name = name,
    displayOrder = displayOrder,
    parts = parts.map { part -> part.toDomain() },
)

fun PartSummaryResponse.toDomain(): QuizScopePart = QuizScopePart(
    id = id,
    chapterId = chapterId,
    name = name,
    partNumber = partNumber,
    contentPreview = contentPreview,
)

fun QuizCreate.toRequest(): QuizCreateRequest = QuizCreateRequest(
    subjectId = subjectId,
    partIds = partIds,
    quizType = quizType.wireValue,
    choiceCount = choiceCount,
    questionCount = questionCount,
    playMode = playMode.wireValue,
    timerEnabled = timerEnabled,
    timerScope = timerScope?.wireValue,
    timerSeconds = timerSeconds,
    difficulty = difficulty.wireValue,
)

fun QuizGenerationAcceptedDataResponse.toDomain(): QuizGenerationAccepted = QuizGenerationAccepted(
    quizSessionId = quizSessionId,
    jobId = jobId,
    status = status.toGenerationStatusDomain(),
    estimatedSeconds = estimatedSeconds,
)

fun QuizGenerationStatusDataResponse.toDomain(): QuizGenerationProgress = QuizGenerationProgress(
    quizSessionId = quizSessionId,
    jobId = jobId,
    status = status.toGenerationStatusDomain(),
    estimatedSeconds = estimatedSeconds,
    progressPct = progressPct,
    generatedCount = generatedCount,
    failReason = failReason,
)

private fun String.toGenerationStatusDomain(): QuizGenerationStatus =
    QuizGenerationStatus.entries.firstOrNull { status -> status.wireValue == this }
        ?: QuizGenerationStatus.Unknown
