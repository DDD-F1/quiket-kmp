package com.f1.quiket.feature.floating.data.remote

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault

@Serializable
data class SubjectCreateRequest(
    val name: String,
    val purpose: String,
    val examDetail: SubjectExamDetailRequest? = null,
    val reviewDetail: SubjectReviewDetailRequest? = null,
    val otherDetail: SubjectOtherDetailRequest? = null,
)

@Serializable
data class SubjectExamDetailRequest(
    val examType: String,
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
data class SubjectReviewDetailRequest(
    val field: String,
    val studyLevel: String,
)

@Serializable
data class SubjectOtherDetailRequest(
    val usagePurpose: String,
    val description: String? = null,
)

@Serializable
data class SubjectNameUpdateRequest(
    val name: String,
)

@Serializable
data class SubjectExamScheduleUpsertRequest(
    val examName: String? = null,
    val examDate: String,
)

@Serializable
data class ChapterNameUpdateRequest(
    val name: String,
)

@Serializable
data class PartUpdateRequest(
    val name: String,
    val content: String,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LectureTextUploadRequest(
    val subjectId: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val chapterName: String? = null,
    val uploadType: String = "text",
    val partSplitMethod: String,
    val text: String,
    val partSplitPlans: List<PartSplitPlanRequest>? = null,
)

@Serializable
data class PartSplitPlanRequest(
    val partNumber: Int,
    val intendedName: String? = null,
)

@Serializable
data class PartTextAddRequest(
    val partName: String,
    val uploadType: String = "text",
    val text: String,
)
