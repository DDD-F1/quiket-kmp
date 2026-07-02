package com.f1.quiket.composeapp.subject.domain.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
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
import com.f1.quiket.composeapp.subject.SubjectListItem

internal interface SubjectRepository {
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
