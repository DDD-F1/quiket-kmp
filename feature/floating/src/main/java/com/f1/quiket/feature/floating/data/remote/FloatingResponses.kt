package com.f1.quiket.feature.floating.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
data class SubjectSummaryResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
)

@Serializable
data class SubjectResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val detail: JsonElement? = null,
    val createdAt: String,
)

@Serializable
data class SubjectDetailResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val detail: JsonElement? = null,
    val examDetail: JsonElement? = null,
    val reviewDetail: JsonElement? = null,
    val otherDetail: JsonElement? = null,
    val createdAt: String,
    val examSchedule: SubjectExamScheduleResponse? = null,
    val chapters: List<ChapterWithPartsResponse> = emptyList(),
)

@Serializable
data class SubjectExamDetailResponse(
    val examType: String = "",
    val univMajorField: String? = null,
    val univMajorName: String? = null,
    val univCourseType: String? = null,
    val mhGrade: String? = null,
    val mhSubjectType: String? = null,
    val certificateId: String? = null,
    val certificateName: String? = null,
    val civilRank: String? = null,
    val civilSeries: String? = null,
    val langType: String? = null,
    val langExamName: String? = null,
    val otherExamName: String? = null,
)

@Serializable
data class SubjectReviewDetailResponse(
    val field: String = "",
    val studyLevel: String = "",
)

@Serializable
data class SubjectOtherDetailResponse(
    val usagePurpose: String = "",
    val description: String? = null,
)

@Serializable
data class CertificateResponse(
    val id: Long,
    val name: String,
    val featured: Boolean,
    val displayOrder: Int,
)

@Serializable
data class ChapterResponse(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
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
data class PartResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
    val subjectId: String? = null,
    val lectureUploadId: String? = null,
    val content: String? = null,
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
data class LectureUploadAcceptedDataResponse(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
)

@Serializable
data class LectureUploadStatusDataResponse(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: String,
    val estimatedSeconds: Int? = null,
    val chapterName: String? = null,
    val progressPct: Int? = null,
    val parts: List<PartSummaryResponse> = emptyList(),
    val failCode: String? = null,
    val failMessage: String? = null,
    val failReason: String? = null,
)
