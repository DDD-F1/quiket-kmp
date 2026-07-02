package com.f1.quiket.feature.login.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.auth.AuthTokenStore
import com.f1.quiket.core.network.auth.TokenPair
import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.feature.login.data.mapper.toDomain
import com.f1.quiket.feature.login.data.mapper.toTokenPair
import com.f1.quiket.feature.login.data.remote.AuthApi
import com.f1.quiket.feature.login.data.remote.AuthErrorMapper
import com.f1.quiket.feature.login.data.remote.AuthTokenDataResponse
import com.f1.quiket.feature.login.data.remote.AuthUserResponse
import com.f1.quiket.feature.login.data.remote.EmailAvailabilityDataResponse
import com.f1.quiket.feature.login.data.remote.EmailVerificationConfirmRequest
import com.f1.quiket.feature.login.data.remote.EmailVerificationRequest
import com.f1.quiket.feature.login.data.remote.EmailVerificationSentDataResponse
import com.f1.quiket.feature.login.data.remote.KakaoAccountLinkRequest
import com.f1.quiket.feature.login.data.remote.KakaoAccountLinkRequiredDataResponse
import com.f1.quiket.feature.login.data.remote.KakaoLoginRequest
import com.f1.quiket.feature.login.data.remote.KakaoNicknameRequest
import com.f1.quiket.feature.login.data.remote.KakaoNicknameRequiredDataResponse
import com.f1.quiket.feature.login.data.remote.LoginRequest
import com.f1.quiket.feature.login.data.remote.LogoutRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetConfirmRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetRequest
import com.f1.quiket.feature.login.data.remote.PasswordResetRequestedDataResponse
import com.f1.quiket.feature.login.data.remote.RefreshTokenRequest
import com.f1.quiket.feature.login.data.remote.SignupDataResponse
import com.f1.quiket.feature.login.data.remote.SignupRequest
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.AuthTokenData
import com.f1.quiket.feature.login.domain.model.AuthUser
import com.f1.quiket.feature.login.domain.model.EmailAvailability
import com.f1.quiket.feature.login.domain.model.EmailVerificationSent
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.model.PasswordResetRequested
import com.f1.quiket.feature.login.domain.model.SignupData
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.Response

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: AuthTokenStore,
    private val dispatchers: AppDispatchers,
    private val errorMapper: AuthErrorMapper,
    private val json: Json,
) : AuthRepository {
    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): AuthResult<SignupData> = execute(
        call = {
            api.signup(
                SignupRequest(
                    email = email,
                    password = password,
                    passwordConfirm = passwordConfirm,
                    nickname = nickname,
                ),
            )
        },
        mapper = SignupDataResponse::toDomain,
    )

    override suspend fun checkEmailAvailability(email: String): AuthResult<EmailAvailability> =
        execute(
            call = { api.checkEmailAvailability(email = email) },
            mapper = EmailAvailabilityDataResponse::toDomain,
        )

    override suspend fun resendEmailVerification(email: String): AuthResult<EmailVerificationSent> =
        execute(
            call = { api.resendEmailVerification(EmailVerificationRequest(email = email)) },
            mapper = EmailVerificationSentDataResponse::toDomain,
        )

    override suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String?,
        verificationToken: String?,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = executeTokenRequest(
        call = {
            api.confirmEmailVerification(
                deviceId = deviceId,
                deviceName = deviceName,
                EmailVerificationConfirmRequest(
                    email = email,
                    verificationCode = verificationCode,
                    verificationToken = verificationToken,
                ),
            )
        },
    )

    override suspend fun login(
        email: String,
        password: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = executeTokenRequest(
        call = {
            api.login(
                deviceId = deviceId,
                deviceName = deviceName,
                request = LoginRequest(
                    email = email,
                    password = password,
                ),
            )
        },
    )

    override suspend fun refreshToken(
        refreshToken: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = executeTokenRequest(
        call = {
            api.refreshToken(
                deviceId = deviceId,
                deviceName = deviceName,
                request = RefreshTokenRequest(refreshToken = refreshToken),
            )
        },
    )

    override suspend fun logout(refreshToken: String?): AuthResult<Unit> = executeEmpty(
        call = {
            val storedTokenPair = tokenStore.getTokenPair()
            api.logout(
                authorization = storedTokenPair?.toAuthorizationHeader(),
                request = LogoutRequest(refreshToken = refreshToken ?: storedTokenPair?.refreshToken),
            )
        },
        afterSuccess = { tokenStore.clear() },
    )

    override suspend fun requestPasswordReset(email: String): AuthResult<PasswordResetRequested> =
        execute(
            call = { api.requestPasswordReset(PasswordResetRequest(email = email)) },
            mapper = PasswordResetRequestedDataResponse::toDomain,
        )

    override suspend fun confirmPasswordReset(
        email: String,
        newPassword: String,
        newPasswordConfirm: String,
        resetToken: String?,
        verificationCode: String?,
    ): AuthResult<Unit> = executeEmpty(
        call = {
            api.confirmPasswordReset(
                PasswordResetConfirmRequest(
                    email = email,
                    resetToken = resetToken,
                    verificationCode = verificationCode,
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm,
                ),
            )
        },
    )

    override suspend fun kakaoLogin(
        kakaoAccessToken: String,
        agreedToTerms: Boolean,
        deviceId: String?,
        deviceName: String?,
    ): KakaoLoginResult = withContext(dispatchers.io) {
        try {
            val response = api.kakaoLogin(
                deviceId = deviceId,
                deviceName = deviceName,
                request = KakaoLoginRequest(
                    kakaoAccessToken = kakaoAccessToken,
                    agreedToTerms = agreedToTerms,
                ),
            )

            when {
                response.isSuccessful -> handleSuccessfulKakaoLogin(response)
                response.code() == HTTP_CONFLICT -> handleKakaoAccountLinkRequired(response)
                else -> KakaoLoginResult.Failure(errorMapper.map(response))
            }
        } catch (throwable: Throwable) {
            throwable.throwIfCancellation()
            KakaoLoginResult.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
        agreedToLink: Boolean,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = executeTokenRequest(
        call = {
            api.linkKakaoAccount(
                deviceId = deviceId,
                deviceName = deviceName,
                request = KakaoAccountLinkRequest(
                    linkToken = linkToken,
                    email = email,
                    password = password,
                    agreedToLink = agreedToLink,
                ),
            )
        },
    )

    override suspend fun completeKakaoNickname(
        signupToken: String,
        nickname: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = executeTokenRequest(
        call = {
            api.completeKakaoNickname(
                deviceId = deviceId,
                deviceName = deviceName,
                request = KakaoNicknameRequest(
                    signupToken = signupToken,
                    nickname = nickname,
                ),
            )
        },
    )

    override suspend fun getMe(): AuthResult<AuthUser> = execute(
        call = {
            api.getMe(
                authorization = tokenStore.getTokenPair()?.toAuthorizationHeader(),
            )
        },
        mapper = AuthUserResponse::toDomain,
    )

    private suspend fun executeTokenRequest(
        call: suspend () -> Response<ApiResponse<AuthTokenDataResponse>>,
    ): AuthResult<AuthTokenData> = execute(
        call = call,
        mapper = AuthTokenDataResponse::toDomain,
        afterSuccess = { response -> tokenStore.saveTokenPair(response.toTokenPair()) },
    )

    private suspend fun <Dto : Any, Domain> execute(
        call: suspend () -> Response<ApiResponse<Dto>>,
        mapper: (Dto) -> Domain,
        afterSuccess: suspend (Dto) -> Unit = {},
    ): AuthResult<Domain> = withContext(dispatchers.io) {
        try {
            val response = call()
            if (!response.isSuccessful) {
                return@withContext errorMapper.map(response)
            }

            val body = response.body()
                ?: return@withContext emptyBodyFailure(response.code())

            if (!body.success) {
                return@withContext errorMapper.mapEnvelopeFailure(
                    httpCode = response.code(),
                    code = body.code,
                    message = body.message,
                )
            }

            val data = body.data
                ?: return@withContext emptyDataFailure(response.code(), body.code, body.message)
            val mappedData = mapper(data)
            afterSuccess(data)
            AuthResult.Success(mappedData)
        } catch (throwable: Throwable) {
            throwable.throwIfCancellation()
            errorMapper.map(throwable)
        }
    }

    private suspend fun executeEmpty(
        call: suspend () -> Response<ApiResponse<Unit>>,
        afterSuccess: suspend () -> Unit = {},
    ): AuthResult<Unit> = withContext(dispatchers.io) {
        try {
            val response = call()
            if (!response.isSuccessful) {
                return@withContext errorMapper.map(response)
            }

            val body = response.body()
                ?: return@withContext emptyBodyFailure(response.code())

            if (!body.success) {
                return@withContext errorMapper.mapEnvelopeFailure(
                    httpCode = response.code(),
                    code = body.code,
                    message = body.message,
                )
            }

            afterSuccess()
            AuthResult.Success(Unit)
        } catch (throwable: Throwable) {
            throwable.throwIfCancellation()
            errorMapper.map(throwable)
        }
    }

    private suspend fun handleSuccessfulKakaoLogin(
        response: Response<ApiResponse<JsonElement>>,
    ): KakaoLoginResult {
        val body = response.body()
            ?: return KakaoLoginResult.Failure(emptyBodyFailure(response.code()))

        if (!body.success) {
            return KakaoLoginResult.Failure(
                errorMapper.mapEnvelopeFailure(
                    httpCode = response.code(),
                    code = body.code,
                    message = body.message,
                ),
            )
        }

        val data = body.data
            ?: return KakaoLoginResult.Failure(emptyDataFailure(response.code(), body.code, body.message))

        return when (response.code()) {
            HTTP_OK, HTTP_CREATED -> {
                val tokenResponse = decodeKakaoData<AuthTokenDataResponse>(data)
                    ?: return KakaoLoginResult.Failure(parseFailure(response.code()))
                val tokenData = tokenResponse.toDomain()
                tokenStore.saveTokenPair(tokenResponse.toTokenPair())
                KakaoLoginResult.LoggedIn(tokenData)
            }

            HTTP_ACCEPTED -> {
                val nicknameRequired = decodeKakaoData<KakaoNicknameRequiredDataResponse>(data)
                    ?: return KakaoLoginResult.Failure(parseFailure(response.code()))
                KakaoLoginResult.NicknameRequired(nicknameRequired.toDomain())
            }

            else -> KakaoLoginResult.Failure(
                AuthResult.Failure(
                    code = "AUTH_UNEXPECTED_KAKAO_STATUS",
                    message = "예상하지 못한 Kakao 로그인 응답입니다.",
                    httpCode = response.code(),
                ),
            )
        }
    }

    private fun handleKakaoAccountLinkRequired(
        response: Response<ApiResponse<JsonElement>>,
    ): KakaoLoginResult {
        val errorBody = response.errorBody()?.string()
        val linkRequiredResponse = errorBody
            ?.takeIf(String::isNotBlank)
            ?.let { body ->
                runCatching {
                    json.decodeFromString<ApiResponse<KakaoAccountLinkRequiredDataResponse>>(body)
                }.getOrNull()
            }

        val data = linkRequiredResponse?.data
        return if (data != null) {
            KakaoLoginResult.AccountLinkRequired(data.toDomain())
        } else {
            KakaoLoginResult.Failure(
                errorMapper.map(
                    httpCode = response.code(),
                    errorBody = errorBody,
                ),
            )
        }
    }

    private inline fun <reified T> decodeKakaoData(data: JsonElement): T? =
        runCatching { json.decodeFromJsonElement<T>(data) }.getOrNull()

    private fun TokenPair.toAuthorizationHeader(): String = "$tokenType $accessToken"

    private fun Throwable.throwIfCancellation() {
        if (this is CancellationException) {
            throw this
        }
    }

    private fun emptyBodyFailure(httpCode: Int): AuthResult.Failure = AuthResult.Failure(
        code = "AUTH_EMPTY_RESPONSE",
        message = "서버 응답 본문이 비어 있습니다.",
        httpCode = httpCode,
    )

    private fun emptyDataFailure(
        httpCode: Int,
        code: String,
        message: String,
    ): AuthResult.Failure = AuthResult.Failure(
        code = code.ifBlank { "AUTH_EMPTY_DATA" },
        message = message.ifBlank { "서버 응답 데이터가 비어 있습니다." },
        httpCode = httpCode,
    )

    private fun parseFailure(httpCode: Int): AuthResult.Failure = AuthResult.Failure(
        code = "AUTH_RESPONSE_PARSE_ERROR",
        message = "서버 응답을 해석할 수 없습니다.",
        httpCode = httpCode,
    )

    private companion object {
        const val HTTP_OK = 200
        const val HTTP_CREATED = 201
        const val HTTP_ACCEPTED = 202
        const val HTTP_CONFLICT = 409
    }
}
