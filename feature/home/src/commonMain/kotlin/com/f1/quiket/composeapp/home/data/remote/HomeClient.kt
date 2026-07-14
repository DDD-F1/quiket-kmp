package com.f1.quiket.composeapp.home.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.home.domain.model.HomeData
import com.f1.quiket.composeapp.home.domain.model.HomeException
import com.f1.quiket.composeapp.home.domain.model.HomeHero
import com.f1.quiket.composeapp.home.domain.model.HomeUserSummary
import com.f1.quiket.composeapp.home.domain.model.RecentActivity
import com.f1.quiket.composeapp.home.domain.model.SubjectExamSchedule
import com.f1.quiket.composeapp.home.domain.model.SubjectSummary
import com.f1.quiket.composeapp.network.ApiException
import com.f1.quiket.composeapp.network.decodeApiData
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import com.f1.quiket.composeapp.network.requireApiSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

        return response.decodeHomeData<HomeDataResponse>(
            missingMessage = "홈 응답에 데이터가 없습니다.",
            failureMessage = "홈 정보를 불러오지 못했습니다.",
            dataFailureMessage = "홈 응답을 해석하지 못했습니다.",
        ).toDomain()
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

        return response.decodeHomeData<SubjectPageResponse>(
            missingMessage = "과목 응답에 데이터가 없습니다.",
            failureMessage = "과목 정보를 불러오지 못했습니다.",
            dataFailureMessage = "과목 응답을 해석하지 못했습니다.",
        ).content.map { it.toDomain() }
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

        response.requireHomeSuccess(failureMessage = "시험 일정을 저장하지 못했습니다.")
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

        response.requireHomeSuccess(failureMessage = "시험 일정을 삭제하지 못했습니다.")
    }

    private suspend inline fun <reified T> io.ktor.client.statement.HttpResponse.decodeHomeData(
        missingMessage: String,
        failureMessage: String,
        dataFailureMessage: String,
    ): T = try {
        decodeApiData(
            json = json,
            missingMessage = missingMessage,
            failureMessage = failureMessage,
            dataFailureMessage = dataFailureMessage,
        )
    } catch (error: ApiException) {
        throw error.toHomeException()
    }

    private suspend fun io.ktor.client.statement.HttpResponse.requireHomeSuccess(
        failureMessage: String,
    ) {
        try {
            requireApiSuccess(json = json, failureMessage = failureMessage)
        } catch (error: ApiException) {
            throw error.toHomeException()
        }
    }

    private fun ApiException.toHomeException(): HomeException = HomeException(
        message = message.orEmpty(),
        isUnauthorized = isUnauthorized,
    )
}

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
