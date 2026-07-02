package com.f1.quiket.composeapp.home

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class HomeClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getHome(session: SessionSnapshot): HomeData {
        val authorization = session.authorizationHeader()
            ?: throw HomeException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}home") {
            header("Authorization", authorization)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw HomeException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw HomeException(envelope.message.ifBlank { "홈 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw HomeException("홈 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<HomeDataResponse>(data).toDomain()
        }.getOrElse {
            throw HomeException("홈 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun getSubjects(
        session: SessionSnapshot,
        page: Int = 0,
        size: Int = 50,
    ): List<SubjectSummary> {
        val authorization = session.authorizationHeader()
            ?: throw HomeException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}subjects") {
            header("Authorization", authorization)
            parameter("page", page)
            parameter("size", size)
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == 401) {
            throw HomeException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw HomeException(envelope.message.ifBlank { "과목 정보를 불러오지 못했습니다." })
        }

        val data = envelope.data ?: throw HomeException("과목 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<SubjectPageResponse>(data).content.map { it.toDomain() }
        }.getOrElse {
            throw HomeException("과목 응답을 해석하지 못했습니다.")
        }
    }

    suspend fun upsertExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
        examName: String?,
        examDate: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw HomeException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/exam-schedule") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                SubjectExamScheduleUpsertRequest(
                    examName = examName,
                    examDate = examDate,
                ),
            )
        }

        val envelope = parseEnvelope(response.bodyAsText())
        if (response.status.value == 401) {
            throw HomeException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw HomeException(envelope.message.ifBlank { "시험 일정을 저장하지 못했습니다." })
        }
    }

    suspend fun deleteExamSchedule(
        session: SessionSnapshot,
        subjectId: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw HomeException("로그인이 필요합니다.", isUnauthorized = true)

        val response = httpClient.delete("${baseUrl.ensureTrailingSlash()}subjects/$subjectId/exam-schedule") {
            header("Authorization", authorization)
        }

        val envelope = parseEnvelope(response.bodyAsText())
        if (response.status.value == 401) {
            throw HomeException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw HomeException(envelope.message.ifBlank { "시험 일정을 삭제하지 못했습니다." })
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw HomeException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

internal class HomeException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal data class HomeData(
    val user: HomeUserSummary,
    val hero: HomeHero?,
    val dDayCards: List<SubjectExamSchedule>,
    val subjects: List<SubjectSummary>,
    val recentActivities: List<RecentActivity>,
)

internal data class HomeHero(
    val hasActiveQuiz: Boolean,
    val activeQuiz: RecentActivity?,
)

internal data class HomeUserSummary(
    val nickname: String,
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val levelName: String?,
)

internal data class SubjectSummary(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
    val lastActivityAt: String?,
    val examSchedule: SubjectExamSchedule?,
)

internal data class SubjectExamSchedule(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int?,
)

internal data class RecentActivity(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String?,
    val playSessionId: String?,
    val resultId: String?,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String?,
    val progressPct: Int?,
    val scoreText: String?,
    val createdAt: String,
)

@Serializable
private data class SubjectPageResponse(
    val content: List<SubjectSummaryResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
)

@Serializable
private data class SubjectExamScheduleUpsertRequest(
    val examName: String? = null,
    val examDate: String,
)

@Serializable
private data class HomeDataResponse(
    val user: HomeUserSummaryResponse,
    val hero: HomeHeroResponse? = null,
    val dDayCards: List<SubjectExamScheduleResponse> = emptyList(),
    val subjects: List<SubjectSummaryResponse> = emptyList(),
    val recentActivities: List<RecentActivityResponse> = emptyList(),
) {
    fun toDomain(): HomeData = HomeData(
        user = user.toDomain(),
        hero = hero?.toDomain(),
        dDayCards = dDayCards.map { it.toDomain() },
        subjects = subjects.map { it.toDomain() },
        recentActivities = recentActivities.map { it.toDomain() },
    )
}

@Serializable
private data class HomeHeroResponse(
    val hasActiveQuiz: Boolean = false,
    val activeQuiz: RecentActivityResponse? = null,
) {
    fun toDomain(): HomeHero = HomeHero(
        hasActiveQuiz = hasActiveQuiz,
        activeQuiz = activeQuiz?.toDomain(),
    )
}

@Serializable
private data class HomeUserSummaryResponse(
    val nickname: String,
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val levelName: String? = null,
) {
    fun toDomain(): HomeUserSummary = HomeUserSummary(
        nickname = nickname,
        dotoriBalance = dotoriBalance,
        xpTotal = xpTotal,
        currentLevel = currentLevel,
        levelName = levelName,
    )
}

@Serializable
private data class SubjectSummaryResponse(
    val id: String,
    val name: String,
    val purpose: String,
    val chapterCount: Int,
    val partCount: Int,
    val lastActivityAt: String? = null,
    val examSchedule: SubjectExamScheduleResponse? = null,
) {
    fun toDomain(): SubjectSummary = SubjectSummary(
        id = id,
        name = name,
        purpose = purpose,
        chapterCount = chapterCount,
        partCount = partCount,
        lastActivityAt = lastActivityAt,
        examSchedule = examSchedule?.toDomain(),
    )
}

@Serializable
private data class SubjectExamScheduleResponse(
    val id: String,
    val subjectId: String,
    val examName: String,
    val examDate: String,
    val dDay: Int? = null,
) {
    fun toDomain(): SubjectExamSchedule = SubjectExamSchedule(
        id = id,
        subjectId = subjectId,
        examName = examName,
        examDate = examDate,
        dDay = dDay,
    )
}

@Serializable
private data class RecentActivityResponse(
    val activityId: String,
    val activityType: String,
    val quizSessionId: String? = null,
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
    fun toDomain(): RecentActivity = RecentActivity(
        activityId = activityId,
        activityType = activityType,
        quizSessionId = quizSessionId,
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

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
