package com.f1.quiket.feature.floating.presentation.viewmodel

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.floating.domain.model.AddSubjectState
import com.f1.quiket.feature.floating.domain.model.Certificate
import com.f1.quiket.feature.floating.domain.model.Part
import com.f1.quiket.feature.floating.domain.model.StudyPurpose
import com.f1.quiket.feature.floating.domain.model.Subject
import com.f1.quiket.feature.floating.domain.model.SubjectChapter
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectDetail
import com.f1.quiket.feature.floating.domain.model.SubjectExamSchedule
import com.f1.quiket.feature.floating.domain.model.SubjectSummary
import com.f1.quiket.feature.floating.domain.repository.SubjectRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddSubjectViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun createSubject_whenSuccess_emitsSubjectDetailReady() = runTest {
        val repository = FakeSubjectRepository(
            createSubjectResult = NetworkResult.Success(subject(id = "subject-1")),
        )
        val viewModel = AddSubjectViewModel(repository)
        val detailReadyEvents = mutableListOf<Unit>()
        backgroundScope.launch {
            viewModel.subjectDetailReady.toList(detailReadyEvents)
        }
        runCurrent()

        viewModel.createSubject(
            AddSubjectState(
                subjectName = "운영체제",
                studyPurpose = StudyPurpose.SELF_STUDY,
            ),
        )
        runCurrent()

        assertThat(repository.lastCreateRequest?.name).isEqualTo("운영체제")
        assertThat(repository.lastCreateRequest?.purpose).isEqualTo("review")
        assertThat(viewModel.createdSubjectId.value).isEqualTo("subject-1")
        assertThat(detailReadyEvents).hasSize(1)
        assertThat(viewModel.isSavingSubject.value).isFalse()
        assertThat(viewModel.subjectSaveErrorMessage.value).isNull()
    }

    @Test
    fun createSubject_whenFailure_doesNotEmitSubjectDetailReady() = runTest {
        val repository = FakeSubjectRepository(
            createSubjectResult = NetworkResult.Failure(
                code = "SUBJECT_CREATE_FAILED",
                message = "과목 생성 실패",
            ),
        )
        val viewModel = AddSubjectViewModel(repository)
        val detailReadyEvents = mutableListOf<Unit>()
        backgroundScope.launch {
            viewModel.subjectDetailReady.toList(detailReadyEvents)
        }
        runCurrent()

        viewModel.createSubject(AddSubjectState(subjectName = "운영체제"))
        runCurrent()

        assertThat(viewModel.createdSubjectId.value).isNull()
        assertThat(detailReadyEvents).isEmpty()
        assertThat(viewModel.isSavingSubject.value).isFalse()
        assertThat(viewModel.subjectSaveErrorMessage.value).isEqualTo("과목 생성 실패")
    }

    @Test
    fun updateSubjectDetails_whenFailure_doesNotEmitSubjectDetailReady() = runTest {
        val repository = FakeSubjectRepository(
            updateSubjectDetailsResult = NetworkResult.Failure(
                code = "SUBJECT_UPDATE_FAILED",
                message = "과목 수정 실패",
            ),
        )
        val viewModel = AddSubjectViewModel(repository)
        val detailReadyEvents = mutableListOf<Unit>()
        backgroundScope.launch {
            viewModel.subjectDetailReady.toList(detailReadyEvents)
        }
        runCurrent()

        viewModel.updateSubjectDetails(
            subjectId = "subject-1",
            addSubjectState = AddSubjectState(subjectName = "운영체제"),
        )
        runCurrent()

        assertThat(repository.lastUpdateSubjectId).isEqualTo("subject-1")
        assertThat(detailReadyEvents).isEmpty()
        assertThat(viewModel.isSavingSubject.value).isFalse()
        assertThat(viewModel.subjectSaveErrorMessage.value).isEqualTo("과목 수정 실패")
    }

    private class FakeSubjectRepository(
        private val subjectsResult: NetworkResult<List<SubjectSummary>> =
            NetworkResult.Success(emptyList()),
        private val createSubjectResult: NetworkResult<Subject> =
            NetworkResult.Failure(code = "TEST", message = "not configured"),
        private val updateSubjectDetailsResult: NetworkResult<Subject> =
            NetworkResult.Failure(code = "TEST", message = "not configured"),
    ) : SubjectRepository {
        var lastCreateRequest: SubjectCreate? = null
            private set
        var lastUpdateSubjectId: String? = null
            private set
        var lastUpdateRequest: SubjectCreate? = null
            private set

        override suspend fun getSubjects(page: Int, size: Int): NetworkResult<List<SubjectSummary>> =
            subjectsResult

        override suspend fun createSubject(request: SubjectCreate): NetworkResult<Subject> {
            lastCreateRequest = request
            return createSubjectResult
        }

        override suspend fun getSubject(subjectId: String): NetworkResult<SubjectDetail> =
            unhandled("getSubject")

        override suspend fun deleteSubject(subjectId: String): NetworkResult<Unit> =
            unhandled("deleteSubject")

        override suspend fun updateSubjectName(subjectId: String, name: String): NetworkResult<Subject> =
            unhandled("updateSubjectName")

        override suspend fun updateSubjectDetails(
            subjectId: String,
            request: SubjectCreate,
        ): NetworkResult<Subject> {
            lastUpdateSubjectId = subjectId
            lastUpdateRequest = request
            return updateSubjectDetailsResult
        }

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
            unhandled("getPart")

        override suspend fun updatePart(partId: String, name: String, content: String): NetworkResult<Part> =
            unhandled("updatePart")

        private fun <T> unhandled(method: String): T {
            error("Unhandled SubjectRepository call: $method")
        }
    }

    private fun subject(id: String) = Subject(
        id = id,
        name = "운영체제",
        purpose = "review",
        createdAt = "2026-06-15T00:00:00Z",
    )
}
