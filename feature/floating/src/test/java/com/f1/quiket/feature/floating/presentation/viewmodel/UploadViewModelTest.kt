package com.f1.quiket.feature.floating.presentation.viewmodel

import androidx.test.core.app.ApplicationProvider
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.feature.floating.domain.model.LectureFileUpload
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadAccepted
import com.f1.quiket.feature.floating.domain.model.LectureUploadProgress
import com.f1.quiket.feature.floating.domain.model.LectureUploadStatus
import com.f1.quiket.feature.floating.domain.model.PartFileAdd
import com.f1.quiket.feature.floating.domain.model.PartTextAdd
import com.f1.quiket.feature.floating.domain.repository.LectureUploadRepository
import com.f1.quiket.feature.floating.presentation.component.upload.PartClassifyMethod
import com.f1.quiket.feature.floating.presentation.component.upload.UploadTab
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UploadViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submit_whenPollingReturnsFailedWithChapterId_showsFailureInsteadOfSuccess() = runTest {
        val repository = FakeLectureUploadRepository()
        repository.createTextUploadResult = NetworkResult.Success(acceptedUpload())
        repository.uploadStatusResult = NetworkResult.Success(
            LectureUploadProgress(
                lectureUploadId = "upload-1",
                subjectId = "subject-1",
                chapterId = "chapter-1",
                status = LectureUploadStatus.Failed,
                estimatedSeconds = null,
                chapterName = "운영체제 입문",
                progressPct = 80,
                parts = emptyList(),
                failCode = "LECTURE_CONTENT_ERROR",
                failMessage = "파일을 확인해 주세요.",
                failReason = "OCR 실패",
            ),
        )
        val viewModel = UploadViewModel(
            context = ApplicationProvider.getApplicationContext(),
            lectureUploadRepository = repository,
        )

        viewModel.submit(
            subjectId = "subject-1",
            tab = UploadTab.TEXT,
            classifyMethod = PartClassifyMethod.AI,
            manualSections = emptyList(),
            files = emptyList(),
            images = emptyList(),
            text = "process and thread",
        )
        runCurrent()
        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(repository.requestedUploadStatusId).isEqualTo("upload-1")
        assertThat(viewModel.isSuccess.value).isFalse()
        assertThat(viewModel.isFailed.value).isTrue()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.uploadedChapterId.value).isEqualTo("chapter-1")
        assertThat(viewModel.errorMessage.value).isEqualTo("파일을 확인해 주세요.")
    }

    @Test
    fun submit_whenPollingFailsConsecutively_stopsPollingAndShowsFailure() = runTest {
        val repository = FakeLectureUploadRepository()
        repository.createTextUploadResult = NetworkResult.Success(acceptedUpload())
        repository.uploadStatusResults += listOf(
            NetworkResult.Failure(code = "NETWORK_ERROR", message = "네트워크 오류"),
            NetworkResult.Failure(code = "NETWORK_ERROR", message = "네트워크 오류"),
            NetworkResult.Failure(code = "NETWORK_ERROR", message = "네트워크 오류"),
        )
        val viewModel = UploadViewModel(
            context = ApplicationProvider.getApplicationContext(),
            lectureUploadRepository = repository,
        )

        viewModel.submit(
            subjectId = "subject-1",
            tab = UploadTab.TEXT,
            classifyMethod = PartClassifyMethod.AI,
            manualSections = emptyList(),
            files = emptyList(),
            images = emptyList(),
            text = "process and thread",
        )
        runCurrent()

        advanceTimeBy(2_000L)
        runCurrent()
        assertThat(viewModel.isLoading.value).isTrue()
        assertThat(viewModel.isFailed.value).isFalse()

        advanceTimeBy(2_000L)
        runCurrent()
        assertThat(viewModel.isLoading.value).isTrue()
        assertThat(viewModel.isFailed.value).isFalse()

        advanceTimeBy(2_000L)
        runCurrent()

        assertThat(repository.getUploadStatusCallCount).isEqualTo(3)
        assertThat(viewModel.isSuccess.value).isFalse()
        assertThat(viewModel.isFailed.value).isTrue()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.errorMessage.value)
            .isEqualTo("업로드 상태를 확인하지 못했어요. 네트워크 상태를 확인한 뒤 다시 시도해주세요.")
    }

    private class FakeLectureUploadRepository : LectureUploadRepository {
        var createTextUploadResult: NetworkResult<LectureUploadAccepted> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
        var uploadStatusResult: NetworkResult<LectureUploadProgress> =
            NetworkResult.Failure(code = "TEST", message = "not configured")
        val uploadStatusResults = mutableListOf<NetworkResult<LectureUploadProgress>>()
        var requestedUploadStatusId: String? = null
            private set
        var getUploadStatusCallCount = 0
            private set

        override suspend fun createTextUpload(
            request: LectureTextUpload,
        ): NetworkResult<LectureUploadAccepted> = createTextUploadResult

        override suspend fun createFileUpload(
            request: LectureFileUpload,
            files: List<MultipartBody.Part>,
        ): NetworkResult<LectureUploadAccepted> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun addTextPartToChapter(
            chapterId: String,
            request: PartTextAdd,
        ): NetworkResult<LectureUploadAccepted> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun addFilePartToChapter(
            chapterId: String,
            request: PartFileAdd,
            files: List<MultipartBody.Part>,
        ): NetworkResult<LectureUploadAccepted> =
            NetworkResult.Failure(code = "TEST", message = "not configured")

        override suspend fun getUploadStatus(
            lectureUploadId: String,
        ): NetworkResult<LectureUploadProgress> {
            requestedUploadStatusId = lectureUploadId
            getUploadStatusCallCount += 1
            return if (uploadStatusResults.isNotEmpty()) {
                uploadStatusResults.removeAt(0)
            } else {
                uploadStatusResult
            }
        }
    }

    private fun acceptedUpload() = LectureUploadAccepted(
        lectureUploadId = "upload-1",
        subjectId = "subject-1",
        chapterId = "",
        status = LectureUploadStatus.Pending,
        estimatedSeconds = 3,
    )
}
