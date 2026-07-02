package com.f1.quiket.composeapp.subject.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.subject.domain.model.Certificate
import com.f1.quiket.composeapp.subject.domain.model.Chapter
import com.f1.quiket.composeapp.subject.domain.model.CreatedSubject
import com.f1.quiket.composeapp.subject.domain.model.LectureFileUploadType
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadAccepted
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadProgress
import com.f1.quiket.composeapp.subject.domain.model.PartDetail
import com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod
import com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan
import com.f1.quiket.composeapp.subject.PickedUploadFile
import com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectExamSchedule
import com.f1.quiket.composeapp.subject.domain.model.SubjectListItem
import com.f1.quiket.composeapp.subject.data.remote.SubjectRemoteDataSource
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository

internal class SubjectRepositoryImpl(
    private val remoteDataSource: SubjectRemoteDataSource,
) : SubjectRepository {
    override suspend fun getSubjects(session: SessionSnapshot, page: Int, size: Int): List<SubjectListItem> =
        remoteDataSource.getSubjects(session = session, page = page, size = size)

    override suspend fun createSubject(session: SessionSnapshot, input: SubjectCreateInput): CreatedSubject =
        remoteDataSource.createSubject(session = session, input = input)

    override suspend fun getSubject(session: SessionSnapshot, subjectId: String): SubjectDetail =
        remoteDataSource.getSubject(session = session, subjectId = subjectId)

    override suspend fun deleteSubject(session: SessionSnapshot, subjectId: String) {
        remoteDataSource.deleteSubject(session = session, subjectId = subjectId)
    }

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule = remoteDataSource.upsertExamSchedule(
        session = session,
        subjectId = subjectId,
        examName = examName,
        examDate = examDate,
    )

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) {
        remoteDataSource.deleteExamSchedule(session = session, subjectId = subjectId)
    }

    override suspend fun getCertificates(session: SessionSnapshot): List<Certificate> =
        remoteDataSource.getCertificates(session)

    override suspend fun updateSubjectName(
        session: SessionSnapshot,
        subjectId: String,
        name: String,
    ): CreatedSubject = remoteDataSource.updateSubjectName(
        session = session,
        subjectId = subjectId,
        name = name,
    )

    override suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject = remoteDataSource.updateSubjectDetails(
        session = session,
        subjectId = subjectId,
        input = input,
    )

    override suspend fun updateChapterName(
        session: SessionSnapshot,
        chapterId: String,
        name: String,
    ): Chapter = remoteDataSource.updateChapterName(
        session = session,
        chapterId = chapterId,
        name = name,
    )

    override suspend fun deleteChapter(session: SessionSnapshot, chapterId: String) {
        remoteDataSource.deleteChapter(session = session, chapterId = chapterId)
    }

    override suspend fun getPart(session: SessionSnapshot, partId: String): PartDetail =
        remoteDataSource.getPart(session = session, partId = partId)

    override suspend fun updatePart(
        session: SessionSnapshot,
        partId: String,
        name: String,
        content: String,
    ): PartDetail = remoteDataSource.updatePart(
        session = session,
        partId = partId,
        name = name,
        content = content,
    )

    override suspend fun createTextLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: PartSplitMethod,
        partSplitPlans: List<PartSplitPlan>,
    ): LectureUploadAccepted = remoteDataSource.createTextLectureUpload(
        session = session,
        subjectId = subjectId,
        chapterName = chapterName,
        text = text,
        partSplitMethod = partSplitMethod,
        partSplitPlans = partSplitPlans,
    )

    override suspend fun createFileLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: PartSplitMethod,
        partSplitPlans: List<PartSplitPlan>,
    ): LectureUploadAccepted = remoteDataSource.createFileLectureUpload(
        session = session,
        subjectId = subjectId,
        chapterName = chapterName,
        uploadType = uploadType,
        files = files,
        partSplitMethod = partSplitMethod,
        partSplitPlans = partSplitPlans,
    )

    override suspend fun addTextPartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted = remoteDataSource.addTextPartToChapter(
        session = session,
        chapterId = chapterId,
        partName = partName,
        text = text,
    )

    override suspend fun addFilePartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted = remoteDataSource.addFilePartToChapter(
        session = session,
        chapterId = chapterId,
        partName = partName,
        uploadType = uploadType,
        files = files,
    )

    override suspend fun getLectureUploadStatus(
        session: SessionSnapshot,
        lectureUploadId: String,
    ): LectureUploadProgress = remoteDataSource.getLectureUploadStatus(
        session = session,
        lectureUploadId = lectureUploadId,
    )
}
