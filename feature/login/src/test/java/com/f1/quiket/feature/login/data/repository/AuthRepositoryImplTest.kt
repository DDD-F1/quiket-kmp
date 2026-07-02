package com.f1.quiket.feature.login.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.auth.AuthTokenStore
import com.f1.quiket.core.network.auth.TokenPair
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.feature.login.data.remote.AuthApi
import com.f1.quiket.feature.login.data.remote.AuthErrorMapper
import com.f1.quiket.feature.login.data.remote.AuthTokenDataResponse
import com.f1.quiket.feature.login.data.remote.AuthUserResponse
import com.f1.quiket.feature.login.data.remote.EmailAvailabilityDataResponse
import com.f1.quiket.feature.login.data.remote.EmailVerificationConfirmRequest
import com.f1.quiket.feature.login.data.remote.EmailVerificationRequest
import com.f1.quiket.feature.login.data.remote.EmailVerificationSentDataResponse
import com.f1.quiket.feature.login.data.remote.KakaoAccountLinkRequest
import com.f1.quiket.feature.login.data.remote.KakaoLoginRequest
import com.f1.quiket.feature.login.data.remote.KakaoNicknameRequest
import com.f1.quiket.feature.login.data.remote.LoginRequest
import com.f1.quiket.feature.login.data.remote.LogoutRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetConfirmRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetRequestedDataResponse
import com.f1.quiket.feature.login.data.remote.RefreshTokenRequest
import com.f1.quiket.feature.login.data.remote.SignupDataResponse
import com.f1.quiket.feature.login.data.remote.SignupRequest
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.FieldError
import com.f1.quiket.feature.login.domain.model.KakaoAccountLinkRequired
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.model.KakaoNicknameRequired
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun login_success_savesTokenPair() = runTest {
        val api = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore()
        val repository: AuthRepository = repository(api, tokenStore)

        api.loginHandler = { _, _, request ->
            assertThat(request).isEqualTo(
                LoginRequest(
                    email = "user@example.com",
                    password = "Password123!",
                ),
            )
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_LOGIN_SUCCESS",
                    message = "Login complete.",
                    data = authTokenDataResponse(
                        accessToken = "access-1",
                        refreshToken = "refresh-1",
                    ),
                ),
            )
        }

        val result = repository.login(
            email = "user@example.com",
            password = "Password123!",
        )

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        assertThat(tokenStore.getTokenPair()).isEqualTo(
            tokenPair(
                accessToken = "access-1",
                refreshToken = "refresh-1",
            ),
        )
    }

    @Test
    fun refreshToken_success_replacesTokenPair() = runTest {
        val api = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore()
        val repository: AuthRepository = repository(api, tokenStore)

        tokenStore.saveTokenPair(
            tokenPair(
                accessToken = "old-access",
                refreshToken = "old-refresh",
            ),
        )

        api.refreshTokenHandler = { _, _, request ->
            assertThat(request.refreshToken).isEqualTo("old-refresh")
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_TOKEN_REFRESH_SUCCESS",
                    message = "Token refreshed.",
                    data = authTokenDataResponse(
                        accessToken = "new-access",
                        refreshToken = "new-refresh",
                    ),
                ),
            )
        }

        val result = repository.refreshToken(refreshToken = "old-refresh")

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        assertThat(tokenStore.getTokenPair()).isEqualTo(
            tokenPair(
                accessToken = "new-access",
                refreshToken = "new-refresh",
            ),
        )
    }

    @Test
    fun confirmEmailVerification_success_savesTokenPair() = runTest {
        val api = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore()
        val repository: AuthRepository = repository(api, tokenStore)

        api.confirmEmailVerificationHandler = { deviceId, deviceName, request ->
            assertThat(deviceId).isEqualTo("device-id")
            assertThat(deviceName).isEqualTo("Pixel")
            assertThat(request).isEqualTo(
                EmailVerificationConfirmRequest(
                    email = "user@example.com",
                    verificationCode = "123456",
                ),
            )
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_EMAIL_VERIFICATION_CONFIRMED",
                    message = "Email verified.",
                    data = authTokenDataResponse(
                        accessToken = "verified-access",
                        refreshToken = "verified-refresh",
                    ),
                ),
            )
        }

        val result = repository.confirmEmailVerification(
            email = "user@example.com",
            verificationCode = "123456",
            deviceId = "device-id",
            deviceName = "Pixel",
        )

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        assertThat(tokenStore.getTokenPair()).isEqualTo(
            tokenPair(
                accessToken = "verified-access",
                refreshToken = "verified-refresh",
            ),
        )
    }

    @Test
    fun logout_success_clearsTokenPair() = runTest {
        val api = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore()
        val repository: AuthRepository = repository(api, tokenStore)

        tokenStore.saveTokenPair(tokenPair())

        api.logoutHandler = { _, _ ->
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_LOGOUT_SUCCESS",
                    message = "Logout complete.",
                    data = Unit,
                ),
            )
        }

        val result = repository.logout()

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        assertThat(tokenStore.getTokenPair()).isNull()
    }

    @Test
    fun badRequest_mapsFieldErrors() = runTest {
        val api = FakeAuthApi()
        val repository = repository(api)

        api.signupHandler = {
            errorResponse(
                code = 400,
                body = validationErrorJson,
            )
        }

        val result = repository.signup(
            email = "not-email",
            password = "Password123!",
            passwordConfirm = "Password123!",
            nickname = "tester",
        )

        val failure = result as AuthResult.Failure
        assertThat(failure.httpCode).isEqualTo(400)
        assertThat(failure.code).isEqualTo("COMMON_VALIDATION_ERROR")
        assertThat(failure.fieldErrors).hasSize(1)
        assertThat(failure.fieldErrors.first()).isEqualTo(
            FieldError(
                field = "email",
                reason = "Invalid email",
            ),
        )
    }

    @Test
    fun unauthorized_mapsFailure() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.loginHandler = { _, _, _ ->
            errorResponse(
                code = 401,
                body = unauthorizedErrorJson,
            )
        }

        val result = repository.login(
            email = "user@example.com",
            password = "wrong-password",
        )

        val failure = result as AuthResult.Failure
        assertThat(failure.httpCode).isEqualTo(401)
        assertThat(failure.code).isEqualTo("AUTH_LOGIN_FAILED")
        assertThat(failure.message).isEqualTo("Login failed.")
    }

    @Test
    fun loginFailure_preservesFailureMetadata() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.loginHandler = { _, _, _ ->
            errorResponse(
                code = 401,
                body = loginFailureMetadataJson,
            )
        }

        val result = repository.login(
            email = "user@example.com",
            password = "wrong-password",
        )

        val failure = result as AuthResult.Failure
        assertThat(failure.code).isEqualTo("AUTH_ACCOUNT_LOCKED")
        assertThat(failure.email).isEqualTo("user@example.com")
        assertThat(failure.failedLoginCount).isEqualTo(5)
        assertThat(failure.remainingAttempts).isEqualTo(0)
        assertThat(failure.passwordResetRequired).isTrue()
        assertThat(failure.nextAction).isEqualTo("password_reset")
        assertThat(failure.resetCodeSent).isTrue()
    }

    @Test
    fun passwordResetRequest_mapsRequestBody() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.passwordResetRequestHandler = { request ->
            assertThat(request).isEqualTo(PasswordResetRequest(email = "user@example.com"))
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_PASSWORD_RESET_REQUESTED",
                    message = "Password reset requested.",
                    data = PasswordResetRequestedDataResponse(
                        email = "user@example.com",
                        expiresInSeconds = 600,
                    ),
                ),
            )
        }

        val result = repository.requestPasswordReset(email = "user@example.com")

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
    }

    @Test
    fun passwordResetConfirm_mapsRequestBody() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.passwordResetConfirmHandler = { request ->
            assertThat(request).isEqualTo(
                PasswordResetConfirmRequest(
                    email = "user@example.com",
                    verificationCode = "123456",
                    newPassword = "Password123!",
                    newPasswordConfirm = "Password123!",
                ),
            )
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_PASSWORD_RESET_CONFIRMED",
                    message = "Password reset confirmed.",
                    data = Unit,
                ),
            )
        }

        val result = repository.confirmPasswordReset(
            email = "user@example.com",
            verificationCode = "123456",
            newPassword = "Password123!",
            newPasswordConfirm = "Password123!",
        )

        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
    }

    @Test
    fun kakaoLogin_accepted_mapsNicknameRequired() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.kakaoLoginHandler = { _, _, _ ->
            Response.success(
                202,
                ApiResponse(
                    success = true,
                    code = "AUTH_NICKNAME_REQUIRED",
                    message = "Nickname required.",
                    data = buildJsonObject {
                        put("signupToken", "signup-token")
                        put("provider", "kakao")
                        put("suggestedNickname", "kakao-user")
                    },
                ),
            )
        }

        val result = repository.kakaoLogin(
            kakaoAccessToken = "kakao-access-token",
            agreedToTerms = false,
        )

        val nicknameRequired = result as KakaoLoginResult.NicknameRequired
        assertThat(nicknameRequired.data).isEqualTo(
            KakaoNicknameRequired(
                signupToken = "signup-token",
                provider = nicknameRequired.data.provider,
                suggestedNickname = "kakao-user",
            ),
        )
    }

    @Test
    fun kakaoLogin_conflict_mapsAccountLinkRequired() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.kakaoLoginHandler = { _, _, _ ->
            errorResponse(
                code = 409,
                body = kakaoAccountLinkRequiredJson,
            )
        }

        val result = repository.kakaoLogin(
            kakaoAccessToken = "kakao-access-token",
            agreedToTerms = false,
        )

        val accountLinkRequired = result as KakaoLoginResult.AccountLinkRequired
        assertThat(accountLinkRequired.data).isEqualTo(
            KakaoAccountLinkRequired(
                email = "user@example.com",
                provider = accountLinkRequired.data.provider,
                linkToken = "link-token",
                expiresInSeconds = 600,
            ),
        )
    }

    @Test
    fun kakaoLogin_success_savesTokenPair() = runTest {
        val api = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore()
        val repository: AuthRepository = repository(api, tokenStore)

        api.kakaoLoginHandler = { _, _, _ ->
            Response.success(
                ApiResponse(
                    success = true,
                    code = "AUTH_LOGIN_SUCCESS",
                    message = "Kakao login complete.",
                    data = json.encodeToJsonElement(
                        AuthTokenDataResponse.serializer(),
                        authTokenDataResponse(
                            accessToken = "kakao-access",
                            refreshToken = "kakao-refresh",
                        ),
                    ),
                ),
            )
        }

        val result = repository.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        assertThat(result).isInstanceOf(KakaoLoginResult.LoggedIn::class.java)
        assertThat(tokenStore.getTokenPair()).isEqualTo(
            tokenPair(
                accessToken = "kakao-access",
                refreshToken = "kakao-refresh",
            ),
        )
    }

    @Test
    fun malformedErrorBody_returnsFallbackFailure() = runTest {
        val api = FakeAuthApi()
        val repository: AuthRepository = repository(api)

        api.loginHandler = { _, _, _ ->
            errorResponse(
                code = 500,
                body = "not-json",
            )
        }

        val result = repository.login(
            email = "user@example.com",
            password = "Password123!",
        )

        val failure = result as AuthResult.Failure
        assertThat(failure.httpCode).isEqualTo(500)
        assertThat(failure.code).isEqualTo("AUTH_HTTP_ERROR")
    }

    private fun repository(
        api: FakeAuthApi,
        tokenStore: FakeAuthTokenStore = FakeAuthTokenStore(),
    ): AuthRepositoryImpl = AuthRepositoryImpl(
        api = api,
        tokenStore = tokenStore,
        dispatchers = AppDispatchers(
            io = dispatcher,
            main = dispatcher,
            default = dispatcher,
        ),
        errorMapper = AuthErrorMapper(json),
        json = json,
    )

    private fun authTokenDataResponse(
        accessToken: String,
        refreshToken: String,
    ) = AuthTokenDataResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 1_209_600,
        user = AuthUserResponse(
            id = "018f8c2e-5f73-7b6a-b9f0-3f55e7f7c901",
            email = "user@example.com",
            nickname = "tester",
            dotoriBalance = 0,
            emailVerified = true,
            status = "active",
            providers = listOf("local", "kakao"),
        ),
    )

    private fun tokenPair(
        accessToken: String = "access-token",
        refreshToken: String = "refresh-token",
    ) = TokenPair(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 1_209_600,
    )

    private fun <T> errorResponse(
        code: Int,
        body: String,
    ): Response<T> = Response.error(
        code,
        body.toResponseBody(JSON_MEDIA_TYPE),
    )

    private class FakeAuthTokenStore : AuthTokenStore {
        private val tokenPair = MutableStateFlow<TokenPair?>(null)

        override fun observeTokenPair(): Flow<TokenPair?> = tokenPair

        override suspend fun getTokenPair(): TokenPair? = tokenPair.value

        override suspend fun saveTokenPair(tokenPair: TokenPair) {
            this.tokenPair.value = tokenPair
        }

        override suspend fun clear() {
            tokenPair.value = null
        }
    }

    @Suppress("TooManyFunctions")
    private class FakeAuthApi : AuthApi {
        var signupHandler: suspend (SignupRequest) -> Response<ApiResponse<SignupDataResponse>> = {
            unhandled("signup")
        }
        var loginHandler: suspend (String?, String?, LoginRequest) -> Response<ApiResponse<AuthTokenDataResponse>> =
            { _, _, _ -> unhandled("login") }
        var refreshTokenHandler: suspend (String?, String?, RefreshTokenRequest) -> Response<ApiResponse<AuthTokenDataResponse>> =
            { _, _, _ -> unhandled("refreshToken") }
        var confirmEmailVerificationHandler: suspend (
            String?,
            String?,
            EmailVerificationConfirmRequest,
        ) -> Response<ApiResponse<AuthTokenDataResponse>> = { _, _, _ ->
            unhandled("confirmEmailVerification")
        }
        var logoutHandler: suspend (String?, LogoutRequest) -> Response<ApiResponse<Unit>> = { _, _ ->
            unhandled("logout")
        }
        var passwordResetRequestHandler: suspend (PasswordResetRequest) -> Response<ApiResponse<PasswordResetRequestedDataResponse>> = {
            unhandled("requestPasswordReset")
        }
        var passwordResetConfirmHandler: suspend (PasswordResetConfirmRequest) -> Response<ApiResponse<Unit>> = {
            unhandled("confirmPasswordReset")
        }
        var kakaoLoginHandler: suspend (String?, String?, KakaoLoginRequest) -> Response<ApiResponse<JsonElement>> =
            { _, _, _ -> unhandled("kakaoLogin") }

        override suspend fun signup(request: SignupRequest): Response<ApiResponse<SignupDataResponse>> =
            signupHandler(request)

        override suspend fun checkEmailAvailability(email: String): Response<ApiResponse<EmailAvailabilityDataResponse>> =
            unhandled("checkEmailAvailability")

        override suspend fun resendEmailVerification(request: EmailVerificationRequest): Response<ApiResponse<EmailVerificationSentDataResponse>> =
            unhandled("resendEmailVerification")

        override suspend fun confirmEmailVerification(
            deviceId: String?,
            deviceName: String?,
            request: EmailVerificationConfirmRequest,
        ): Response<ApiResponse<AuthTokenDataResponse>> = confirmEmailVerificationHandler(
            deviceId,
            deviceName,
            request,
        )

        override suspend fun login(
            deviceId: String?,
            deviceName: String?,
            request: LoginRequest,
        ): Response<ApiResponse<AuthTokenDataResponse>> = loginHandler(
            deviceId,
            deviceName,
            request,
        )

        override suspend fun refreshToken(
            deviceId: String?,
            deviceName: String?,
            request: RefreshTokenRequest,
        ): Response<ApiResponse<AuthTokenDataResponse>> = refreshTokenHandler(
            deviceId,
            deviceName,
            request,
        )

        override suspend fun logout(
            authorization: String?,
            request: LogoutRequest,
        ): Response<ApiResponse<Unit>> = logoutHandler(
            authorization,
            request,
        )

        override suspend fun requestPasswordReset(request: PasswordResetRequest): Response<ApiResponse<PasswordResetRequestedDataResponse>> =
            passwordResetRequestHandler(request)

        override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Response<ApiResponse<Unit>> =
            passwordResetConfirmHandler(request)

        override suspend fun kakaoLogin(
            deviceId: String?,
            deviceName: String?,
            request: KakaoLoginRequest,
        ): Response<ApiResponse<JsonElement>> = kakaoLoginHandler(
            deviceId,
            deviceName,
            request,
        )

        override suspend fun linkKakaoAccount(
            deviceId: String?,
            deviceName: String?,
            request: KakaoAccountLinkRequest,
        ): Response<ApiResponse<AuthTokenDataResponse>> = unhandled("linkKakaoAccount")

        override suspend fun completeKakaoNickname(
            deviceId: String?,
            deviceName: String?,
            request: KakaoNicknameRequest,
        ): Response<ApiResponse<AuthTokenDataResponse>> = unhandled("completeKakaoNickname")

        override suspend fun getMe(authorization: String?): Response<ApiResponse<AuthUserResponse>> =
            unhandled("getMe")

        private fun <T> unhandled(method: String): T {
            error("Unhandled AuthApi call: $method")
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }

        val validationErrorJson = """
            {
              "success": false,
              "code": "COMMON_VALIDATION_ERROR",
              "message": "Invalid request.",
              "data": {
                "fieldErrors": [
                  {
                    "field": "email",
                    "reason": "Invalid email"
                  }
                ]
              }
            }
        """.trimIndent()

        val unauthorizedErrorJson = """
            {
              "success": false,
              "code": "AUTH_LOGIN_FAILED",
              "message": "Login failed.",
              "data": null
            }
        """.trimIndent()

        val loginFailureMetadataJson = """
            {
              "success": false,
              "code": "AUTH_ACCOUNT_LOCKED",
              "message": "Account locked.",
              "data": {
                "email": "user@example.com",
                "failedLoginCount": 5,
                "remainingAttempts": 0,
                "passwordResetRequired": true,
                "nextAction": "password_reset",
                "resetCodeSent": true
              }
            }
        """.trimIndent()

        val kakaoAccountLinkRequiredJson = """
            {
              "success": false,
              "code": "AUTH_OAUTH_ACCOUNT_LINK_REQUIRED",
              "message": "Link required.",
              "data": {
                "email": "user@example.com",
                "provider": "kakao",
                "linkToken": "link-token",
                "expiresInSeconds": 600
              }
            }
        """.trimIndent()
    }
}
