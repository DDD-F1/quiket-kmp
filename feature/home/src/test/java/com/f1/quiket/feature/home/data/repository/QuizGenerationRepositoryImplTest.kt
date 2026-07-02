package com.f1.quiket.feature.home.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.home.data.remote.ChapterWithPartsResponse
import com.f1.quiket.feature.home.data.remote.PartSummaryResponse
import com.f1.quiket.feature.home.data.remote.QuizCreateRequest
import com.f1.quiket.feature.home.data.remote.QuizGenerationAcceptedDataResponse
import com.f1.quiket.feature.home.data.remote.QuizGenerationApi
import com.f1.quiket.feature.home.data.remote.QuizGenerationStatusDataResponse
import com.f1.quiket.feature.home.data.remote.QuizScopeDataResponse
import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.ServerQuizType
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
class QuizGenerationRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getQuizScope_success_mapsChaptersAndParts() = runTest {
        val api = FakeQuizGenerationApi()
        val repository = repository(api)

        api.getQuizScopeHandler = { subjectId ->
            assertThat(subjectId).isEqualTo("subject-1")
            successResponse(
                code = "QUIZ_SCOPE_SUCCESS",
                data = QuizScopeDataResponse(
                    subjectId = "subject-1",
                    subjectName = "SQLD",
                    chapters = listOf(
                        ChapterWithPartsResponse(
                            id = "chapter-1",
                            subjectId = "subject-1",
                            name = "SQLD 기본",
                            displayOrder = 1,
                            parts = listOf(
                                PartSummaryResponse(
                                    id = "part-1",
                                    chapterId = "chapter-1",
                                    name = "SQLD 개요",
                                    partNumber = 1,
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        val result = repository.getQuizScope("subject-1")

        val scope = (result as NetworkResult.Success).data
        assertThat(scope.subjectName).isEqualTo("SQLD")
        assertThat(scope.chapters.single().parts.single().id).isEqualTo("part-1")
    }

    @Test
    fun createQuizSession_success_mapsRequestBody() = runTest {
        val api = FakeQuizGenerationApi()
        val repository = repository(api)

        api.createQuizSessionHandler = { request ->
            assertThat(request).isEqualTo(
                QuizCreateRequest(
                    subjectId = "subject-1",
                    partIds = listOf("part-1", "part-2"),
                    quizType = "multiple_choice",
                    choiceCount = 4,
                    questionCount = 10,
                    playMode = "all_at_once",
                    timerEnabled = false,
                    difficulty = "medium",
                ),
            )
            successResponse(
                code = "QUIZ_GENERATION_ACCEPTED",
                data = QuizGenerationAcceptedDataResponse(
                    quizSessionId = "session-1",
                    jobId = "job-1",
                    status = "pending",
                    estimatedSeconds = 20,
                ),
            )
        }

        val result = repository.createQuizSession(
            QuizCreate(
                subjectId = "subject-1",
                partIds = listOf("part-1", "part-2"),
                quizType = ServerQuizType.MultipleChoice,
                choiceCount = 4,
                questionCount = 10,
                playMode = QuizPlayMode.AllAtOnce,
                timerEnabled = false,
                difficulty = QuizDifficulty.Medium,
            ),
        )

        val accepted = (result as NetworkResult.Success).data
        assertThat(accepted.quizSessionId).isEqualTo("session-1")
        assertThat(accepted.status).isEqualTo(QuizGenerationStatus.Pending)
    }

    @Test
    fun getGenerationStatus_success_mapsCompletedProgress() = runTest {
        val api = FakeQuizGenerationApi()
        val repository = repository(api)

        api.getGenerationStatusHandler = { quizSessionId ->
            assertThat(quizSessionId).isEqualTo("session-1")
            successResponse(
                code = "QUIZ_GENERATION_STATUS_SUCCESS",
                data = QuizGenerationStatusDataResponse(
                    quizSessionId = "session-1",
                    jobId = "job-1",
                    status = "completed",
                    progressPct = 100,
                    generatedCount = 10,
                ),
            )
        }

        val result = repository.getGenerationStatus("session-1")

        val progress = (result as NetworkResult.Success).data
        assertThat(progress.status).isEqualTo(QuizGenerationStatus.Completed)
        assertThat(progress.progressPct).isEqualTo(100)
    }

    @Test
    fun createQuizSession_conflict_mapsServerFailure() = runTest {
        val api = FakeQuizGenerationApi()
        val repository = repository(api)

        api.createQuizSessionHandler = {
            errorResponse(
                httpCode = 409,
                body = """
                    {
                      "success": false,
                      "code": "QUIZ_GENERATION_ALREADY_RUNNING",
                      "message": "이미 생성 중인 퀴즈가 있습니다.",
                      "data": null
                    }
                """.trimIndent(),
            )
        }

        val result = repository.createQuizSession(
            QuizCreate(
                subjectId = "subject-1",
                partIds = listOf("part-1"),
                quizType = ServerQuizType.Ox,
                choiceCount = null,
                questionCount = 5,
                difficulty = QuizDifficulty.Easy,
            ),
        ) as NetworkResult.Failure

        assertThat(result.httpCode).isEqualTo(409)
        assertThat(result.code).isEqualTo("QUIZ_GENERATION_ALREADY_RUNNING")
    }

    private fun repository(api: QuizGenerationApi): QuizGenerationRepositoryImpl =
        QuizGenerationRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeQuizGenerationApi : QuizGenerationApi {
        var getQuizScopeHandler: suspend (String) -> Response<ApiResponse<QuizScopeDataResponse>> = {
            unhandled("getQuizScope")
        }
        var createQuizSessionHandler:
            suspend (QuizCreateRequest) -> Response<ApiResponse<QuizGenerationAcceptedDataResponse>> =
            { unhandled("createQuizSession") }
        var getGenerationStatusHandler:
            suspend (String) -> Response<ApiResponse<QuizGenerationStatusDataResponse>> =
            { unhandled("getGenerationStatus") }

        override suspend fun getQuizScope(
            subjectId: String,
        ): Response<ApiResponse<QuizScopeDataResponse>> = getQuizScopeHandler(subjectId)

        override suspend fun createQuizSession(
            request: QuizCreateRequest,
        ): Response<ApiResponse<QuizGenerationAcceptedDataResponse>> = createQuizSessionHandler(request)

        override suspend fun getQuizGenerationStatus(
            quizSessionId: String,
        ): Response<ApiResponse<QuizGenerationStatusDataResponse>> =
            getGenerationStatusHandler(quizSessionId)

        private fun <T> unhandled(method: String): T {
            error("Unhandled QuizGenerationApi call: $method")
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
