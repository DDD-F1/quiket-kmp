package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.subject.domain.model.ChapterWithParts
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadAccepted
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadProgress
import com.f1.quiket.composeapp.subject.domain.model.LectureUploadStatus
import com.f1.quiket.composeapp.subject.domain.model.LectureFileUploadType
import com.f1.quiket.composeapp.subject.domain.model.PickedUploadFile
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod
import com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan
import com.f1.quiket.composeapp.subject.domain.model.SubjectException
import com.f1.quiket.composeapp.subject.domain.repository.SubjectRepository
import com.f1.quiket.composeapp.subject.domain.usecase.SubjectUseCases
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextLectureUploadStateHolderTest {
    @Test
    fun submitCompletedUploadCallsCompletedCallbackImmediately() = runTest {
        val completedIds = mutableListOf<String>()
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                LectureUploadAccepted(
                    lectureUploadId = "upload-completed",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Completed,
                    estimatedSeconds = null,
                )
            },
        )
        val stateHolder = TextLectureUploadStateHolder(
            subjectUseCases = repository.toUseCases(),
        )

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))

        stateHolder.submit(
            onSessionExpired = {},
            onUploadCompleted = { completedIds += it },
        )

        assertEquals(listOf("upload-completed"), completedIds)
        assertEquals(1, repository.createFileLectureUploadCalls)
        assertEquals(0, repository.getLectureUploadStatusCalls)
        assertFalse(stateHolder.isUploading)
        assertFalse(stateHolder.isError)
        assertFalse(stateHolder.uploadFailed)
        assertEquals(100, stateHolder.progressPercent)
        assertEquals("자료 업로드가 완료됐어요", stateHolder.feedbackMessage)
        assertEquals(0, stateHolder.selectedPdfFiles.size)
    }

    @Test
    fun submitPollsUntilServerFailedAndMarksFailure() = runTest {
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                LectureUploadAccepted(
                    lectureUploadId = "upload-failed",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Processing,
                    estimatedSeconds = null,
                )
            },
            getLectureUploadStatusAction = { callIndex, lectureUploadId ->
                if (callIndex == 1) {
                    failProgress(
                        lectureUploadId = lectureUploadId,
                        failReason = "분석을 실패했어요",
                    )
                } else {
                    failProgress(
                        lectureUploadId = lectureUploadId,
                        failReason = "분석을 실패했어요",
                    )
                }
            },
        )
        val stateHolder = TextLectureUploadStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))
        var callbackCalled = false
        stateHolder.submit(
            onSessionExpired = {},
            onUploadCompleted = { callbackCalled = true },
        )

        assertFalse(callbackCalled)
        assertEquals(1, repository.getLectureUploadStatusCalls)
        assertTrue(stateHolder.isError)
        assertEquals(true, stateHolder.uploadFailed)
        assertEquals("분석을 실패했어요", stateHolder.feedbackMessage)
        assertEquals(10, stateHolder.progressPercent)
    }

    @Test
    fun submitRetriesAfterTransientFailuresUntilThreshold() = runTest {
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                LectureUploadAccepted(
                    lectureUploadId = "upload-unstable",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Processing,
                    estimatedSeconds = null,
                )
            },
            getLectureUploadStatusAction = { _, _ ->
                throw IllegalStateException("network error")
            },
        )
        val stateHolder = TextLectureUploadStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))
        stateHolder.submit(onSessionExpired = {}, onUploadCompleted = {})

        assertEquals(3, repository.getLectureUploadStatusCalls)
        assertTrue(stateHolder.isError)
        assertTrue(stateHolder.uploadFailed)
        assertEquals(
            "업로드 상태를 확인하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.",
            stateHolder.feedbackMessage,
        )
    }

    @Test
    fun submitKeepsPollingUntilTimeoutAndMarksError() = runTest {
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                LectureUploadAccepted(
                    lectureUploadId = "upload-timeout",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Pending,
                    estimatedSeconds = null,
                )
            },
            getLectureUploadStatusAction = { _, lectureUploadId ->
                LectureUploadProgress(
                    lectureUploadId = lectureUploadId,
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Pending,
                    estimatedSeconds = null,
                    chapterName = null,
                    progressPct = 15,
                    parts = emptyList(),
                    failCode = null,
                    failMessage = null,
                    failReason = null,
                )
            },
        )
        val stateHolder = TextLectureUploadStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))
        stateHolder.submit(onSessionExpired = {}, onUploadCompleted = {})

        assertTrue(stateHolder.isError)
        assertTrue(stateHolder.uploadFailed)
        assertEquals(
            "자료 분석 시간이 예상보다 길어지고 있어요. 잠시 후 과목 화면에서 다시 확인해주세요.",
            stateHolder.feedbackMessage,
        )
        assertEquals(60, repository.getLectureUploadStatusCalls)
    }

    @Test
    fun submitPollingUnauthorizedCallsSessionExpiredOnceAndClearsPollingState() = runTest {
        var sessionExpiredCount = 0
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                LectureUploadAccepted(
                    lectureUploadId = "upload-unauthorized",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = LectureUploadStatus.Processing,
                    estimatedSeconds = null,
                )
            },
            getLectureUploadStatusAction = { _, _ ->
                throw SubjectException("세션이 만료되었어요", isUnauthorized = true)
            },
        )
        val stateHolder = TextLectureUploadStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))
        stateHolder.submit(
            onSessionExpired = { sessionExpiredCount += 1 },
            onUploadCompleted = {},
        )

        assertEquals(1, sessionExpiredCount)
        assertEquals(2, repository.getLectureUploadStatusCalls)
        assertFalse(stateHolder.isUploading)
        assertFalse(stateHolder.isError)
        assertFalse(stateHolder.uploadFailed)
        assertEquals(null, stateHolder.feedbackMessage)
        assertEquals(0, stateHolder.progressPercent)
    }

    @Test
    fun resetUploadFailureClearsFailureStateAndAllowsRetry() = runTest {
        var uploadCalls = 0
        val repository = UploadFakeSubjectRepository(
            createFileLectureUploadAction = { _, _, _, _, _, _, _ ->
                uploadCalls += 1
                LectureUploadAccepted(
                    lectureUploadId = "upload-retry-$uploadCalls",
                    subjectId = "subject-id",
                    chapterId = "chapter-id",
                    status = if (uploadCalls == 1) LectureUploadStatus.Processing else LectureUploadStatus.Completed,
                    estimatedSeconds = null,
                )
            },
            getLectureUploadStatusAction = { _, lectureUploadId ->
                failProgress(
                    lectureUploadId = lectureUploadId,
                    failReason = "처음 업로드는 실패",
                )
            },
        )
        val stateHolder = TextLectureUploadStateHolder(subjectUseCases = repository.toUseCases())

        stateHolder.bindSubject(subject(), null)
        stateHolder.handlePickedFiles(listOf(lectureUploadFile("lecture.pdf")))
        stateHolder.submit(onSessionExpired = {}, onUploadCompleted = {})

        assertTrue(stateHolder.uploadFailed)
        assertEquals("처음 업로드는 실패", stateHolder.feedbackMessage)

        stateHolder.resetUploadFailure()
        assertFalse(stateHolder.uploadFailed)
        assertFalse(stateHolder.isError)
        assertEquals(null, stateHolder.feedbackMessage)
        assertEquals(0, stateHolder.progressPercent)
        assertFalse(stateHolder.isUploading)

        val completedIds = mutableListOf<String>()
        repository.getLectureUploadStatusAction = { _, lectureUploadId ->
            failProgress(
                lectureUploadId = lectureUploadId,
                failReason = "ignored",
            ).copy(status = LectureUploadStatus.Completed)
        }
        stateHolder.submit(
            onSessionExpired = {},
            onUploadCompleted = { completedIds += it },
        )

        assertEquals(listOf("upload-retry-2"), completedIds)
        assertFalse(stateHolder.uploadFailed)
        assertEquals("자료 업로드가 완료됐어요", stateHolder.feedbackMessage)
    }
}

private fun subject(): SubjectDetail = SubjectDetail(
    id = "subject-id",
    name = "SQLD",
    purpose = "exam",
    detailLabel = "자격증",
    examDetail = null,
    reviewDetail = null,
    otherDetail = null,
    createdAt = "2026-07-01T00:00:00",
    examSchedule = null,
    chapters = listOf(
        ChapterWithParts(
            id = "chapter-1",
            subjectId = "subject-id",
            name = "챕터 1",
            displayOrder = 1,
            parts = emptyList(),
        ),
    ),
)

private fun lectureUploadFile(name: String): PickedUploadFile = PickedUploadFile(
    name = name,
    mimeType = "application/pdf",
    sizeBytes = 4L,
    previewBytes = null,
    cachePath = "/tmp/quiket-${Random.nextLong()}",
)

private fun failProgress(
    lectureUploadId: String,
    failReason: String,
): LectureUploadProgress = LectureUploadProgress(
    lectureUploadId = lectureUploadId,
    subjectId = "subject-id",
    chapterId = "chapter-id",
    status = LectureUploadStatus.Failed,
    estimatedSeconds = null,
    chapterName = null,
    progressPct = 10,
    parts = emptyList(),
    failCode = null,
    failMessage = null,
    failReason = failReason,
)

private class UploadFakeSubjectRepository(
    var createTextLectureUploadAction: suspend (
        SessionSnapshot,
        String,
        String?,
        String,
        PartSplitMethod,
        List<PartSplitPlan>,
    ) -> LectureUploadAccepted = { _, _, _, _, _, _ ->
        error("Not used")
    },
    var createFileLectureUploadAction: suspend (
        SessionSnapshot,
        String,
        String?,
        LectureFileUploadType,
        List<PickedUploadFile>,
        PartSplitMethod,
        List<PartSplitPlan>,
    ) -> LectureUploadAccepted = { _, _, _, _, _, _, _ ->
        error("Not used")
    },
    var addTextPartToChapterAction: suspend (
        SessionSnapshot,
        String,
        String,
        String,
    ) -> LectureUploadAccepted = { _, _, _, _ ->
        error("Not used")
    },
    var addFilePartToChapterAction: suspend (
        SessionSnapshot,
        String,
        String,
        LectureFileUploadType,
        List<PickedUploadFile>,
    ) -> LectureUploadAccepted = { _, _, _, _, _ ->
        error("Not used")
    },
    var getLectureUploadStatusAction: suspend (
        Int,
        String,
    ) -> LectureUploadProgress = { callIndex, lectureUploadId ->
        error("Not used: lectureUploadId=$lectureUploadId, call=$callIndex")
    },
) : SubjectRepository {
    var createTextLectureUploadCalls = 0
    var createFileLectureUploadCalls = 0
    var getLectureUploadStatusCalls = 0

    override suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): List<com.f1.quiket.composeapp.subject.domain.model.SubjectListItem> = unsupported()

    override suspend fun createSubject(
        session: SessionSnapshot,
        input: com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput,
    ): com.f1.quiket.composeapp.subject.domain.model.CreatedSubject = unsupported()

    override suspend fun getSubject(session: SessionSnapshot, subjectId: String): SubjectDetail = subject()

    override suspend fun deleteSubject(session: SessionSnapshot, subjectId: String) = Unit

    override suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ): com.f1.quiket.composeapp.subject.domain.model.SubjectExamSchedule = unsupported()

    override suspend fun deleteExamSchedule(session: SessionSnapshot, subjectId: String) = Unit

    override suspend fun getCertificates(session: SessionSnapshot): List<com.f1.quiket.composeapp.subject.domain.model.Certificate> = unsupported()

    override suspend fun updateSubjectName(
        session: SessionSnapshot,
        subjectId: String,
        name: String,
    ): com.f1.quiket.composeapp.subject.domain.model.CreatedSubject = unsupported()

    override suspend fun updateSubjectDetails(
        session: SessionSnapshot,
        subjectId: String,
        input: com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput,
    ): com.f1.quiket.composeapp.subject.domain.model.CreatedSubject = unsupported()

    override suspend fun updateChapterName(session: SessionSnapshot, chapterId: String, name: String): com.f1.quiket.composeapp.subject.domain.model.Chapter = unsupported()

    override suspend fun deleteChapter(session: SessionSnapshot, chapterId: String) = Unit

    override suspend fun getPart(session: SessionSnapshot, partId: String): com.f1.quiket.composeapp.subject.domain.model.PartDetail =
        unsupported()

    override suspend fun updatePart(
        session: SessionSnapshot,
        partId: String,
        name: String,
        content: String,
    ): com.f1.quiket.composeapp.subject.domain.model.PartDetail = unsupported()

    override suspend fun createTextLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        text: String,
        partSplitMethod: com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod,
        partSplitPlans: List<com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan>,
    ): LectureUploadAccepted {
        createTextLectureUploadCalls += 1
        return createTextLectureUploadAction(
            session,
            subjectId,
            chapterName,
            text,
            partSplitMethod,
            partSplitPlans,
        )
    }

    override suspend fun createFileLectureUpload(
        session: SessionSnapshot,
        subjectId: String,
        chapterName: String?,
        uploadType: com.f1.quiket.composeapp.subject.domain.model.LectureFileUploadType,
        files: List<PickedUploadFile>,
        partSplitMethod: com.f1.quiket.composeapp.subject.domain.model.PartSplitMethod,
        partSplitPlans: List<com.f1.quiket.composeapp.subject.domain.model.PartSplitPlan>,
    ): LectureUploadAccepted {
        createFileLectureUploadCalls += 1
        return createFileLectureUploadAction(
            session,
            subjectId,
            chapterName,
            uploadType,
            files,
            partSplitMethod,
            partSplitPlans,
        )
    }

    override suspend fun addTextPartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        text: String,
    ): LectureUploadAccepted {
        return addTextPartToChapterAction(session, chapterId, partName, text)
    }

    override suspend fun addFilePartToChapter(
        session: SessionSnapshot,
        chapterId: String,
        partName: String,
        uploadType: LectureFileUploadType,
        files: List<PickedUploadFile>,
    ): LectureUploadAccepted {
        return addFilePartToChapterAction(session, chapterId, partName, uploadType, files)
    }

    override suspend fun getLectureUploadStatus(session: SessionSnapshot, lectureUploadId: String): LectureUploadProgress {
        getLectureUploadStatusCalls += 1
        return getLectureUploadStatusAction(getLectureUploadStatusCalls, lectureUploadId)
    }

    fun toUseCases(): SubjectUseCases = SubjectUseCases(
        authenticatedCallRunner = AuthenticatedCallRunner(
            sessionRepository = FakeSessionRepository(),
            authRepository = FakeAuthRepository(),
        ),
        repository = this,
    )

    private fun unsupported(): Nothing = error("Not needed for this test")
}
