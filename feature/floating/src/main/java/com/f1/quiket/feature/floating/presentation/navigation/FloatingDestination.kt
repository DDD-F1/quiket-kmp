package com.f1.quiket.feature.floating.presentation.navigation

import android.net.Uri
import com.f1.quiket.core.navigation.QuiketDestination
import com.f1.quiket.feature.floating.domain.model.ExamType
import com.f1.quiket.feature.floating.domain.model.StudyField
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.UsagePurpose

data object ScheduleExamDestination : QuiketDestination {
    override val route: String = "schedule_exam"
}

data object CreateQuizDestination : QuiketDestination {
    override val route: String = "create_quiz"
}

data object UploadDestination : QuiketDestination {
    override val route: String = "upload"
}

data object AddSubjectDestination : QuiketDestination {
    override val route: String = "add_subject"
}

data object AddSubjectStep1Destination : QuiketDestination {
    override val route: String = "add_subject/step1"
}

data object AddSubjectStep2Destination : QuiketDestination {
    const val ARG_PURPOSE = "purpose"
    override val route: String = "add_subject/step2/{$ARG_PURPOSE}"

    fun createRoute(purpose: StudyPurpose) = "add_subject/step2/${purpose.name}"
}

data object AddSubjectStep3Destination : QuiketDestination {
    const val ARG_PURPOSE = "purpose"
    const val ARG_EXAM_TYPE = "examType"
    const val ARG_FIELD = "field"
    const val ARG_USAGE = "usage"

    override val route: String =
        "add_subject/step3/{$ARG_PURPOSE}?$ARG_EXAM_TYPE={$ARG_EXAM_TYPE}&$ARG_FIELD={$ARG_FIELD}&$ARG_USAGE={$ARG_USAGE}"

    fun createRoute(
        purpose: StudyPurpose,
        examType: ExamType? = null,
        studyField: StudyField? = null,
        usagePurpose: UsagePurpose? = null,
    ) = buildString {
        append("add_subject/step3/${purpose.name}")
        append("?$ARG_EXAM_TYPE=${examType?.name ?: ""}")
        append("&$ARG_FIELD=${studyField?.name ?: ""}")
        append("&$ARG_USAGE=${usagePurpose?.name ?: ""}")
    }
}

/**
 * 과목 선택 화면.
 * 강의가 지정되어 있을 때만 진입합니다.
 */
data object LectureSelectDestination : QuiketDestination {
    override val route: String = "lecture_select"
}

/**
 * 강의 뷰어 화면.
 */
data object LectureViewDestination : QuiketDestination {
    const val ARG_SUBJECT_ID = "subjectId"
    const val ARG_CHAPTER_ID = "chapterId"
    const val ARG_CHAPTER_NUMBER = "chapterNumber"
    const val ARG_CHAPTER_NAME = "chapterName"
    const val ARG_PART_COUNT = "partCount"

    override val route: String =
        "lecture_view/{$ARG_SUBJECT_ID}/{$ARG_CHAPTER_ID}/{$ARG_CHAPTER_NUMBER}/{$ARG_CHAPTER_NAME}/{$ARG_PART_COUNT}"

    fun createRoute(
        subjectId: String,
        chapterId: String,
        chapterNumber: Int,
        chapterName: String,
        partCount: Int,
    ) = "lecture_view/${routeArg(subjectId)}/${routeArg(chapterId)}/$chapterNumber/${routeArg(chapterName)}/$partCount"
}

/**
 * 자료 확인 화면.
 * 업로드 완료 후 챕터/파트 이름을 수정하는 화면
 */
data object MaterialCheckDestination : QuiketDestination {
    const val ARG_LECTURE_UPLOAD_ID = "lectureUploadId"
    const val ARG_CHAPTER_NUMBER = "chapterNumber"

    override val route: String =
        "material_check/{$ARG_LECTURE_UPLOAD_ID}/{$ARG_CHAPTER_NUMBER}"

    fun createRoute(lectureUploadId: String, chapterNumber: Int) =
        "material_check/${routeArg(lectureUploadId)}/$chapterNumber"
}

/**
 * 자료 추가 화면.
 * - 강의 없이 바로 진입: route = "lecture_upload_file"
 * - 과목 선택 후 진입 : route = "lecture_upload_file/{lectureId}/{lectureTitle}/{chapterCount}"
 */
data object LectureUploadFileDestination : QuiketDestination {
    const val ARG_LECTURE_ID = "lectureId"
    const val ARG_LECTURE_TITLE = "lectureTitle"
    const val ARG_CHAPTER_COUNT = "chapterCount"
    const val ARG_CATEGORY = "category"

    // 강의 지정 없이 바로 진입하는 경우
    override val route: String = "lecture_upload_file"

    // 과목 선택 후 진입하는 경우
    const val routeWithArgs =
        "lecture_upload_file/{$ARG_LECTURE_ID}/{$ARG_LECTURE_TITLE}/{$ARG_CHAPTER_COUNT}/{$ARG_CATEGORY}"

    fun createRoute(
        lectureId: String,
        lectureTitle: String,
        chapterCount: Int,
        category: String,
    ) = "lecture_upload_file/${routeArg(lectureId)}/${routeArg(lectureTitle)}/$chapterCount/${routeArg(category)}"
}

private fun routeArg(value: String, fallback: String = "-"): String =
    Uri.encode(value.ifBlank { fallback })
