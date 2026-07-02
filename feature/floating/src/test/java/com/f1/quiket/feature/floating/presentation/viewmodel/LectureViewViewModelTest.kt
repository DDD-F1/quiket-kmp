package com.f1.quiket.feature.floating.presentation.viewmodel

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.floating.domain.model.Certificate
import com.f1.quiket.feature.floating.domain.model.ChapterWithParts
import com.f1.quiket.feature.floating.domain.model.Part
import com.f1.quiket.feature.floating.domain.model.PartSummary
import com.f1.quiket.feature.floating.domain.model.Subject
import com.f1.quiket.feature.floating.domain.model.SubjectChapter
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.SubjectExamSchedule
import com.f1.quiket.feature.floating.domain.model.SubjectSummary
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LectureViewViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updatePartName_beforePartLoaded_doesNotCallRepositoryAndEmitsMessage() = runTest {
        val repository = FakeSubjectRepository()
        val viewModel = LectureViewViewModel(repository)
        val event = async { viewModel.events.first() }
        runCurrent()

        viewModel.updatePartName(partId = "part-1", name = "새 파트명")
        runCurrent()

        assertThat(repository.lastUpdatedPartId).isNull()
        assertThat(event.await())
            .isEqualTo(LectureViewEvent.ShowMessage("파트 정보를 불러온 뒤 다시 시도해주세요."))
    }

    @Test
    fun updatePartName_whenSuccess_updatesCurrentNameAndTocWithRequestedName() = runTest {
        val repository = FakeSubjectRepository(
            getSubjectResult = NetworkResult.Success(subjectDetail()),
            getPartResult = NetworkResult.Success(part(name = "기존 파트명", content = "본문")),
            updatePartResult = NetworkResult.Success(part(name = "기존 파트명", content = "본문")),
        )
        val viewModel = LectureViewViewModel(repository)
        val event = async { viewModel.events.first() }
        runCurrent()

        viewModel.loadSubject(subjectId = "subject-1", initialChapterId = "chapter-1")
        advanceUntilIdle()
        viewModel.updatePartName(partId = "part-1", name = "  새 파트명  ")
        advanceUntilIdle()

        assertThat(repository.lastUpdatedPartId).isEqualTo("part-1")
        assertThat(repository.lastUpdatedPartName).isEqualTo("새 파트명")
        assertThat(repository.lastUpdatedPartContent).isEqualTo("본문")
        assertThat(viewModel.currentPartName.value).isEqualTo("새 파트명")
        assertThat(viewModel.tocChapters.value.first().parts.first().title).isEqualTo("새 파트명")
        assertThat(viewModel.isUpdatingPartName.value).isFalse()
        assertThat(event.await()).isEqualTo(LectureViewEvent.PartNameUpdated("새 파트명"))
    }

    @Test
    fun updatePartName_whenSubjectReloadReturnsStaleName_keepsRequestedNameInToc() = runTest {
        val repository = FakeSubjectRepository(
            getSubjectResult = NetworkResult.Success(subjectDetail()),
            getPartResult = NetworkResult.Success(part(name = "기존 파트명", content = "본문")),
            updatePartResult = NetworkResult.Success(part(name = "기존 파트명", content = "본문")),
        )
        val viewModel = LectureViewViewModel(repository)

        viewModel.loadSubject(subjectId = "subject-1", initialChapterId = "chapter-1")
        advanceUntilIdle()
        viewModel.updatePartName(partId = "part-1", name = "새 파트명")
        advanceUntilIdle()
        viewModel.loadSubject(subjectId = "subject-1", initialChapterId = "chapter-1")
        advanceUntilIdle()

        assertThat(viewModel.currentPartName.value).isEqualTo("새 파트명")
        assertThat(viewModel.tocChapters.value.first().parts.first().title).isEqualTo("새 파트명")
    }

    @Test
    fun updatePartName_whenFailure_keepsCurrentNameAndEmitsMessage() = runTest {
        val repository = FakeSubjectRepository(
            getSubjectResult = NetworkResult.Success(subjectDetail()),
            getPartResult = NetworkResult.Success(part(name = "기존 파트명", content = "본문")),
            updatePartResult = NetworkResult.Failure(
                code = "PART_UPDATE_FAILED",
                message = "파트 수정 실패",
            ),
        )
        val viewModel = LectureViewViewModel(repository)
        val event = async { viewModel.events.first() }
        runCurrent()

        viewModel.loadSubject(subjectId = "subject-1", initialChapterId = "chapter-1")
        advanceUntilIdle()
        viewModel.updatePartName(partId = "part-1", name = "새 파트명")
        advanceUntilIdle()

        assertThat(repository.lastUpdatedPartId).isEqualTo("part-1")
        assertThat(viewModel.currentPartName.value).isEqualTo("기존 파트명")
        assertThat(viewModel.tocChapters.value.first().parts.first().title).isEqualTo("기존 파트명")
        assertThat(viewModel.isUpdatingPartName.value).isFalse()
        assertThat(event.await()).isEqualTo(LectureViewEvent.ShowMessage("파트 수정 실패"))
    }

    private class FakeSubjectRepository(
        private val getSubjectResult: NetworkResult<SubjectDetail> =
            NetworkResult.Failure(code = "TEST", message = "not configured"),
        private val getPartResult: NetworkResult<Part> =
            NetworkResult.Failure(code = "TEST", message = "not configured"),
        private val updatePartResult: NetworkResult<Part> =
            NetworkResult.Failure(code = "TEST", message = "not configured"),
    ) : SubjectRepository {
        var lastUpdatedPartId: String? = null
            private set
        var lastUpdatedPartName: String? = null
            private set
        var lastUpdatedPartContent: String? = null
            private set

        override suspend fun getSubjects(page: Int, size: Int): NetworkResult<List<SubjectSummary>> =
            unhandled("getSubjects")

        override suspend fun createSubject(request: SubjectCreate): NetworkResult<Subject> =
            unhandled("createSubject")

        override suspend fun getSubject(subjectId: String): NetworkResult<SubjectDetail> =
            getSubjectResult

        override suspend fun deleteSubject(subjectId: String): NetworkResult<Unit> =
            unhandled("deleteSubject")

        override suspend fun updateSubjectName(subjectId: String, name: String): NetworkResult<Subject> =
            unhandled("updateSubjectName")

        override suspend fun updateSubjectDetails(
            subjectId: String,
            request: SubjectCreate,
        ): NetworkResult<Subject> = unhandled("updateSubjectDetails")

        override suspend fun upsertExamSchedule(
            subjectId: String,
            examName: String?,
            examDate: String,
        ): NetworkResult<SubjectExamSchedule> = unhandled("upsertExamSchedule")

        override suspend fun deleteExamSchedule(subjectId: String): NetworkResult<Unit> =
            unhandled("deleteExamSchedule")

        override suspend fun getCertificates(): NetworkResult<List<Certificate>> =
            unhandled("getCertificates")

        override suspend fun deleteChapter(chapterId: String): NetworkResult<Unit> =
            unhandled("deleteChapter")

        override suspend fun updateChapterName(chapterId: String, name: String): NetworkResult<SubjectChapter> =
            unhandled("updateChapterName")

        override suspend fun getPart(partId: String): NetworkResult<Part> =
            getPartResult

        override suspend fun updatePart(partId: String, name: String, content: String): NetworkResult<Part> {
            lastUpdatedPartId = partId
            lastUpdatedPartName = name
            lastUpdatedPartContent = content
            return updatePartResult
        }

        private fun <T> unhandled(method: String): T {
            error("Unhandled SubjectRepository call: $method")
        }
    }

    private fun subjectDetail() = SubjectDetail(
        id = "subject-1",
        name = "운영체제",
        purpose = "review",
        createdAt = "2026-06-15T00:00:00Z",
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
                        name = "기존 파트명",
                        partNumber = 1,
                        contentPreview = null,
                    ),
                ),
            ),
        ),
    )

    private fun part(
        name: String,
        content: String?,
    ) = Part(
        id = "part-1",
        chapterId = "chapter-1",
        name = name,
        partNumber = 1,
        content = content,
    )
}
