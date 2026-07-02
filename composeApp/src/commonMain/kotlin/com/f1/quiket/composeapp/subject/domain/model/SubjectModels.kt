package com.f1.quiket.composeapp.subject.domain.model

internal class SubjectException(
    message: String,
    val isUnauthorized: Boolean = false,
    val statusCode: Int? = null,
) : Exception(message)

internal data class SubjectDetail(
    val id: String,
    val name: String,
    val purpose: String,
    val detailLabel: String?,
    val examDetail: SubjectExamDetail? = null,
    val reviewDetail: SubjectReviewDetail? = null,
    val otherDetail: SubjectOtherDetail? = null,
    val createdAt: String,
    val examSchedule: SubjectExamSchedule?,
    val chapters: List<ChapterWithParts>,
)

internal data class SubjectListItem(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
)

internal data class CreatedSubject(
    val id: String,
    val name: String,
    val purpose: String,
    val createdAt: String,
)

internal data class SubjectCreateInput(
    val name: String,
    val purpose: SubjectCreatePurpose,
    val examDetail: SubjectExamDetailInput? = null,
    val reviewDetail: SubjectReviewDetailInput? = null,
    val otherDetail: SubjectOtherDetailInput? = null,
)

internal data class SubjectExamDetailInput(
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

internal data class SubjectReviewDetailInput(
    val field: String,
    val studyLevel: String,
)

internal data class SubjectOtherDetailInput(
    val usagePurpose: String,
    val description: String? = null,
)

internal data class SubjectExamDetail(
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

internal data class SubjectReviewDetail(
    val field: String,
    val studyLevel: String,
)

internal data class SubjectOtherDetail(
    val usagePurpose: String,
    val description: String? = null,
)

internal data class Chapter(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
)

internal enum class SubjectCreatePurpose(
    val title: String,
    val description: String,
    val wireValue: String,
) {
    Exam(
        title = "시험·자격증 대비",
        description = "시험일, 범위를 체계적으로 준비해요",
        wireValue = "exam",
    ),
    Review(
        title = "자기계발·일반 복습",
        description = "언제든 퀴즈로 복습해요",
        wireValue = "review",
    ),
    Other(
        title = "기타",
        description = "자유롭게 설정할게요",
        wireValue = "other",
    ),
}

internal data class SubjectExamSchedule(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)

internal data class Certificate(
    val id: Long,
    val name: String,
    val featured: Boolean,
    val displayOrder: Int,
)

internal data class ChapterWithParts(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<PartSummary>,
)

internal data class PartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

internal data class PartDetail(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
    val subjectId: String?,
    val lectureUploadId: String?,
    val content: String?,
)

internal data class LectureUploadAccepted(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: LectureUploadStatus,
    val estimatedSeconds: Int?,
)

internal data class LectureUploadProgress(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: LectureUploadStatus,
    val estimatedSeconds: Int?,
    val chapterName: String?,
    val progressPct: Int?,
    val parts: List<PartSummary>,
    val failCode: String?,
    val failMessage: String?,
    val failReason: String?,
)

internal data class PartSplitPlan(
    val partNumber: Int,
    val intendedName: String? = null,
)

internal enum class PartSplitMethod(
    val wireValue: String,
) {
    Auto("auto"),
    Manual("manual"),
}

internal enum class LectureUploadStatus(
    val wireValue: String,
) {
    Pending("pending"),
    Processing("processing"),
    Completed("completed"),
    Failed("failed"),
    Unknown("unknown"),
}

internal enum class LectureFileUploadType(
    val wireValue: String,
) {
    Pdf("pdf"),
    Image("image"),
}

internal fun String.toSubjectPurposeLabel(): String = when (lowercase()) {
    "exam" -> "시험 준비"
    "review" -> "복습"
    "self_study" -> "자기주도 학습"
    "other" -> "기타"
    else -> this
}
