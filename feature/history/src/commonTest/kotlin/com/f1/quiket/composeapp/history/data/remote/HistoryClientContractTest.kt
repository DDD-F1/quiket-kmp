package com.f1.quiket.composeapp.history.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.history.domain.model.HistoryActivityType
import com.f1.quiket.composeapp.history.domain.model.HistoryException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HistoryClientContractTest {
    @Test
    fun sendsAuthenticatedPagingRequestAndMapsKnownAndUnknownActivities() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/home/recent-activities", request.url.encodedPath)
            assertEquals("2", request.url.parameters["page"])
            assertEquals("20", request.url.parameters["size"])
            assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
            respondJson(
                body = """
                    {
                      "success": true,
                      "code": "OK",
                      "message": "",
                      "data": {
                        "content": [
                          {
                            "activityId": "activity-1",
                            "activityType": "quiz_completed",
                            "quizSessionId": "quiz-1",
                            "clientSessionId": "client-1",
                            "playSessionId": "play-1",
                            "resultId": "result-1",
                            "title": "퀴즈 완료",
                            "subjectId": "subject-1",
                            "subjectName": "SQLD",
                            "status": "completed",
                            "progressPct": 100,
                            "scoreText": "8/10",
                            "createdAt": "2026-07-14T10:00:00"
                          },
                          {
                            "activityId": "activity-2",
                            "activityType": "future_activity",
                            "title": "새 활동",
                            "subjectId": "subject-2",
                            "subjectName": "정보처리기사",
                            "createdAt": "2026-07-14T11:00:00"
                          }
                        ],
                        "page": 2,
                        "size": 20,
                        "totalElements": 42,
                        "totalPages": 3,
                        "hasNext": true
                      }
                    }
                """.trimIndent(),
            )
        }
        val client = historyClient(engine)

        val page = client.getRecentActivities(
            session = authenticatedSession(),
            page = 2,
            size = 20,
        )

        assertEquals(42, page.totalElements)
        assertTrue(page.hasNext)
        assertEquals(HistoryActivityType.QuizCompleted, page.activities[0].activityType)
        assertEquals(HistoryActivityType.Unknown, page.activities[1].activityType)
        assertEquals("client-1", page.activities[0].clientSessionId)
    }

    @Test
    fun mapsUnauthorizedEnvelopeToHistoryException() = runTest {
        val engine = MockEngine {
            respondJson(
                status = HttpStatusCode.Unauthorized,
                body = """{"success":false,"code":"UNAUTHORIZED","message":"세션 만료","data":null}""",
            )
        }
        val client = historyClient(engine)

        val error = assertFailsWith<HistoryException> {
            client.getRecentActivities(authenticatedSession(), page = 0, size = 10)
        }

        assertTrue(error.isUnauthorized)
        assertEquals("세션 만료", error.message)
    }

    @Test
    fun rejectsMissingAccessTokenBeforeSendingRequest() = runTest {
        val engine = MockEngine {
            error("HTTP request must not be sent without a token")
        }
        val client = historyClient(engine)

        val error = assertFailsWith<HistoryException> {
            client.getRecentActivities(
                session = authenticatedSession().copy(accessToken = null),
                page = 0,
                size = 10,
            )
        }

        assertTrue(error.isUnauthorized)
        assertEquals("로그인이 필요합니다.", error.message)
    }
}

private fun historyClient(engine: MockEngine): HistoryClient = HistoryClient(
    httpClient = HttpClient(engine) {
        expectSuccess = false
    },
    json = Json { ignoreUnknownKeys = true },
    baseUrl = "https://example.test/api/v1",
)

private fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
) = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)

private fun authenticatedSession(): SessionSnapshot = SessionSnapshot(
    onboardingCompleted = true,
    homeGuideCompleted = true,
    accessToken = "access-token",
    refreshToken = "refresh-token",
    tokenType = "Bearer",
    nickname = "테스터",
    accessTokenExpiresIn = 3600,
    refreshTokenExpiresIn = 7200,
)
