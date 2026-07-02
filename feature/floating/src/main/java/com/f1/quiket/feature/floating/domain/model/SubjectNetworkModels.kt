package com.f1.quiket.feature.floating.domain.model

data class SubjectSummary(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
)

data class Subject(
    val id: String,
    val name: String,
    val purpose: String,
    val createdAt: String,
)

data class SubjectDetail(
    val id: String,
    val name: String,
    val purpose: String,
    val createdAt: String,
    val chapters: List<ChapterWithParts>,
    val examSchedule: SubjectExamSchedule? = null,
    val examDetail: SubjectExamDetail? = null,
    val reviewDetail: SubjectReviewDetail? = null,
    val otherDetail: SubjectOtherDetail? = null,
)

data class SubjectCreate(
    val name: String,
    val purpose: String,
    val examDetail: SubjectExamDetail? = null,
    val reviewDetail: SubjectReviewDetail? = null,
    val otherDetail: SubjectOtherDetail? = null,
)

data class SubjectExamDetail(
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

data class SubjectReviewDetail(
    val field: String,
    val studyLevel: String,
)

data class SubjectOtherDetail(
    val usagePurpose: String,
    val description: String? = null,
)

data class Certificate(
    val id: Long,
    val name: String,
    val featured: Boolean,
    val displayOrder: Int,
)

data class SubjectChapter(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
)

data class ChapterWithParts(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<PartSummary>,
)

data class PartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

data class Part(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val content: String?,
)

data class SubjectExamSchedule(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)
