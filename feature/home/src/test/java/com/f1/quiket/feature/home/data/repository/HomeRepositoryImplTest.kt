package com.f1.quiket.feature.home.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.home.data.remote.HomeApi
import com.f1.quiket.feature.home.data.remote.HomeDataResponse
import com.f1.quiket.feature.home.data.remote.HomeHeroResponse
import com.f1.quiket.feature.home.data.remote.HomeUserSummaryResponse
import com.f1.quiket.feature.home.data.remote.PageResponse
import com.f1.quiket.feature.home.data.remote.RecentActivityResponse
import com.f1.quiket.feature.home.data.remote.SubjectExamScheduleResponse
import com.f1.quiket.feature.home.data.remote.SubjectSummaryResponse
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
class HomeRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getHome_success_mapsHeroAndDDayCards() = runTest {
        val api = FakeHomeApi()
        val repository = repository(api)

        api.getHomeHandler = {
            successResponse(
                code = "HOME_SUCCESS",
                data = homeDataResponse(),
            )
        }

        val result = repository.getHome()

        val home = (result as NetworkResult.Success).data
        assertThat(home.hero?.hasActiveQuiz).isTrue()
        assertThat(home.hero?.activeQuiz?.quizSessionId).isEqualTo("quiz-session-1")
        assertThat(home.dDayCards.first().examName).isEqualTo("SQLD 정기시험")
        assertThat(home.subjects.first().examSchedule?.dDay).isEqualTo(3)
    }

    @Test
    fun getSubjects_success_mapsPageContent() = runTest {
        val api = FakeHomeApi()
        val repository = repository(api)

        api.getSubjectsHandler = { page, size ->
            assertThat(page).isEqualTo(0)
            assertThat(size).isEqualTo(50)
            successResponse(
                code = "SUBJECT_LIST_SUCCESS",
                data = PageResponse(
                    content = listOf(
                        SubjectSummaryResponse(
                            id = "subject-1",
                            name = "SQLD",
                            purpose = "exam",
                            chapterCount = 3,
                            partCount = 7,
                        ),
                    ),
                ),
            )
        }

        val result = repository.getSubjects()

        val subjects = (result as NetworkResult.Success).data
        assertThat(subjects).hasSize(1)
        assertThat(subjects.first().id).isEqualTo("subject-1")
        assertThat(subjects.first().partCount).isEqualTo(7)
    }

    @Test
    fun getRecentActivities_emptyPage_returnsEmptyList() = runTest {
        val api = FakeHomeApi()
        val repository = repository(api)

        api.getRecentActivitiesHandler = { _, _ ->
            successResponse(
                code = "HOME_RECENT_ACTIVITIES_SUCCESS",
                data = PageResponse(content = emptyList<RecentActivityResponse>()),
            )
        }

        val result = repository.getRecentActivities()

        assertThat((result as NetworkResult.Success).data).isEmpty()
    }

    @Test
    fun getHome_unauthorized_mapsLoginRequiredFailure() = runTest {
        val api = FakeHomeApi()
        val repository = repository(api)

        api.getHomeHandler = {
            errorResponse(
                httpCode = 401,
                body = """
                    {
                      "success": false,
                      "code": "AUTH_LOGIN_REQUIRED",
                      "message": "로그인이 필요합니다.",
                      "data": null
                    }
                """.trimIndent(),
            )
        }

        val result = repository.getHome() as NetworkResult.Failure

        assertThat(result.httpCode).isEqualTo(401)
        assertThat(result.code).isEqualTo("AUTH_LOGIN_REQUIRED")
        assertThat(result.message).isEqualTo("로그인이 필요합니다.")
    }

    private fun repository(api: HomeApi): HomeRepositoryImpl = HomeRepositoryImpl(
        api = api,
        responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
        dispatchers = AppDispatchers(
            io = dispatcher,
            main = dispatcher,
            default = dispatcher,
        ),
    )

    private class FakeHomeApi : HomeApi {
        var getHomeHandler: suspend () -> Response<ApiResponse<HomeDataResponse>> = {
            unhandled("getHome")
        }
        var getRecentActivitiesHandler:
            suspend (Int, Int) -> Response<ApiResponse<PageResponse<RecentActivityResponse>>> =
            { _, _ -> unhandled("getRecentActivities") }
        var getSubjectsHandler:
            suspend (Int, Int) -> Response<ApiResponse<PageResponse<SubjectSummaryResponse>>> =
            { _, _ -> unhandled("getSubjects") }

        override suspend fun getHome(): Response<ApiResponse<HomeDataResponse>> =
            getHomeHandler()

        override suspend fun getRecentActivities(
            page: Int,
            size: Int,
        ): Response<ApiResponse<PageResponse<RecentActivityResponse>>> =
            getRecentActivitiesHandler(page, size)

        override suspend fun getSubjects(
            page: Int,
            size: Int,
        ): Response<ApiResponse<PageResponse<SubjectSummaryResponse>>> =
            getSubjectsHandler(page, size)

        private fun <T> unhandled(method: String): T {
            error("Unhandled HomeApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val jsonMediaType = "application/json".toMediaType()

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

        fun homeDataResponse(): HomeDataResponse = HomeDataResponse(
            user = HomeUserSummaryResponse(
                nickname = "tester",
                dotoriBalance = 10,
                xpTotal = 100,
                currentLevel = 2,
            ),
            hero = HomeHeroResponse(
                hasActiveQuiz = true,
                activeQuiz = RecentActivityResponse(
                    activityId = "activity-1",
                    activityType = "quiz_generating",
                    quizSessionId = "quiz-session-1",
                    title = "SQLD 퀴즈",
                    subjectId = "subject-1",
                    subjectName = "SQLD",
                    createdAt = "2026-05-21T09:00:00+09:00",
                ),
            ),
            dDayCards = listOf(
                SubjectExamScheduleResponse(
                    id = "schedule-1",
                    subjectId = "subject-1",
                    examName = "SQLD 정기시험",
                    examDate = "2026-06-30",
                    dDay = 3,
                ),
            ),
            subjects = listOf(
                SubjectSummaryResponse(
                    id = "subject-1",
                    name = "SQLD",
                    purpose = "exam",
                    chapterCount = 3,
                    partCount = 7,
                    lastActivityAt = "2026-05-20T09:00:00+09:00",
                    examSchedule = SubjectExamScheduleResponse(
                        id = "schedule-1",
                        subjectId = "subject-1",
                        examName = "SQLD 정기시험",
                        examDate = "2026-06-30",
                        dDay = 3,
                    ),
                ),
            ),
        )
    }
}
