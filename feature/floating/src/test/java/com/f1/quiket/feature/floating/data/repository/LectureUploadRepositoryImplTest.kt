package com.f1.quiket.feature.floating.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.floating.data.remote.LectureTextUploadRequest
import com.f1.quiket.feature.floating.data.remote.LectureUploadAcceptedDataResponse
import com.f1.quiket.feature.floating.data.remote.LectureUploadApi
import com.f1.quiket.feature.floating.data.remote.LectureUploadStatusDataResponse
import com.f1.quiket.feature.floating.data.remote.PartTextAddRequest
import com.f1.quiket.feature.floating.domain.model.LectureFileUpload
import com.f1.quiket.feature.floating.domain.model.LectureFileUploadType
import com.f1.quiket.feature.floating.domain.model.LectureTextUpload
import com.f1.quiket.feature.floating.domain.model.LectureUploadStatus
import com.f1.quiket.feature.floating.domain.model.PartFileAdd
import com.f1.quiket.feature.floating.domain.model.PartSplitMethod
import com.f1.quiket.feature.floating.domain.model.PartSplitPlan
import com.f1.quiket.feature.floating.domain.model.PartTextAdd
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LectureUploadRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun createTextUpload_success_mapsRequestBody() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.createTextLectureUploadHandler = { request ->
            assertThat(request).isEqualTo(
                LectureTextUploadRequest(
                    subjectId = "subject-1",
                    chapterName = "운영체제 입문",
                    uploadType = "text",
                    partSplitMethod = "manual",
                    text = "process and thread",
                    partSplitPlans = listOf(
                        com.f1.quiket.feature.floating.data.remote.PartSplitPlanRequest(
                            partNumber = 1,
                            intendedName = "프로세스",
                        ),
                    ),
                ),
            )
            successResponse(
                code = "LECTURE_UPLOAD_ACCEPTED",
                data = acceptedResponse(status = "pending"),
            )
        }

        val result = repository.createTextUpload(
            LectureTextUpload(
                subjectId = "subject-1",
                chapterName = "운영체제 입문",
                partSplitMethod = PartSplitMethod.Manual,
                text = "process and thread",
                partSplitPlans = listOf(
                    PartSplitPlan(
                        partNumber = 1,
                        intendedName = "프로세스",
                    ),
                ),
            ),
        )

        val accepted = (result as NetworkResult.Success).data
        assertThat(accepted.lectureUploadId).isEqualTo("upload-1")
        assertThat(accepted.status).isEqualTo(LectureUploadStatus.Pending)
    }

    @Test
    fun createFileUpload_success_mapsMultipartSignature() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.createFileLectureUploadHandler = { subjectId, chapterName, uploadType, splitMethod, plansJson, files ->
            assertThat(subjectId.readUtf8()).isEqualTo("subject-1")
            assertThat(chapterName?.readUtf8()).isEqualTo("운영체제 입문")
            assertThat(uploadType.readUtf8()).isEqualTo("pdf")
            assertThat(splitMethod.readUtf8()).isEqualTo("auto")
            assertThat(plansJson).isNull()
            assertThat(files).hasSize(1)
            successResponse(
                code = "LECTURE_UPLOAD_ACCEPTED",
                data = acceptedResponse(status = "processing"),
            )
        }

        val filePart = MultipartBody.Part.createFormData(
            name = "files",
            filename = "lecture.pdf",
            body = "pdf".toRequestBody("application/pdf".toMediaType()),
        )

        val result = repository.createFileUpload(
            request = LectureFileUpload(
                subjectId = "subject-1",
                chapterName = "운영체제 입문",
                uploadType = LectureFileUploadType.Pdf,
                partSplitMethod = PartSplitMethod.Auto,
            ),
            files = listOf(filePart),
        )

        assertThat((result as NetworkResult.Success).data.status)
            .isEqualTo(LectureUploadStatus.Processing)
    }

    @Test
    fun createFileUpload_manualSplit_includesPartSplitPlansJson() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.createFileLectureUploadHandler = { _, _, _, splitMethod, plansJson, files ->
            assertThat(splitMethod.readUtf8()).isEqualTo("manual")
            assertThat(plansJson?.readUtf8()).isEqualTo(
                """[{"partNumber":1,"intendedName":"프로세스"}]""",
            )
            assertThat(files).hasSize(1)
            successResponse(
                code = "LECTURE_UPLOAD_ACCEPTED",
                data = acceptedResponse(status = "pending"),
            )
        }

        val filePart = MultipartBody.Part.createFormData(
            name = "files",
            filename = "lecture.pdf",
            body = "pdf".toRequestBody("application/pdf".toMediaType()),
        )

        val result = repository.createFileUpload(
            request = LectureFileUpload(
                subjectId = "subject-1",
                chapterName = "운영체제 입문",
                uploadType = LectureFileUploadType.Pdf,
                partSplitMethod = PartSplitMethod.Manual,
                partSplitPlans = listOf(
                    PartSplitPlan(
                        partNumber = 1,
                        intendedName = "프로세스",
                    ),
                ),
            ),
            files = listOf(filePart),
        )

        assertThat((result as NetworkResult.Success).data.status)
            .isEqualTo(LectureUploadStatus.Pending)
    }

    @Test
    fun addTextPartToChapter_success_mapsRequestBody() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.addTextPartToChapterHandler = { chapterId, request ->
            assertThat(chapterId).isEqualTo("chapter-1")
            assertThat(request).isEqualTo(
                PartTextAddRequest(
                    partName = "스레드",
                    uploadType = "text",
                    text = "thread content",
                ),
            )
            successResponse(
                code = "PART_ADD_ACCEPTED",
                data = acceptedResponse(status = "pending"),
            )
        }

        val result = repository.addTextPartToChapter(
            chapterId = "chapter-1",
            request = PartTextAdd(
                partName = "스레드",
                text = "thread content",
            ),
        )

        assertThat((result as NetworkResult.Success).data.chapterId).isEqualTo("chapter-1")
    }

    @Test
    fun addFilePartToChapter_success_mapsMultipartSignature() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.addFilePartToChapterHandler = { chapterId, partName, uploadType, files ->
            assertThat(chapterId).isEqualTo("chapter-1")
            assertThat(partName.readUtf8()).isEqualTo("스레드")
            assertThat(uploadType.readUtf8()).isEqualTo("image")
            assertThat(files).hasSize(1)
            successResponse(
                code = "PART_ADD_ACCEPTED",
                data = acceptedResponse(status = "processing"),
            )
        }

        val filePart = MultipartBody.Part.createFormData(
            name = "files",
            filename = "lecture.png",
            body = "png".toRequestBody("image/png".toMediaType()),
        )

        val result = repository.addFilePartToChapter(
            chapterId = "chapter-1",
            request = PartFileAdd(
                partName = "스레드",
                uploadType = LectureFileUploadType.Image,
            ),
            files = listOf(filePart),
        )

        assertThat((result as NetworkResult.Success).data.status)
            .isEqualTo(LectureUploadStatus.Processing)
    }

    @Test
    fun getUploadStatus_failed_mapsFailReason() = runTest {
        val api = FakeLectureUploadApi()
        val repository = repository(api)

        api.getLectureUploadStatusHandler = { lectureUploadId ->
            assertThat(lectureUploadId).isEqualTo("upload-1")
            successResponse(
                code = "LECTURE_UPLOAD_STATUS_SUCCESS",
                data = LectureUploadStatusDataResponse(
                    lectureUploadId = "upload-1",
                    subjectId = "subject-1",
                    chapterId = "chapter-1",
                    status = "failed",
                    progressPct = 80,
                    failCode = "LECTURE_CONTENT_ERROR",
                    failMessage = "파일을 확인해 주세요.",
                    failReason = "OCR 실패",
                ),
            )
        }

        val result = repository.getUploadStatus("upload-1")

        val progress = (result as NetworkResult.Success).data
        assertThat(progress.status).isEqualTo(LectureUploadStatus.Failed)
        assertThat(progress.failCode).isEqualTo("LECTURE_CONTENT_ERROR")
        assertThat(progress.failMessage).isEqualTo("파일을 확인해 주세요.")
        assertThat(progress.failReason).isEqualTo("OCR 실패")
    }

    private fun repository(api: LectureUploadApi): LectureUploadRepositoryImpl =
        LectureUploadRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeLectureUploadApi : LectureUploadApi {
        var createTextLectureUploadHandler:
            suspend (LectureTextUploadRequest) -> Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            { unhandled("createTextLectureUpload") }
        var createFileLectureUploadHandler:
            suspend (RequestBody, RequestBody?, RequestBody, RequestBody, RequestBody?, List<MultipartBody.Part>) ->
                Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            { _, _, _, _, _, _ -> unhandled("createFileLectureUpload") }
        var addTextPartToChapterHandler:
            suspend (String, PartTextAddRequest) -> Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            { _, _ -> unhandled("addTextPartToChapter") }
        var addFilePartToChapterHandler:
            suspend (String, RequestBody, RequestBody, List<MultipartBody.Part>) ->
                Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            { _, _, _, _ -> unhandled("addFilePartToChapter") }
        var getLectureUploadStatusHandler:
            suspend (String) -> Response<ApiResponse<LectureUploadStatusDataResponse>> =
            { unhandled("getLectureUploadStatus") }

        override suspend fun createTextLectureUpload(
            request: LectureTextUploadRequest,
        ): Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            createTextLectureUploadHandler(request)

        override suspend fun createFileLectureUpload(
            subjectId: RequestBody,
            chapterName: RequestBody?,
            uploadType: RequestBody,
            partSplitMethod: RequestBody,
            partSplitPlansJson: RequestBody?,
            files: List<MultipartBody.Part>,
        ): Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            createFileLectureUploadHandler(
                subjectId,
                chapterName,
                uploadType,
                partSplitMethod,
                partSplitPlansJson,
                files,
            )

        override suspend fun addTextPartToChapter(
            chapterId: String,
            request: PartTextAddRequest,
        ): Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            addTextPartToChapterHandler(chapterId, request)

        override suspend fun addFilePartToChapter(
            chapterId: String,
            partName: RequestBody,
            uploadType: RequestBody,
            files: List<MultipartBody.Part>,
        ): Response<ApiResponse<LectureUploadAcceptedDataResponse>> =
            addFilePartToChapterHandler(chapterId, partName, uploadType, files)

        override suspend fun getLectureUploadStatus(
            lectureUploadId: String,
        ): Response<ApiResponse<LectureUploadStatusDataResponse>> =
            getLectureUploadStatusHandler(lectureUploadId)

        private fun <T> unhandled(method: String): T {
            error("Unhandled LectureUploadApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun acceptedResponse(status: String): LectureUploadAcceptedDataResponse =
            LectureUploadAcceptedDataResponse(
                lectureUploadId = "upload-1",
                subjectId = "subject-1",
                chapterId = "chapter-1",
                status = status,
                estimatedSeconds = 10,
            )

        fun <T : Any> successResponse(
            code: String,
            data: T,
        ): Response<ApiResponse<T>> = Response.success(
            ApiResponse(
                success = true,
                code = code,
                message = "success",
                data = data,
            ),
        )

        fun RequestBody.readUtf8(): String {
            val buffer = Buffer()
            writeTo(buffer)
            return buffer.readUtf8()
        }
    }
}
