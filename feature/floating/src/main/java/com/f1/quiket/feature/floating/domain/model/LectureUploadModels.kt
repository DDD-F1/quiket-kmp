package com.f1.quiket.feature.floating.domain.model

data class LectureTextUpload(
    val subjectId: String,
    val chapterName: String?,
    val partSplitMethod: PartSplitMethod,
    val text: String,
    val partSplitPlans: List<PartSplitPlan> = emptyList(),
)

data class LectureFileUpload(
    val subjectId: String,
    val chapterName: String?,
    val uploadType: LectureFileUploadType,
    val partSplitMethod: PartSplitMethod,
    val partSplitPlans: List<PartSplitPlan> = emptyList(),
)

data class PartTextAdd(
    val partName: String,
    val text: String,
)

data class PartFileAdd(
    val partName: String,
    val uploadType: LectureFileUploadType,
)

data class PartSplitPlan(
    val partNumber: Int,
    val intendedName: String? = null,
)

data class LectureUploadAccepted(
    val lectureUploadId: String,
    val subjectId: String,
    val chapterId: String,
    val status: LectureUploadStatus,
    val estimatedSeconds: Int?,
)

data class LectureUploadProgress(
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

enum class PartSplitMethod(
    val wireValue: String,
) {
    Auto("auto"),
    Manual("manual"),
}

enum class LectureFileUploadType(
    val wireValue: String,
) {
    Pdf("pdf"),
    Image("image"),
}

enum class LectureUploadStatus(
    val wireValue: String,
) {
    Pending("pending"),
    Processing("processing"),
    Completed("completed"),
    Failed("failed"),
    Unknown("unknown"),
}
