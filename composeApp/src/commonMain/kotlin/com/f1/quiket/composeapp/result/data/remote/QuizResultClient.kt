package com.f1.quiket.composeapp.result.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import com.f1.quiket.composeapp.result.domain.model.QuestionOption
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.domain.model.QuizResultException
import com.f1.quiket.composeapp.result.domain.model.QuizReviewItem
import com.f1.quiket.composeapp.result.domain.model.ResultPartSummary
import com.f1.quiket.composeapp.result.domain.model.RetryAvailable
import com.f1.quiket.composeapp.result.domain.model.RewardSummary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class QuizResultClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getQuizResult(
        session: SessionSnapshot,
        resultId: String,
    ): QuizResult {
        val authorization = session.authorizationHeader()
            ?: throw QuizResultException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}quiz-results/$resultId") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw QuizResultException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw QuizResultException(envelope.message.ifBlank { "결과를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw QuizResultException("결과 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<QuizResultDataResponse>(data).toDomain()
        }.getOrElse {
            throw QuizResultException("결과 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw QuizResultException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

@Serializable
private data class QuizResultDataResponse(
    val playSessionId: String,
    val resultId: String? = null,
    val quizSessionId: String,
    val subjectId: String,
    val subjectName: String? = null,
    val totalCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skipCount: Int,
    val accuracyPct: Int,
    val elapsedMs: Int,
    val rewards: RewardSummaryResponse,
    val reviewItems: List<QuizReviewItemResponse> = emptyList(),
    val retryAvailable: RetryAvailableResponse? = null,
    val createdAt: String? = null,
) {
    fun toDomain(): QuizResult = QuizResult(
        playSessionId = playSessionId,
        resultId = resultId,
        quizSessionId = quizSessionId,
        subjectId = subjectId,
        subjectName = subjectName,
        totalCount = totalCount,
        correctCount = correctCount,
        wrongCount = wrongCount,
        skipCount = skipCount,
        accuracyPct = accuracyPct,
        elapsedMs = elapsedMs,
        rewards = rewards.toDomain(),
        reviewItems = reviewItems.map { it.toDomain() },
        retryAvailable = retryAvailable?.toDomain(),
        createdAt = createdAt,
    )
}

@Serializable
private data class RewardSummaryResponse(
    val dotoriEarned: Int,
    val xpEarned: Int,
    val leveledUp: Boolean,
    val newLevel: Int? = null,
    val currentDotoriBalance: Int? = null,
    val currentXpTotal: Int? = null,
) {
    fun toDomain(): RewardSummary = RewardSummary(
        dotoriEarned = dotoriEarned,
        xpEarned = xpEarned,
        leveledUp = leveledUp,
        newLevel = newLevel,
        currentDotoriBalance = currentDotoriBalance,
        currentXpTotal = currentXpTotal,
    )
}

@Serializable
private data class RetryAvailableResponse(
    val retryAll: Boolean = false,
    val retryWrong: Boolean = false,
    val wrongCount: Int? = null,
) {
    fun toDomain(): RetryAvailable = RetryAvailable(
        retryAll = retryAll,
        retryWrong = retryWrong,
        wrongCount = wrongCount,
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
    val sourcePart: ResultPartSummaryResponse? = null,
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
private data class ResultPartSummaryResponse(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String? = null,
) {
    fun toDomain(): ResultPartSummary = ResultPartSummary(
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
