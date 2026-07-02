package com.f1.quiket.feature.review.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.review.data.remote.QuestionOptionResponse
import com.f1.quiket.feature.review.data.remote.QuizReviewDataResponse
import com.f1.quiket.feature.review.data.remote.QuizReviewItemResponse
import com.f1.quiket.feature.review.data.remote.ReviewApi
import com.f1.quiket.feature.review.domain.model.QuizReviewFilter
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getQuizResultReview_success_mapsWrongFilterAndItems() = runTest {
        val api = FakeReviewApi()
        val repository = repository(api)

        api.getQuizResultReviewHandler = { resultId, filter ->
            assertThat(resultId).isEqualTo("result-1")
            assertThat(filter).isEqualTo("wrong")
            successResponse(
                code = "QUIZ_REVIEW_SUCCESS",
                data = QuizReviewDataResponse(
                    playSessionId = "play-1",
                    items = listOf(
                        QuizReviewItemResponse(
                            questionId = "question-1",
                            displayOrder = 1,
                            body = "정답은?",
                            options = listOf(
                                QuestionOptionResponse(
                                    id = "option-1",
                                    optionNumber = 1,
                                    content = "정답",
                                ),
                            ),
                            answerValue = "option-1",
                            correctServer = false,
                            incorrectExplanation = "다시 확인해보세요.",
                        ),
                    ),
                ),
            )
        }

        val result = repository.getQuizResultReview(
            resultId = "result-1",
            filter = QuizReviewFilter.Wrong,
        )

        val review = (result as NetworkResult.Success).data
        assertThat(review.items.single().questionId).isEqualTo("question-1")
        assertThat(review.items.single().options.single().content).isEqualTo("정답")
    }

    private fun repository(api: ReviewApi): ReviewRepositoryImpl =
        ReviewRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeReviewApi : ReviewApi {
        var getQuizResultReviewHandler:
            suspend (String, String) -> Response<ApiResponse<QuizReviewDataResponse>> =
            { _, _ -> unhandled("getQuizResultReview") }

        override suspend fun getQuizResultReview(
            resultId: String,
            filter: String,
        ): Response<ApiResponse<QuizReviewDataResponse>> =
            getQuizResultReviewHandler(resultId, filter)

        private fun <T> unhandled(method: String): T {
            error("Unhandled ReviewApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

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
    }
}
