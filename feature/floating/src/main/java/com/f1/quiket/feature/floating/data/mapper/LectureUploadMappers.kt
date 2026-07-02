package com.f1.quiket.feature.floating.data.mapper

import com.f1.quiket.feature.floating.data.remote.LectureTextUploadRequest
import com.f1.quiket.feature.floating.data.remote.LectureUploadAcceptedDataResponse
import com.f1.quiket.feature.floating.data.remote.LectureUploadStatusDataResponse
import com.f1.quiket.feature.floating.data.remote.PartSplitPlanRequest
import com.f1.quiket.feature.floating.data.remote.PartTextAddRequest
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadAccepted
import com.f1.quiket.feature.floating.domain.model.LectureUploadProgress
import com.f1.quiket.feature.floating.domain.model.LectureUploadStatus
import com.f1.quiket.feature.floating.domain.model.PartTextAdd
import com.f1.quiket.feature.floating.domain.model.PartSplitPlan

fun LectureTextUpload.toRequest(): LectureTextUploadRequest = LectureTextUploadRequest(
    subjectId = subjectId,
    chapterName = chapterName,
    partSplitMethod = partSplitMethod.wireValue,
    text = text,
    partSplitPlans = partSplitPlans.takeIf { plans -> plans.isNotEmpty() }
        ?.map { plan -> plan.toRequest() },
)

fun PartSplitPlan.toRequest(): PartSplitPlanRequest = PartSplitPlanRequest(
    partNumber = partNumber,
    intendedName = intendedName,
)

fun PartTextAdd.toRequest(): PartTextAddRequest = PartTextAddRequest(
    partName = partName,
    text = text,
)

fun LectureUploadAcceptedDataResponse.toDomain(): LectureUploadAccepted = LectureUploadAccepted(
    lectureUploadId = lectureUploadId,
    subjectId = subjectId,
    chapterId = chapterId,
    status = status.toLectureUploadStatus(),
    estimatedSeconds = estimatedSeconds,
)

fun LectureUploadStatusDataResponse.toDomain(): LectureUploadProgress = LectureUploadProgress(
    lectureUploadId = lectureUploadId,
    subjectId = subjectId,
    chapterId = chapterId,
    status = status.toLectureUploadStatus(),
    estimatedSeconds = estimatedSeconds,
    chapterName = chapterName,
    progressPct = progressPct,
    parts = parts.map { part -> part.toDomain() },
    failCode = failCode,
    failMessage = failMessage,
    failReason = failReason,
)

private fun String.toLectureUploadStatus(): LectureUploadStatus =
    LectureUploadStatus.entries.firstOrNull { status -> status.wireValue == this }
        ?: LectureUploadStatus.Unknown
