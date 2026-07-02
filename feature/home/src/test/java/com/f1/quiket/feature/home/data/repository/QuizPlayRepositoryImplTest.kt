package com.f1.quiket.feature.home.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.home.data.remote.QuestionAnswerResponse
import com.f1.quiket.feature.home.data.remote.QuestionOptionResponse
import com.f1.quiket.feature.home.data.remote.QuestionResponse
import com.f1.quiket.feature.home.data.remote.QuizPlayApi
import com.f1.quiket.feature.home.data.remote.QuizPlaySessionDataResponse
import com.f1.quiket.feature.home.data.remote.QuizPlayStartRequest
import com.f1.quiket.feature.home.data.remote.QuizRetryRequest
import com.f1.quiket.feature.home.data.remote.QuizAnswerSubmitItemRequest
import com.f1.quiket.feature.home.data.remote.QuizResultDataResponse
import com.f1.quiket.feature.home.data.remote.QuizResultSubmitRequest
import com.f1.quiket.feature.home.data.remote.QuizSessionDataResponse
import com.f1.quiket.feature.home.data.remote.RewardSummaryResponse
import com.f1.quiket.feature.home.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlaySessionStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class QuizPlayRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getQuizSession_success_mapsQuestionsAndPlayMode() = runTest {
        val api = FakeQuizPlayApi()
        val repository = repository(api)

        api.getQuizSessionHandler = { quizSessionId ->
            assertThat(quizSessionId).isEqualTo("session-1")
            successResponse(
                code = "QUIZ_SESSION_SUCCESS",
                data = quizSessionResponse(playMode = "one_by_one"),
            )
        }

        val result = repository.getQuizSession("session-1")

        val quizSession = (result as NetworkResult.Success).data
        assertThat(quizSession.playMode).isEqualTo(QuizPlayMode.OneByOne)
        assertThat(quizSession.questions.single().options.single().content).isEqualTo("정답")
    }

    @Test
    fun startQuizPlaySession_success_mapsRequestBody() = runTest {
        val api = FakeQuizPlayApi()
        val repository = repository(api)

        api.startQuizPlaySessionHandler = { quizSessionId, request ->
            assertThat(quizSessionId).isEqualTo("session-1")
            assertThat(request).isEqualTo(
                QuizPlayStartRequest(
                    clientSessionId = "client-1",
                    playType = "first",
                    questionShuffled = true,
                    optionShuffled = false,
                    shuffleSeed = "seed-1",
                ),
            )
            successResponse(
                code = "QUIZ_PLAY_SESSION_CREATED",
                data = QuizPlaySessionDataResponse(
                    playSessionId = "play-1",
                    clientSessionId = "client-1",
                    quizSessionId = "session-1",
                    playType = "first",
                    status = "in_progress",
                ),
            )
        }

        val result = repository.startQuizPlaySession(
            quizSessionId = "session-1",
            request = QuizPlayStart(
                clientSessionId = "client-1",
                playType = QuizPlayType.First,
                questionShuffled = true,
                optionShuffled = false,
                shuffleSeed = "seed-1",
            ),
        )

        val playSession = (result as NetworkResult.Success).data
        assertThat(playSession.playSessionId).isEqualTo("play-1")
        assertThat(playSession.status).isEqualTo(QuizPlaySessionStatus.InProgress)
    }

    @Test
    fun submitQuizResult_success_mapsRequestBody() = runTest {
        val api = FakeQuizPlayApi()
        val repository = repository(api)

        api.submitQuizResultHandler = { request ->
            assertThat(request).isEqualTo(
                QuizResultSubmitRequest(
                    clientSessionId = "client-1",
                    quizSessionId = "session-1",
                    playType = "first",
                    elapsedMs = 0,
                    optionShuffled = false,
                    answers = listOf(
                        QuizAnswerSubmitItemRequest(
                            questionId = "question-1",
                            selectedOptionId = "option-1",
                            correctClient = true,
                            skipped = false,
                        ),
                    ),
                ),
            )
            successResponse(
                code = "QUIZ_RESULT_SUBMITTED",
                data = quizResultResponse(),
            )
        }

        val result = repository.submitQuizResult(
            QuizResultSubmit(
                clientSessionId = "client-1",
                quizSessionId = "session-1",
                playType = QuizPlayType.First,
                elapsedMs = 0,
                optionShuffled = false,
                answers = listOf(
                    QuizAnswerSubmitItem(
                        questionId = "question-1",
                        selectedOptionId = "option-1",
                        correctClient = true,
                        skipped = false,
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
    fun getQuizResult_success_mapsResult() = runTest {
        val api = FakeQuizPlayApi()
        val repository = repository(api)

        api.getQuizResultHandler = { resultId ->
            assertThat(resultId).isEqualTo("result-1")
            successResponse(
                code = "QUIZ_RESULT_SUCCESS",
                data = quizResultResponse(),
            )
        }

        val result = repository.getQuizResult("result-1")

        val quizResult = (result as NetworkResult.Success).data
        assertThat(quizResult.correctCount).isEqualTo(1)
        assertThat(quizResult.accuracyPct).isEqualTo(100)
    }

    @Test
    fun retryAllQuestions_success_mapsRequestAndRetryPlaySession() = runTest {
        val api = FakeQuizPlayApi()
        val repository = repository(api)

        api.retryAllQuestionsHandler = { resultId, request ->
            assertThat(resultId).isEqualTo("result-1")
            assertThat(request).isEqualTo(
                QuizRetryRequest(
                    clientSessionId = "client-retry",
                    questionShuffled = false,
                    optionShuffled = false,
                ),
            )
            successResponse(
                code = "QUIZ_RETRY_ALL_SUCCESS",
                data = QuizPlaySessionDataResponse(
                    playSessionId = "play-retry",
                    clientSessionId = "client-retry",
                    quizSessionId = "session-retry",
                    playType = "retry_all",
                    status = "in_progress",
                    quizSession = quizSessionResponse(playMode = "all_at_once"),
                ),
            )
        }

        val result = repository.retryAllQuestions(
            resultId = "result-1",
            request = QuizRetry(clientSessionId = "client-retry"),
        )

        val playSession = (result as NetworkResult.Success).data
        assertThat(playSession.playSessionId).isEqualTo("play-retry")
        assertThat(playSession.playType).isEqualTo(QuizPlayType.RetryAll)
        assertThat(playSession.quizSession?.questions).hasSize(1)
    }

    private fun repository(api: QuizPlayApi): QuizPlayRepositoryImpl =
        QuizPlayRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeQuizPlayApi : QuizPlayApi {
        var getQuizSessionHandler:
            suspend (String) -> Response<ApiResponse<QuizSessionDataResponse>> =
            { unhandled("getQuizSession") }
        var startQuizPlaySessionHandler:
            suspend (String, QuizPlayStartRequest) -> Response<ApiResponse<QuizPlaySessionDataResponse>> =
            { _, _ -> unhandled("startQuizPlaySession") }
        var submitQuizResultHandler:
            suspend (QuizResultSubmitRequest) -> Response<ApiResponse<QuizResultDataResponse>> =
            { unhandled("submitQuizResult") }
        var getQuizResultHandler:
            suspend (String) -> Response<ApiResponse<QuizResultDataResponse>> =
            { unhandled("getQuizResult") }
        var retryAllQuestionsHandler:
            suspend (String, QuizRetryRequest) -> Response<ApiResponse<QuizPlaySessionDataResponse>> =
            { _, _ -> unhandled("retryAllQuestions") }
        var retryWrongQuestionsHandler:
            suspend (String, QuizRetryRequest) -> Response<ApiResponse<QuizPlaySessionDataResponse>> =
            { _, _ -> unhandled("retryWrongQuestions") }

        override suspend fun getQuizSession(
            quizSessionId: String,
        ): Response<ApiResponse<QuizSessionDataResponse>> = getQuizSessionHandler(quizSessionId)

        override suspend fun startQuizPlaySession(
            quizSessionId: String,
            request: QuizPlayStartRequest,
        ): Response<ApiResponse<QuizPlaySessionDataResponse>> =
            startQuizPlaySessionHandler(quizSessionId, request)

        override suspend fun submitQuizResult(
            request: QuizResultSubmitRequest,
        ): Response<ApiResponse<QuizResultDataResponse>> = submitQuizResultHandler(request)

        override suspend fun getQuizResult(
            resultId: String,
        ): Response<ApiResponse<QuizResultDataResponse>> = getQuizResultHandler(resultId)

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
            error("Unhandled QuizPlayApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun quizSessionResponse(playMode: String): QuizSessionDataResponse = QuizSessionDataResponse(
            id = "session-1",
            subjectId = "subject-1",
            subjectName = "SQLD",
            quizType = "multiple_choice",
            choiceCount = 4,
            questionCount = 1,
            playMode = playMode,
            difficulty = "easy",
            status = "completed",
            questions = listOf(
                QuestionResponse(
                    id = "question-1",
                    subjectId = "subject-1",
                    partId = "part-1",
                    partName = "SQLD 개요",
                    questionType = "multiple_choice",
                    difficulty = "easy",
                    body = "정답은?",
                    displayOrder = 1,
                    options = listOf(
                        QuestionOptionResponse(
                            id = "option-1",
                            optionNumber = 1,
                            content = "정답",
                        ),
                    ),
                    answer = QuestionAnswerResponse(answerValue = "option-1"),
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

        fun quizResultResponse(): QuizResultDataResponse = QuizResultDataResponse(
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
            elapsedMs = 0,
            rewards = RewardSummaryResponse(
                dotoriEarned = 10,
                xpEarned = 5,
                leveledUp = false,
            ),
        )
    }
}
