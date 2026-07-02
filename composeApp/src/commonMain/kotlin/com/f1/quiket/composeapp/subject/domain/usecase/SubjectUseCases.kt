package com.f1.quiket.composeapp.subject.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.subject.Certificate
import com.f1.quiket.composeapp.subject.Chapter
import com.f1.quiket.composeapp.subject.CreatedSubject
import com.f1.quiket.composeapp.subject.LectureFileUploadType
import com.f1.quiket.composeapp.subject.LectureUploadAccepted
import com.f1.quiket.composeapp.subject.LectureUploadProgress
import com.f1.quiket.composeapp.subject.PartDetail
import com.f1.quiket.composeapp.subject.PartSplitMethod
import com.f1.quiket.composeapp.subject.PartSplitPlan
import com.f1.quiket.composeapp.subject.PickedUploadFile
import com.f1.quiket.composeapp.subject.SubjectCreateInput
import com.f1.quiket.composeapp.subject.SubjectDetail
import com.f1.quiket.composeapp.subject.SubjectExamSchedule
import com.f1.quiket.composeapp.subject.SubjectException
import com.f1.quiket.composeapp.subject.SubjectListItem
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository

internal class SubjectUseCases(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: SubjectRepository,
) {
    suspend fun getSubjects(page: Int = 0, size: Int = 50): List<SubjectListItem> =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getSubjects(session = session, page = page, size = size)
        }

    suspend fun createSubject(input: SubjectCreateInput): CreatedSubject =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.createSubject(session = session, input = input)
        }

    suspend fun getSubject(subjectId: String): SubjectDetail =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getSubject(session = session, subjectId = subjectId)
        }

    suspend fun deleteSubject(subjectId: String) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.deleteSubject(session = session, subjectId = subjectId)
        }
    }

    suspend fun upsertExamSchedule(
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.upsertExamSchedule(
                session = session,
                subjectId = subjectId,
                examName = examName,
                examDate = examDate,
            )
        }

    suspend fun deleteExamSchedule(subjectId: String) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.deleteExamSchedule(session = session, subjectId = subjectId)
        }
    }

    suspend fun getCertificates(): List<Certificate> =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getCertificates(session)
        }

    suspend fun updateSubjectName(subjectId: String, name: String): CreatedSubject =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateSubjectName(session = session, subjectId = subjectId, name = name)
        }

    suspend fun updateSubjectDetails(
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateSubjectDetails(
                session = session,
                subjectId = subjectId,
                input = input,
            )
        }

    suspend fun updateChapterName(chapterId: String, name: String): Chapter =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateChapterName(session = session, chapterId = chapterId, name = name)
        }

    suspend fun deleteChapter(chapterId: String) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.deleteChapter(session = session, chapterId = chapterId)
        }
    }

    suspend fun getPart(partId: String): PartDetail =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getPart(session = session, partId = partId)
        }

    suspend fun updatePart(partId: String, name: String, content: String): PartDetail =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updatePart(session = session, partId = partId, name = name, content = content)
        }

    suspend fun createTextLectureUpload(
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.createTextLectureUpload(
                session = session,
                subjectId = subjectId,
                chapterName = chapterName,
                text = text,
                partSplitMethod = partSplitMethod,
                partSplitPlans = partSplitPlans,
            )
        }

    suspend fun createFileLectureUpload(
        subjectId: String,
        chapterName: String?,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.createFileLectureUpload(
                session = session,
                subjectId = subjectId,
                chapterName = chapterName,
                uploadType = uploadType,
                files = files,
                partSplitMethod = partSplitMethod,
                partSplitPlans = partSplitPlans,
            )
        }

    suspend fun addTextPartToChapter(
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.addTextPartToChapter(
                session = session,
                chapterId = chapterId,
                partName = partName,
                text = text,
            )
        }

    suspend fun addFilePartToChapter(
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.addFilePartToChapter(
                session = session,
                chapterId = chapterId,
                partName = partName,
                uploadType = uploadType,
                files = files,
            )
        }

    suspend fun getLectureUploadStatus(lectureUploadId: String): LectureUploadProgress =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getLectureUploadStatus(
                session = session,
                lectureUploadId = lectureUploadId,
            )
        }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is SubjectException && error.isUnauthorized
}
