package com.f1.quiket.feature.history.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.history.data.remote.HistoryApi
import com.f1.quiket.feature.history.data.remote.QuizAnswerSubmitItemRequest
import com.f1.quiket.feature.history.data.remote.QuizPlaySessionDataResponse
import com.f1.quiket.feature.history.data.remote.QuizResultDataResponse
import com.f1.quiket.feature.history.data.remote.QuizResultSubmitRequest
import com.f1.quiket.feature.history.data.remote.QuizRetryRequest
import com.f1.quiket.feature.history.data.remote.RecentActivityPageResponse
import com.f1.quiket.feature.history.data.remote.RecentActivityResponse
import com.f1.quiket.feature.history.data.remote.RetryQuestionAnswerResponse
import com.f1.quiket.feature.history.data.remote.RetryQuestionResponse
import com.f1.quiket.feature.history.data.remote.RetryQuizSessionResponse
import com.f1.quiket.feature.history.data.remote.RewardSummaryResponse
import com.f1.quiket.feature.history.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.history.domain.model.QuizPlayType
import com.f1.quiket.feature.history.domain.model.QuizResultSubmit
import com.f1.quiket.feature.history.domain.model.QuizRetry
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
class HistoryRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getRecentActivities_success_mapsPage() = runTest {
        val api = FakeHistoryApi()
        val repository = repository(api)

        api.getRecentActivitiesHandler = { page, size ->
            assertThat(page).isEqualTo(1)
            assertThat(size).isEqualTo(10)
            successResponse(
                code = "RECENT_ACTIVITIES_SUCCESS",
                data = RecentActivityPageResponse(
                    content = listOf(
                        RecentActivityResponse(
                            activityId = "activity-1",
                            activityType = "quiz_completed",
                            quizSessionId = "session-1",
                            playSessionId = "play-1",
                            resultId = "result-1",
                            title = "SQLD 객관식 10문제",
                            subjectId = "subject-1",
                            subjectName = "SQLD",
                            status = "completed",
                            scoreText = "8/10",
                            createdAt = "2026-05-28T20:00:00+09:00",
                        ),
                    ),
                    page = 1,
                    size = 10,
                    totalElements = 21,
                    totalPages = 3,
                    hasNext = true,
                ),
            )
        }

        val result = repository.getRecentActivities(page = 1, size = 10)

        val page = (result as NetworkResult.Success).data
        assertThat(page.activities).hasSize(1)
        assertThat(page.activities.first().playSessionId).isEqualTo("play-1")
        assertThat(page.hasNext).isTrue()
    }

    @Test
    fun submitQuizResult_success_mapsRequestBody() = runTest {
        val api = FakeHistoryApi()
        val repository = repository(api)

        api.submitQuizResultHandler = { request ->
            assertThat(request).isEqualTo(
                QuizResultSubmitRequest(
                    clientSessionId = "client-1",
                    quizSessionId = "session-1",
                    playType = "first",
                    elapsedMs = 12000,
                    questionShuffled = false,
                    optionShuffled = true,
                    answers = listOf(
                        QuizAnswerSubmitItemRequest(
                            questionId = "question-1",
                            selectedOptionId = "option-1",
                            skipped = false,
                            answerElapsedMs = 3000,
                        ),
                    ),
                ),
            )
            successResponse(
                code = "QUIZ_RESULT_SUCCESS",
                data = resultResponse(),
            )
        }

        val result = repository.submitQuizResult(
            QuizResultSubmit(
                clientSessionId = "client-1",
                quizSessionId = "session-1",
                playType = QuizPlayType.First,
                elapsedMs = 12000,
                answers = listOf(
                    QuizAnswerSubmitItem(
                        questionId = "question-1",
                        selectedOptionId = "option-1",
                        skipped = false,
                        answerElapsedMs = 3000,
                    ),
                ),
            ),
        )

        val quizResult = (result as NetworkResult.Success).data
        assertThat(quizResult.playSessionId).isEqualTo("play-1")
        assertThat(quizResult.resultId).isEqualTo("result-1")
        assertThat(quizResult.rewards.dotoriEarned).isEqualTo(10)
    }

    @Test
    fun retryAllQuestions_success_mapsQuizSessionSnapshot() = runTest {
        val api = FakeHistoryApi()
        val repository = repository(api)

        api.retryAllQuestionsHandler = { resultId, request ->
            assertThat(resultId).isEqualTo("result-1")
            assertThat(request.clientSessionId).isEqualTo("client-retry")
            successResponse(
                code = "QUIZ_RETRY_ALL_SUCCESS",
                data = playSessionResponse(),
            )
        }

        val result = repository.retryAllQuestions(
            resultId = "result-1",
            request = QuizRetry(clientSessionId = "client-retry"),
        )

        val playSession = (result as NetworkResult.Success).data
        assertThat(playSession.quizSession?.id).isEqualTo("session-retry")
        assertThat(playSession.quizSession?.questions?.first()?.answer?.answerValue)
            .isEqualTo("1")
    }

    @Test
    fun retryWrongQuestions_conflict_mapsServerFailure() = runTest {
        val api = FakeHistoryApi()
        val repository = repository(api)

        api.retryWrongQuestionsHandler = { resultId, request ->
            assertThat(resultId).isEqualTo("result-1")
            assertThat(request.clientSessionId).isEqualTo("client-retry")
            errorResponse(
                httpCode = 409,
                body = """
                    {
                      "success": false,
                      "code": "QUIZ_RETRY_WRONG_EMPTY",
                      "message": "오답 문항이 없습니다.",
                      "data": null
                    }
                """.trimIndent(),
            )
        }

        val result = repository.retryWrongQuestions(
            resultId = "result-1",
            request = QuizRetry(clientSessionId = "client-retry"),
        ) as NetworkResult.Failure

        assertThat(result.httpCode).isEqualTo(409)
        assertThat(result.code).isEqualTo("QUIZ_RETRY_WRONG_EMPTY")
    }

    private fun repository(api: HistoryApi): HistoryRepositoryImpl =
        HistoryRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeHistoryApi : HistoryApi {
        var getRecentActivitiesHandler:
            suspend (Int, Int) -> Response<ApiResponse<RecentActivityPageResponse>> =
            { _, _ -> unhandled("getRecentActivities") }
        var submitQuizResultHandler:
            suspend (QuizResultSubmitRequest) -> Response<ApiResponse<QuizResultDataResponse>> =
            { unhandled("submitQuizResult") }
        var retryAllQuestionsHandler:
            suspend (String, QuizRetryRequest) -> Response<ApiResponse<QuizPlaySessionDataResponse>> =
            { _, _ -> unhandled("retryAllQuestions") }
        var retryWrongQuestionsHandler:
            suspend (String, QuizRetryRequest) -> Response<ApiResponse<QuizPlaySessionDataResponse>> =
            { _, _ -> unhandled("retryWrongQuestions") }

        override suspend fun getRecentActivities(
            page: Int,
            size: Int,
        ): Response<ApiResponse<RecentActivityPageResponse>> =
            getRecentActivitiesHandler(page, size)

        override suspend fun submitQuizResult(
            request: QuizResultSubmitRequest,
        ): Response<ApiResponse<QuizResultDataResponse>> = submitQuizResultHandler(request)

        override suspend fun getQuizResult(
            resultId: String,
        ): Response<ApiResponse<QuizResultDataResponse>> = unhandled("getQuizResult")

        override suspend fun retryAllQuestions(
            resultId: String,
            request: QuizRetryRequest,
        ): Response<ApiResponse<QuizPlaySessionDataResponse>> =
            retryAllQuestionsHandler(resultId, request)

        override suspend fun retryWrongQuestions(
            resultId: String,
            request: QuizRetryRequest,
        ): Response<ApiResponse<QuizPlaySessionDataResponse>> =
            retryWrongQuestionsHandler(resultId, request)

        private fun <T> unhandled(method: String): T {
            error("Unhandled HistoryApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val jsonMediaType = "application/json".toMediaType()

        fun resultResponse(): QuizResultDataResponse = QuizResultDataResponse(
            playSessionId = "play-1",
            resultId = "result-1",
            quizSessionId = "session-1",
            subjectId = "subject-1",
            subjectName = "SQLD",
            totalCount = 1,
            correctCount = 1,
            wrongCount = 0,
            skipCount = 0,
            accuracyPct = 100,
            elapsedMs = 12000,
            rewards = RewardSummaryResponse(
                dotoriEarned = 10,
                xpEarned = 20,
                leveledUp = false,
            ),
        )

        fun playSessionResponse(): QuizPlaySessionDataResponse = QuizPlaySessionDataResponse(
            playSessionId = "play-retry",
            clientSessionId = "client-retry",
            quizSessionId = "session-retry",
            playType = "retry_all",
            status = "in_progress",
            quizSession = RetryQuizSessionResponse(
                id = "session-retry",
                subjectId = "subject-1",
                subjectName = "SQLD",
                quizType = "multiple_choice",
                choiceCount = 4,
                questionCount = 1,
                playMode = "all_at_once",
                difficulty = "medium",
                status = "completed",
                questions = listOf(
                    RetryQuestionResponse(
                        id = "question-1",
                        questionType = "multiple_choice",
                        difficulty = "medium",
                        body = "다음 중 정답은?",
                        displayOrder = 1,
                        options = listOf(
                            com.f1.quiket.feature.history.data.remote.QuestionOptionResponse(
                                id = "1",
                                optionNumber = 1,
                                content = "정답",
                            ),
                        ),
                        answer = RetryQuestionAnswerResponse(answerValue = "1"),
                    ),
                ),
            ),
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

        fun <T> errorResponse(
            httpCode: Int,
            body: String,
        ): Response<T> = Response.error(
            httpCode,
            body.toResponseBody(jsonMediaType),
        )
    }
}
