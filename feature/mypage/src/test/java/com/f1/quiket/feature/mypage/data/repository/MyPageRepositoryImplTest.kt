package com.f1.quiket.feature.mypage.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.core.network.retrofit.NetworkErrorMapper
import com.f1.quiket.feature.mypage.data.remote.AccountDeleteRequest
import com.f1.quiket.feature.mypage.data.remote.EmailVerificationSentDataResponse
import com.f1.quiket.feature.mypage.data.remote.FcmTokenUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.FeedbackCreateRequest
import com.f1.quiket.feature.mypage.data.remote.FeedbackDataResponse
import com.f1.quiket.feature.mypage.data.remote.GamificationDataResponse
import com.f1.quiket.feature.mypage.data.remote.MyEmailChangeConfirmRequest
import com.f1.quiket.feature.mypage.data.remote.MyEmailChangeRequest
import com.f1.quiket.feature.mypage.data.remote.MyPageApi
import com.f1.quiket.feature.mypage.data.remote.MyProfileDataResponse
import com.f1.quiket.feature.mypage.data.remote.NicknameUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.NotificationSettingsDataResponse
import com.f1.quiket.feature.mypage.data.remote.NotificationSettingsUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.PasswordChangeRequest
import com.f1.quiket.feature.mypage.domain.model.FeedbackCategory
import com.f1.quiket.feature.mypage.domain.model.FeedbackCreate
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MyPageRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getMyProfile_withoutEmail_mapsEmptyEmail() = runTest {
        val api = FakeMyPageApi()
        val repository = repository(api)

        api.getMyProfileHandler = {
            successResponse(
                code = "MY_PROFILE_SUCCESS",
                data = profileResponse(
                    nickname = "카카오사용자",
                    email = null,
                    emailVerified = false,
                    providers = listOf("kakao"),
                ),
            )
        }

        val result = repository.getMyProfile()

        val profile = (result as NetworkResult.Success).data
        assertThat(profile.email).isEmpty()
        assertThat(profile.emailVerified).isFalse()
        assertThat(profile.providers).containsExactly("kakao")
    }

    @Test
    fun updateMyNickname_success_mapsRequestBody() = runTest {
        val api = FakeMyPageApi()
        val repository = repository(api)

        api.updateMyNicknameHandler = { request ->
            assertThat(request).isEqualTo(NicknameUpdateRequest(nickname = "도토리장인"))
            successResponse(
                code = "PROFILE_UPDATE_SUCCESS",
                data = profileResponse(nickname = request.nickname),
            )
        }

        val result = repository.updateMyNickname("도토리장인")

        val profile = (result as NetworkResult.Success).data
        assertThat(profile.nickname).isEqualTo("도토리장인")
    }

    @Test
    fun requestMyEmailChange_success_mapsRequestBody() = runTest {
        val api = FakeMyPageApi()
        val repository = repository(api)

        api.requestMyEmailChangeHandler = { request ->
            assertThat(request).isEqualTo(MyEmailChangeRequest(newEmail = "new.user@example.com"))
            successResponse(
                code = "MY_EMAIL_CHANGE_VERIFICATION_SENT",
                data = EmailVerificationSentDataResponse(
                    email = request.newEmail,
                    expiresInSeconds = 600,
                ),
            )
        }

        val result = repository.requestMyEmailChange("new.user@example.com")

        val verification = (result as NetworkResult.Success).data
        assertThat(verification.email).isEqualTo("new.user@example.com")
        assertThat(verification.expiresInSeconds).isEqualTo(600)
    }

    @Test
    fun confirmMyEmailChange_success_mapsRequestBody() = runTest {
        val api = FakeMyPageApi()
        val repository = repository(api)

        api.confirmMyEmailChangeHandler = { request ->
            assertThat(request).isEqualTo(
                MyEmailChangeConfirmRequest(
                    newEmail = "new.user@example.com",
                    verificationCode = "123456",
                ),
            )
            successResponse(
                code = "MY_EMAIL_CHANGE_SUCCESS",
                data = profileResponse(
                    nickname = "도토리장인",
                    email = request.newEmail,
                ),
            )
        }

        val result = repository.confirmMyEmailChange(
            newEmail = "new.user@example.com",
            verificationCode = "123456",
        )

        val profile = (result as NetworkResult.Success).data
        assertThat(profile.email).isEqualTo("new.user@example.com")
        assertThat(profile.nickname).isEqualTo("도토리장인")
    }

    @Test
    fun createFeedback_success_mapsRequestBody() = runTest {
        val api = FakeMyPageApi()
        val repository = repository(api)

        api.createFeedbackHandler = { request ->
            assertThat(request).isEqualTo(
                FeedbackCreateRequest(
                    category = "bug",
                    body = "결과 화면 확인이 필요해요.",
                    replyEmail = "user@example.com",
                    appVersion = "1.0.0",
                    osVersion = "Android 15",
                    deviceModel = "Pixel",
                ),
            )
            successResponse(
                code = "FEEDBACK_CREATE_SUCCESS",
                data = FeedbackDataResponse(
                    id = "feedback-1",
                    category = request.category,
                    body = request.body,
                    replyEmail = request.replyEmail,
                    appVersion = request.appVersion,
                    osVersion = request.osVersion,
                    deviceModel = request.deviceModel,
                    createdAt = "2026-05-20T00:00:00Z",
                ),
            )
        }

        val result = repository.createFeedback(
            FeedbackCreate(
                category = FeedbackCategory.Bug,
                body = "결과 화면 확인이 필요해요.",
                replyEmail = "user@example.com",
                appVersion = "1.0.0",
                osVersion = "Android 15",
                deviceModel = "Pixel",
            ),
        )

        val feedback = (result as NetworkResult.Success).data
        assertThat(feedback.category).isEqualTo(FeedbackCategory.Bug)
        assertThat(feedback.id).isEqualTo("feedback-1")
    }

    private fun repository(api: MyPageApi): MyPageRepositoryImpl =
        MyPageRepositoryImpl(
            api = api,
            responseHandler = ApiResponseHandler(NetworkErrorMapper(json)),
            dispatchers = AppDispatchers(
                io = dispatcher,
                main = dispatcher,
                default = dispatcher,
            ),
        )

    private class FakeMyPageApi : MyPageApi {
        var updateMyNicknameHandler:
            suspend (NicknameUpdateRequest) -> Response<ApiResponse<MyProfileDataResponse>> =
            { unhandled("updateMyNickname") }
        var requestMyEmailChangeHandler:
            suspend (MyEmailChangeRequest) -> Response<ApiResponse<EmailVerificationSentDataResponse>> =
            { unhandled("requestMyEmailChange") }
        var confirmMyEmailChangeHandler:
            suspend (MyEmailChangeConfirmRequest) -> Response<ApiResponse<MyProfileDataResponse>> =
            { unhandled("confirmMyEmailChange") }
        var createFeedbackHandler:
            suspend (FeedbackCreateRequest) -> Response<ApiResponse<FeedbackDataResponse>> =
            { unhandled("createFeedback") }
        var getMyProfileHandler:
            suspend () -> Response<ApiResponse<MyProfileDataResponse>> =
            { unhandled("getMyProfile") }

        override suspend fun getMyGamification(): Response<ApiResponse<GamificationDataResponse>> =
            unhandled("getMyGamification")

        override suspend fun getMyProfile(): Response<ApiResponse<MyProfileDataResponse>> =
            getMyProfileHandler()

        override suspend fun updateMyNickname(
            request: NicknameUpdateRequest,
        ): Response<ApiResponse<MyProfileDataResponse>> = updateMyNicknameHandler(request)

        override suspend fun requestMyEmailChange(
            request: MyEmailChangeRequest,
        ): Response<ApiResponse<EmailVerificationSentDataResponse>> = requestMyEmailChangeHandler(request)

        override suspend fun confirmMyEmailChange(
            request: MyEmailChangeConfirmRequest,
        ): Response<ApiResponse<MyProfileDataResponse>> = confirmMyEmailChangeHandler(request)

        override suspend fun updateMyPassword(
            request: PasswordChangeRequest,
        ): Response<ApiResponse<Unit>> = unhandled("updateMyPassword")

        override suspend fun deleteMyAccount(
            request: AccountDeleteRequest,
        ): Response<ApiResponse<Unit>> = unhandled("deleteMyAccount")

        override suspend fun getNotificationSettings(): Response<ApiResponse<NotificationSettingsDataResponse>> =
            unhandled("getNotificationSettings")

        override suspend fun updateNotificationSettings(
            request: NotificationSettingsUpdateRequest,
        ): Response<ApiResponse<NotificationSettingsDataResponse>> = unhandled("updateNotificationSettings")

        override suspend fun updateFcmToken(
            request: FcmTokenUpdateRequest,
        ): Response<ApiResponse<Unit>> = unhandled("updateFcmToken")

        override suspend fun createFeedback(
            request: FeedbackCreateRequest,
        ): Response<ApiResponse<FeedbackDataResponse>> = createFeedbackHandler(request)

        private fun <T> unhandled(method: String): T {
            error("Unhandled MyPageApi call: $method")
        }
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun profileResponse(
            nickname: String,
            email: String? = "user@example.com",
            emailVerified: Boolean = true,
            providers: List<String> = listOf("local"),
        ): MyProfileDataResponse = MyProfileDataResponse(
            id = "user-1",
            email = email,
            nickname = nickname,
            dotoriBalance = 20,
            emailVerified = emailVerified,
            status = "active",
            providers = providers,
            xpTotal = 100,
            currentLevel = 2,
            createdAt = "2026-05-20T00:00:00Z",
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
    }
}
