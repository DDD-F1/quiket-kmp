package com.f1.quiket.composeapp.history.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.HistoryActivity
import com.f1.quiket.composeapp.history.domain.model.HistoryActivityType
import com.f1.quiket.composeapp.history.domain.model.HistoryException
import com.f1.quiket.composeapp.history.domain.model.RecentActivityPage
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class HistoryClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getRecentActivities(
        session: SessionSnapshot,
        page: Int,
        size: Int,
    ): RecentActivityPage {
        val authorization = session.authorizationHeader()
            ?: throw HistoryException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}home/recent-activities") {
            header("Authorization", authorization)
            parameter("page", page)
            parameter("size", size)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw HistoryException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw HistoryException(envelope.message.ifBlank { "기록을 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw HistoryException("기록 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<RecentActivityPageResponse>(data).toDomain()
        }.getOrElse {
            throw HistoryException("기록 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw HistoryException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

@Serializable
private data class RecentActivityPageResponse(
    val content: List<RecentActivityResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
) {
    fun toDomain(): RecentActivityPage = RecentActivityPage(
        activities = content.map { it.toDomain() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext,
    )
}

@Serializable
private data class RecentActivityResponse(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String? = null,
    val clientSessionId: String? = null,
    val playSessionId: String? = null,
    val resultId: String? = null,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String? = null,
    val progressPct: Int? = null,
    val scoreText: String? = null,
    val createdAt: String,
) {
    fun toDomain(): HistoryActivity = HistoryActivity(
        activityId = activityId,
        activityType = activityType.toActivityType(),
        quizSessionId = quizSessionId,
        clientSessionId = clientSessionId,
        playSessionId = playSessionId,
        resultId = resultId,
        title = title,
        subjectId = subjectId,
        subjectName = subjectName,
        status = status,
        progressPct = progressPct,
        scoreText = scoreText,
        createdAt = createdAt,
    )
}

private fun String.toActivityType(): HistoryActivityType =
    HistoryActivityType.entries.firstOrNull { it.wireValue == this }
        ?: HistoryActivityType.Unknown

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
