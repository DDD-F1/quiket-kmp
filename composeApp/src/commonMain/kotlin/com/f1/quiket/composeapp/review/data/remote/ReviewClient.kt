package com.f1.quiket.composeapp.review.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import com.f1.quiket.composeapp.review.domain.model.QuestionOption
import com.f1.quiket.composeapp.review.domain.model.QuizReview
import com.f1.quiket.composeapp.review.domain.model.QuizReviewItem
import com.f1.quiket.composeapp.review.domain.model.ReviewException
import com.f1.quiket.composeapp.review.domain.model.ReviewFilter
import com.f1.quiket.composeapp.review.domain.model.ReviewPartSummary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class ReviewClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getQuizReview(
        session: SessionSnapshot,
        resultId: String,
        filter: ReviewFilter = ReviewFilter.All,
    ): QuizReview {
        val authorization = session.authorizationHeader()
            ?: throw ReviewException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}quiz-results/$resultId/review") {
            header("Authorization", authorization)
            parameter("filter", filter.wireValue)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw ReviewException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw ReviewException(envelope.message.ifBlank { "오답노트를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw ReviewException("오답노트 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizReviewDataResponse>(data).toDomain()
        }.getOrElse {
            throw ReviewException("오답노트 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw ReviewException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

@Serializable
private data class QuizReviewDataResponse(
    val playSessionId: String,
    val items: List<QuizReviewItemResponse> = emptyList(),
) {
    fun toDomain(): QuizReview = QuizReview(
        playSessionId = playSessionId,
        items = items.map { it.toDomain() },
    )
}

@Serializable
private data class QuizReviewItemResponse(
    val questionId: String,
    val displayOrder: Int,
    val summary: String? = null,
    val body: String,
    val options: List<QuestionOptionResponse> = emptyList(),
    val selectedOptionId: String? = null,
    val selectedValue: String? = null,
    val answerValue: String? = null,
    val correctServer: Boolean,
    val skipped: Boolean = false,
    val correctExplanation: String? = null,
    val incorrectExplanation: String? = null,
    val sourcePart: PartSummaryResponse? = null,
) {
    fun toDomain(): QuizReviewItem = QuizReviewItem(
        questionId = questionId,
        displayOrder = displayOrder,
        summary = summary,
        body = body,
        options = options.map { it.toDomain() },
        selectedOptionId = selectedOptionId,
        selectedValue = selectedValue,
        answerValue = answerValue,
        correctServer = correctServer,
        skipped = skipped,
        correctExplanation = correctExplanation,
        incorrectExplanation = incorrectExplanation,
        sourcePart = sourcePart?.toDomain(),
    )
}

@Serializable
private data class QuestionOptionResponse(
    val id: String,
    val optionNumber: Int,
    val content: String,
) {
    fun toDomain(): QuestionOption = QuestionOption(
        id = id,
        optionNumber = optionNumber,
        content = content,
    )
}

@Serializable
private data class PartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
) {
    fun toDomain(): ReviewPartSummary = ReviewPartSummary(
        id = id,
        chapterId = chapterId,
        name = name,
        partNumber = partNumber,
        contentPreview = contentPreview,
    )
}

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
