package com.f1.quiket.composeapp.subject.data.remote

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

internal interface SubjectRemoteDataSource {
    suspend fun getSubjects(session: SessionSnapshot, page: Int = 0, size: Int = 50): List<SubjectListItem>
    suspend fun createSubject(session: SessionSnapshot, input: SubjectCreateInput): CreatedSubject
    suspend fun getSubject(session: SessionSnapshot, subjectId: String): SubjectDetail
    suspend fun deleteSubject(session: SessionSnapshot, subjectId: String)
    suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule
    suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String)
    suspend fun getCertificates(session: SessionSnapshot): List<Certificate>
    suspend fun updateSubjectName(session: SessionSnapshot, subjectId: String, name: String): CreatedSubject
    suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject
    suspend fun updateChapterName(session: SessionSnapshot, chapterId: String, name: String): Chapter
    suspend fun deleteChapter(session: SessionSnapshot, chapterId: String)
    suspend fun getPart(session: SessionSnapshot, partId: String): PartDetail
    suspend fun updatePart(session: SessionSnapshot, partId: String, name: String, content: String): PartDetail
    suspend fun createTextLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted
    suspend fun createFileLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: PartSplitMethod = PartSplitMethod.Auto,
        partSplitPlans: List<PartSplitPlan> = emptyList(),
    ): LectureUploadAccepted
    suspend fun addTextPartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted
    suspend fun addFilePartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted
    suspend fun getLectureUploadStatus(
        session: SessionSnapshot,
        lectureUploadId: String,
    ): LectureUploadProgress
}

internal class SubjectRemoteDataSourceImpl(
    private val client: SubjectClient,
) : SubjectRemoteDataSource {
    override suspend fun getSubjects(session: SessionSnapshot, page: Int, size: Int): List<SubjectListItem> =
        client.getSubjects(session = session, page = page, size = size)

    override suspend fun createSubject(session: SessionSnapshot, input: SubjectCreateInput): CreatedSubject =
        client.createSubject(session = session, input = input)

    override suspend fun getSubject(session: SessionSnapshot, subjectId: String): SubjectDetail =
        client.getSubject(session = session, subjectId = subjectId)

    override suspend fun deleteSubject(session: SessionSnapshot, subjectId: String) {
        client.deleteSubject(session = session, subjectId = subjectId)
    }

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule = client.upsertExamSchedule(
        session = session,
        subjectId = subjectId,
        examName = examName,
        examDate = examDate,
    )

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) {
        client.deleteExamSchedule(session = session, subjectId = subjectId)
    }

    override suspend fun getCertificates(session: SessionSnapshot): List<Certificate> =
        client.getCertificates(session)

    override suspend fun updateSubjectName(
        session: SessionSnapshot,
        subjectId: String,
        name: String,
    ): CreatedSubject = client.updateSubjectName(
        session = session,
        subjectId = subjectId,
        name = name,
    )

    override suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject = client.updateSubjectDetails(
        session = session,
        subjectId = subjectId,
        input = input,
    )

    override suspend fun updateChapterName(
        session: SessionSnapshot,
        chapterId: String,
        name: String,
    ): Chapter = client.updateChapterName(
        session = session,
        chapterId = chapterId,
        name = name,
    )

    override suspend fun deleteChapter(session: SessionSnapshot, chapterId: String) {
        client.deleteChapter(session = session, chapterId = chapterId)
    }

    override suspend fun getPart(session: SessionSnapshot, partId: String): PartDetail =
        client.getPart(session = session, partId = partId)

    override suspend fun updatePart(
        session: SessionSnapshot,
        partId: String,
        name: String,
        content: String,
    ): PartDetail = client.updatePart(
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
    ): LectureUploadAccepted = client.createTextLectureUpload(
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
    ): LectureUploadAccepted = client.createFileLectureUpload(
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
    ): LectureUploadAccepted = client.addTextPartToChapter(
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
    ): LectureUploadAccepted = client.addFilePartToChapter(
        session = session,
        chapterId = chapterId,
        partName = partName,
        uploadType = uploadType,
        files = files,
    )

    override suspend fun getLectureUploadStatus(
        session: SessionSnapshot,
        lectureUploadId: String,
    ): LectureUploadProgress = client.getLectureUploadStatus(
        session = session,
        lectureUploadId = lectureUploadId,
    )
}
