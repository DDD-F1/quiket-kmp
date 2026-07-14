package com.f1.quiket.composeapp.history.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.HistoryActivity
import com.f1.quiket.composeapp.history.domain.model.HistoryActivityType
import com.f1.quiket.composeapp.history.domain.model.HistoryException
import com.f1.quiket.composeapp.history.domain.model.RecentActivityPage
import com.f1.quiket.composeapp.network.ApiException
import com.f1.quiket.composeapp.network.decodeApiData
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

        return try {
            response.decodeApiData<RecentActivityPageResponse>(
                json = json,
                missingMessage = "기록 응답에 데이터가 없습니다.",
                failureMessage = "기록을 불러오지 못했습니다.",
                dataFailureMessage = "기록 응답을 해석하지 못했습니다.",
            ).toDomain()
        } catch (error: ApiException) {
            throw HistoryException(
                message = error.message.orEmpty(),
                isUnauthorized = error.isUnauthorized,
            )
        }
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
