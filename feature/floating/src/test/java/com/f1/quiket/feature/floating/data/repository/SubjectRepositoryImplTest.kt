package com.f1.quiket.feature.floating.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.floating.data.remote.CertificateResponse
import com.f1.quiket.feature.floating.data.remote.ChapterNameUpdateRequest
import com.f1.quiket.feature.floating.data.remote.ChapterResponse
import com.f1.quiket.feature.floating.data.remote.PageResponse
import com.f1.quiket.feature.floating.data.remote.PartResponse
import com.f1.quiket.feature.floating.data.remote.PartUpdateRequest
import com.f1.quiket.feature.floating.data.remote.SubjectApi
import com.f1.quiket.feature.floating.data.remote.SubjectCreateRequest
import com.f1.quiket.feature.floating.data.remote.SubjectExamScheduleResponse
import com.f1.quiket.feature.floating.data.remote.SubjectExamScheduleUpsertRequest
import com.f1.quiket.feature.floating.data.remote.SubjectNameUpdateRequest
import com.f1.quiket.feature.floating.data.remote.SubjectResponse
import com.f1.quiket.feature.floating.data.remote.SubjectSummaryResponse
import com.f1.quiket.feature.floating.data.remote.SubjectDetailResponse
import com.f1.quiket.feature.floating.domain.model.SubjectCreate
import com.f1.quiket.feature.floating.domain.model.SubjectReviewDetail
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun createSubject_success_mapsRequestBody() = runTest {
        val api = FakeSubjectApi()
        val repository = repository(api)

        api.createSubjectHandler = { request ->
            assertThat(request).isEqualTo(
                SubjectCreateRequest(
                    name = "운영체제",
                    purpose = "review",
                    reviewDetail = com.f1.quiket.feature.floating.data.remote.SubjectReviewDetailRequest(
                        field = "cs",
                        studyLevel = "basic",
                    ),
                ),
            )
            successResponse(
                code = "SUBJECT_CREATE_SUCCESS",
                data = SubjectResponse(
                    id = "subject-1",
                    name = request.name,
                    purpose = request.purpose,
                    createdAt = "2026-05-20T00:00:00Z",
                ),
            )
        }

        val result = repository.createSubject(
            SubjectCreate(
                name = "운영체제",
                purpose = "review",
                reviewDetail = SubjectReviewDetail(
                    field = "cs",
                    studyLevel = "basic",
                ),
            ),
        )

        val subject = (result as NetworkResult.Success).data
        assertThat(subject.id).isEqualTo("subject-1")
        assertThat(subject.name).isEqualTo("운영체제")
    }

    @Test
    fun updateChapterName_success_mapsSubjectChapter() = runTest {
        val api = FakeSubjectApi()
        val repository = repository(api)

        api.updateChapterNameHandler = { chapterId, request ->
            assertThat(chapterId).isEqualTo("chapter-1")
            assertThat(request.name).isEqualTo("새 챕터")
            successResponse(
                code = "CHAPTER_UPDATE_SUCCESS",
                data = ChapterResponse(
                    id = "chapter-1",
                    subjectId = "subject-1",
                    name = "새 챕터",
                    displayOrder = 1,
                ),
            )
        }

        val result = repository.updateChapterName("chapter-1", "새 챕터")

        val chapter = (result as NetworkResult.Success).data
        assertThat(chapter.id).isEqualTo("chapter-1")
        assertThat(chapter.name).isEqualTo("새 챕터")
    }

    @Test
    fun getSubjects_httpError_mapsFailure() = runTest {
        val api = FakeSubjectApi()
        val repository = repository(api)

        api.getSubjectsHandler = { _, _ ->
            errorResponse(
                httpCode = 403,
                body = """
                    {
                      "success": false,
                      "code": "SUBJECT_FORBIDDEN",
                      "message": "권한이 없습니다.",
                      "data": null
                    }
                """.trimIndent(),
            )
        }

        val result = repository.getSubjects() as NetworkResult.Failure

        assertThat(result.httpCode).isEqualTo(403)
        assertThat(result.code).isEqualTo("SUBJECT_FORBIDDEN")
    }

    private fun repository(api: SubjectApi): SubjectRepositoryImpl = SubjectRepositoryImpl(
        api = api,
        responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
        dispatchers = AppDispatchers(
            io = dispatcher,
            main = dispatcher,
            default = dispatcher,
        ),
    )

    private class FakeSubjectApi : SubjectApi {
        var getSubjectsHandler:
            suspend (Int, Int) -> Response<ApiResponse<PageResponse<SubjectSummaryResponse>>> =
            { _, _ -> unhandled("getSubjects") }
        var createSubjectHandler:
            suspend (SubjectCreateRequest) -> Response<ApiResponse<SubjectResponse>> =
            { unhandled("createSubject") }
        var updateChapterNameHandler:
            suspend (String, ChapterNameUpdateRequest) -> Response<ApiResponse<ChapterResponse>> =
            { _, _ -> unhandled("updateChapterName") }

        override suspend fun getSubjects(
            page: Int,
            size: Int,
        ): Response<ApiResponse<PageResponse<SubjectSummaryResponse>>> =
            getSubjectsHandler(page, size)

        override suspend fun createSubject(
            request: SubjectCreateRequest,
        ): Response<ApiResponse<SubjectResponse>> = createSubjectHandler(request)

        override suspend fun getSubject(
            subjectId: String,
        ): Response<ApiResponse<SubjectDetailResponse>> = unhandled("getSubject")

        override suspend fun deleteSubject(
            subjectId: String,
        ): Response<ApiResponse<Unit>> = unhandled("deleteSubject")

        override suspend fun updateSubjectName(
            subjectId: String,
            request: SubjectNameUpdateRequest,
        ): Response<ApiResponse<SubjectResponse>> = unhandled("updateSubjectName")

        override suspend fun updateSubjectDetails(
            subjectId: String,
            request: SubjectCreateRequest,
        ): Response<ApiResponse<SubjectResponse>> = unhandled("updateSubjectDetails")

        override suspend fun upsertSubjectExamSchedule(
            subjectId: String,
            request: SubjectExamScheduleUpsertRequest,
        ): Response<ApiResponse<SubjectExamScheduleResponse>> = unhandled("upsertSubjectExamSchedule")

        override suspend fun deleteSubjectExamSchedule(
            subjectId: String,
        ): Response<ApiResponse<Unit>> = unhandled("deleteSubjectExamSchedule")

        override suspend fun getCertificates(): Response<ApiResponse<List<CertificateResponse>>> =
            unhandled("getCertificates")

        override suspend fun deleteChapter(
            chapterId: String,
        ): Response<ApiResponse<Unit>> = unhandled("deleteChapter")

        override suspend fun updateChapterName(
            chapterId: String,
            request: ChapterNameUpdateRequest,
        ): Response<ApiResponse<ChapterResponse>> = updateChapterNameHandler(chapterId, request)

        override suspend fun getPart(partId: String): Response<ApiResponse<PartResponse>> =
            unhandled("getPart")

        override suspend fun updatePart(
            partId: String,
            request: PartUpdateRequest,
        ): Response<ApiResponse<PartResponse>> = unhandled("updatePart")

        private fun <T> unhandled(method: String): T {
            error("Unhandled SubjectApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val jsonMediaType = "application/json".toMediaType()

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

        fun <T> errorResponse(
            httpCode: Int,
            body: String,
        ): Response<T> = Response.error(
            httpCode,
            body.toResponseBody(jsonMediaType),
        )
    }
}
