package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.FakeAuthRepository
import com.f1.quiket.composeapp.FakeSessionRepository
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.subject.domain.model.Certificate
import com.f1.quiket.composeapp.subject.domain.model.Chapter
import com.f1.quiket.composeapp.subject.domain.model.ChapterWithParts
import com.f1.quiket.composeapp.subject.domain.model.CreatedSubject
import com.f1.quiket.composeapp.subject.domain.model.LectureFileUploadType
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadAccepted
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadProgress
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadStatus
import com.f1.quiket.composeapp.subject.domain.model.PartDetail
import com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod
import com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan
import com.f1.quiket.composeapp.subject.domain.model.PartSummary
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import com.f1.quiket.composeapp.subject.PartDetailUiState
import com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.SubjectDetailUiState
import com.f1.quiket.composeapp.subject.domain.model.SubjectExamSchedule
import com.f1.quiket.composeapp.subject.domain.model.SubjectListItem
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SubjectDetailStateHolderTest {
    @Test
    fun updateChapterNameUpdatesLoadedSubjectState() = runTest {
        val repository = FakeSubjectRepository()
        val stateHolder = SubjectDetailStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.loadSubject(
            subjectId = "subject-1",
            subjectName = "SQLD",
            onSessionExpired = {},
        )
        val chapter = repository.subject.chapters.single()

        val updated = stateHolder.updateChapterName(
            chapter = chapter,
            name = "변경된 챕터",
            onSessionExpired = {},
        )

        assertTrue(updated)
        val state = assertIs<SubjectDetailUiState.Success>(stateHolder.state)
        assertEquals("변경된 챕터", state.subject.chapters.single().name)
        assertEquals("챕터명이 수정됐어요", stateHolder.managementMessage)
        assertFalse(stateHolder.isManagementBusy)
    }

    @Test
    fun deleteChapterRemovesChapterFromLoadedSubjectState() = runTest {
        val repository = FakeSubjectRepository()
        val stateHolder = SubjectDetailStateHolder(subjectUseCases = repository.toUseCases())
        stateHolder.loadSubject(
            subjectId = "subject-1",
            subjectName = "SQLD",
            onSessionExpired = {},
        )
        val chapter = repository.subject.chapters.single()

        val deleted = stateHolder.deleteChapter(
            chapter = chapter,
            onSessionExpired = {},
        )

        assertTrue(deleted)
        val state = assertIs<SubjectDetailUiState.Success>(stateHolder.state)
        assertEquals(emptyList(), state.subject.chapters)
        assertEquals("챕터가 삭제됐어요", stateHolder.managementMessage)
        assertEquals("chapter-1", repository.deletedChapterId)
    }

    @Test
    fun savePartEditUpdatesPartStateAndReturnsUpdatedPart() = runTest {
        val repository = FakeSubjectRepository()
        val stateHolder = PartDetailStateHolder(subjectUseCases = repository.toUseCases())
        stateHolder.loadPart(partId = "part-1", onSessionExpired = {})
        stateHolder.enterEditMode()
        stateHolder.updateDraftName("새 파트명")
        stateHolder.updateDraftContent("새 파트 내용입니다.")

        val updatedPart = stateHolder.saveEdit(
            partId = "part-1",
            onSessionExpired = {},
        )

        assertNotNull(updatedPart)
        assertEquals("새 파트명", updatedPart.name)
        assertEquals("새 파트 내용입니다.", updatedPart.content)
        assertFalse(stateHolder.isEditMode)
        assertFalse(stateHolder.isSaving)
        assertEquals("파트 내용이 저장됐어요", stateHolder.feedbackMessage)
        val state = assertIs<PartDetailUiState.Success>(stateHolder.state)
        assertEquals("새 파트명", state.part.name)
        assertEquals("새 파트 내용입니다.", state.part.contentPreview)
    }
}

private class FakeSubjectRepository : SubjectRepository {
    var subject: SubjectDetail = testSubject()
    var part: PartDetail = testPart()
    var deletedChapterId: String? = null

    override suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): List<SubjectListItem> = unsupported()

    override suspend fun createSubject(
        session: SessionSnapshot,
        input: SubjectCreateInput,
    ): CreatedSubject = unsupported()

    override suspend fun getSubject(
        session: SessionSnapshot,
        subjectId: String,
    ): SubjectDetail = subject

    override suspend fun deleteSubject(session: SessionSnapshot, subjectId: String) = Unit

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): SubjectExamSchedule = SubjectExamSchedule(
        id = "schedule-1",
        subjectId = subjectId,
        examName = examName.orEmpty(),
        examDate = examDate,
        dDay = 10,
    )

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) = Unit

    override suspend fun getCertificates(session: SessionSnapshot): List<Certificate> = unsupported()

    override suspend fun updateSubjectName(
        session: SessionSnapshot,
        subjectId: String,
        name: String,
    ): CreatedSubject = CreatedSubject(
        id = subjectId,
        name = name,
        purpose = subject.purpose,
        createdAt = subject.createdAt,
    )

    override suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: SubjectCreateInput,
    ): CreatedSubject = unsupported()

    override suspend fun updateChapterName(
        session: SessionSnapshot,
        chapterId: String,
        name: String,
    ): Chapter = Chapter(
        id = chapterId,
        subjectId = subject.id,
        name = name,
        displayOrder = 1,
    )

    override suspend fun deleteChapter(session: SessionSnapshot, chapterId: String) {
        deletedChapterId = chapterId
    }

    override suspend fun getPart(session: SessionSnapshot, partId: String): PartDetail = part

    override suspend fun updatePart(
        session: SessionSnapshot,
        partId: String,
        name: String,
        content: String,
    ): PartDetail {
        part = part.copy(
            id = partId,
            name = name,
            content = content,
            contentPreview = content,
        )
        return part
    }

    override suspend fun createTextLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: PartSplitMethod,
        partSplitPlans: List<PartSplitPlan>,
    ): LectureUploadAccepted = unsupported()

    override suspend fun createFileLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: PartSplitMethod,
        partSplitPlans: List<PartSplitPlan>,
    ): LectureUploadAccepted = unsupported()

    override suspend fun addTextPartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted = unsupported()

    override suspend fun addFilePartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted = unsupported()

    override suspend fun getLectureUploadStatus(
        session: SessionSnapshot,
        lectureUploadId: String,
    ): LectureUploadProgress = LectureUploadProgress(
        lectureUploadId = lectureUploadId,
        subjectId = subject.id,
        chapterId = "chapter-1",
        status = LectureUploadStatus.Completed,
        estimatedSeconds = null,
        chapterName = "챕터 1",
        progressPct = 100,
        parts = emptyList(),
        failCode = null,
        failMessage = null,
        failReason = null,
    )

    fun toUseCases(): SubjectUseCases = SubjectUseCases(
        authenticatedCallRunner = AuthenticatedCallRunner(
            sessionRepository = FakeSessionRepository(),
            authRepository = FakeAuthRepository(),
        ),
        repository = this,
    )

    private fun unsupported(): Nothing = error("Not needed for this test")
}

private fun testSubject(): SubjectDetail = SubjectDetail(
    id = "subject-1",
    name = "SQLD",
    purpose = "exam",
    detailLabel = "자격증",
    createdAt = "2026-07-01T00:00:00",
    examSchedule = null,
    chapters = listOf(
        ChapterWithParts(
            id = "chapter-1",
            subjectId = "subject-1",
            name = "챕터 1",
            displayOrder = 1,
            parts = listOf(
                PartSummary(
                    id = "part-1",
                    chapterId = "chapter-1",
                    name = "파트 1",
                    partNumber = 1,
                    contentPreview = "기존 내용",
                ),
            ),
        ),
    ),
)

private fun testPart(): PartDetail = PartDetail(
    id = "part-1",
    chapterId = "chapter-1",
    name = "파트 1",
    partNumber = 1,
    contentPreview = "기존 내용",
    subjectId = "subject-1",
    lectureUploadId = null,
    content = "기존 내용",
)
