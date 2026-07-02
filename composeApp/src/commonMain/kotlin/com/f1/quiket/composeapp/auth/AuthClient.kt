package com.f1.quiket.composeapp.auth

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class AuthClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun checkEmailAvailability(email: String): EmailAvailability {
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}auth/emails/availability") {
            parameter("email", email)
        }

        return decodeResponse(
            response = response,
            failureMessage = "이메일 확인에 실패했습니다.",
            missingMessage = "이메일 확인 응답이 비어 있습니다.",
        )
    }

    suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                SignupRequest(
                    email = email,
                    password = password,
                    passwordConfirm = passwordConfirm,
                    nickname = nickname,
                ),
            )
        }

        return decodeResponse(
            response = response,
            failureMessage = "회원가입에 실패했습니다.",
            missingMessage = "회원가입 응답이 비어 있습니다.",
        )
    }

    suspend fun resendEmailVerification(email: String): EmailVerificationSent {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/email-verifications") {
            contentType(ContentType.Application.Json)
            setBody(EmailVerificationRequest(email = email))
        }

        return decodeResponse(
            response = response,
            failureMessage = "인증번호 재요청에 실패했습니다.",
            missingMessage = "인증번호 응답이 비어 있습니다.",
        )
    }

    suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String,
    ): AuthTokenData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/email-verifications/confirm") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(
                EmailVerificationConfirmRequest(
                    email = email,
                    verificationCode = verificationCode,
                ),
            )
        }

        return decodeResponse<AuthTokenDataResponse>(
            response = response,
            failureMessage = "이메일 인증에 실패했습니다.",
            missingMessage = "이메일 인증 응답에 토큰 정보가 없습니다.",
        ).toDomain()
    }

    suspend fun requestPasswordReset(email: String): PasswordResetRequested {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/password-reset/requests") {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetRequest(email = email))
        }

        return decodeResponse(
            response = response,
            failureMessage = "비밀번호 재설정 요청에 실패했습니다.",
            missingMessage = "비밀번호 재설정 응답이 비어 있습니다.",
        )
    }

    suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/password-reset/confirm") {
            contentType(ContentType.Application.Json)
            setBody(
                PasswordResetConfirmRequest(
                    email = email,
                    verificationCode = verificationCode,
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm,
                ),
            )
        }

        decodeEmptyResponse(
            response = response,
            failureMessage = "비밀번호 재설정에 실패했습니다.",
        )
    }

    suspend fun login(email: String, password: String): AuthTokenData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/login") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(LoginRequest(email = email, password = password))
        }

        return decodeResponse<AuthTokenDataResponse>(
            response = response,
            failureMessage = "로그인에 실패했습니다.",
            missingMessage = "로그인 응답에 토큰 정보가 없습니다.",
        ).toDomain()
    }

    suspend fun kakaoLogin(
        kakaoAccessToken: String,
        agreedToTerms: Boolean = true,
    ): KakaoLoginResult {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/oauth/kakao/login") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(
                KakaoLoginRequest(
                    kakaoAccessToken = kakaoAccessToken,
                    agreedToTerms = agreedToTerms,
                ),
            )
        }

        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        val data = envelope.data

        if (response.status.value == HttpConflict) {
            val linkRequired = data?.let { element ->
                runCatching {
                    json.decodeFromJsonElement<KakaoAccountLinkRequired>(element)
                }.getOrNull()
            }
            return if (linkRequired != null) {
                KakaoLoginResult.AccountLinkRequired(linkRequired)
            } else {
                KakaoLoginResult.Failure(
                    envelope.message.ifBlank { "카카오 계정 연결 정보를 해석하지 못했습니다." },
                )
            }
        }

        if (response.status.value !in 200..299 || !envelope.success) {
            return KakaoLoginResult.Failure(envelope.message.ifBlank { "카카오 로그인에 실패했습니다." })
        }

        if (data == null) {
            return KakaoLoginResult.Failure("카카오 로그인 응답 데이터가 비어 있습니다.")
        }

        return when (response.status.value) {
            HttpOk, HttpCreated -> {
                val tokenResponse = runCatching {
                    json.decodeFromJsonElement<AuthTokenDataResponse>(data)
                }.getOrNull()
                if (tokenResponse == null) {
                    KakaoLoginResult.Failure("카카오 로그인 응답을 해석하지 못했습니다.")
                } else {
                    KakaoLoginResult.LoggedIn(tokenResponse.toDomain())
                }
            }

            HttpAccepted -> {
                val nicknameRequired = runCatching {
                    json.decodeFromJsonElement<KakaoNicknameRequired>(data)
                }.getOrNull()
                if (nicknameRequired == null) {
                    KakaoLoginResult.Failure("카카오 닉네임 설정 정보를 해석하지 못했습니다.")
                } else {
                    KakaoLoginResult.NicknameRequired(nicknameRequired)
                }
            }

            else -> KakaoLoginResult.Failure("예상하지 못한 카카오 로그인 응답입니다.")
        }
    }

    suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
        agreedToLink: Boolean = true,
    ): AuthTokenData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/oauth/kakao/link") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(
                KakaoAccountLinkRequest(
                    linkToken = linkToken,
                    email = email,
                    password = password,
                    agreedToLink = agreedToLink,
                ),
            )
        }

        return decodeResponse<AuthTokenDataResponse>(
            response = response,
            failureMessage = "카카오 계정 연결에 실패했습니다.",
            missingMessage = "카카오 계정 연결 응답에 토큰 정보가 없습니다.",
        ).toDomain()
    }

    suspend fun completeKakaoNickname(
        signupToken: String,
        nickname: String,
    ): AuthTokenData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/oauth/kakao/nickname") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(
                KakaoNicknameRequest(
                    signupToken = signupToken,
                    nickname = nickname,
                ),
            )
        }

        return decodeResponse<AuthTokenDataResponse>(
            response = response,
            failureMessage = "카카오 닉네임 설정에 실패했습니다.",
            missingMessage = "카카오 닉네임 설정 응답에 토큰 정보가 없습니다.",
        ).toDomain()
    }

    suspend fun refreshToken(refreshToken: String): AuthTokenData {
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/token/refresh") {
            contentType(ContentType.Application.Json)
            header("X-Device-Id", DeviceInfo.deviceId)
            header("X-Device-Name", DeviceInfo.deviceName)
            setBody(RefreshTokenRequest(refreshToken = refreshToken))
        }

        return decodeResponse<AuthTokenDataResponse>(
            response = response,
            failureMessage = "로그인 갱신에 실패했습니다.",
            missingMessage = "로그인 갱신 응답에 토큰 정보가 없습니다.",
        ).toDomain()
    }

    suspend fun logout(session: SessionSnapshot) {
        val authorization = session.authorizationHeader()
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}auth/logout") {
            contentType(ContentType.Application.Json)
            if (authorization != null) {
                header("Authorization", authorization)
            }
            setBody(LogoutRequest(refreshToken = session.refreshToken))
        }

        decodeEmptyResponse(
            response = response,
            failureMessage = "로그아웃에 실패했습니다.",
        )
    }

    suspend fun getMe(session: SessionSnapshot): AuthUser {
        val authorization = session.authorizationHeader()
            ?: throw AuthException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}auth/me") {
            header("Authorization", authorization)
        }

        return decodeResponse<AuthUserResponse>(
            response = response,
            failureMessage = "사용자 정보를 불러오지 못했습니다.",
            missingMessage = "사용자 정보 응답이 비어 있습니다.",
        ).toDomain()
    }

    private suspend inline fun <reified T> decodeResponse(
        response: HttpResponse,
        failureMessage: String,
        missingMessage: String,
    ): T {
        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == HttpUnauthorized) {
            val errorData = envelope.toAuthErrorData(json)
            throw AuthException(
                message = envelope.message.ifBlank { "로그인이 만료되었습니다." },
                isUnauthorized = true,
                code = envelope.code,
                email = errorData?.email,
                failedLoginCount = errorData?.failedLoginCount,
                resetCodeSent = errorData?.resetCodeSent,
            )
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            val errorData = envelope.toAuthErrorData(json)
            throw AuthException(
                message = envelope.message.ifBlank { failureMessage },
                code = envelope.code,
                email = errorData?.email,
                failedLoginCount = errorData?.failedLoginCount,
                resetCodeSent = errorData?.resetCodeSent,
            )
        }

        val data = envelope.data ?: throw AuthException(missingMessage)
        return runCatching {
            json.decodeFromJsonElement<T>(data)
        }.getOrElse {
            throw AuthException("서버 응답을 해석하지 못했습니다.")
        }
    }

    private suspend fun decodeEmptyResponse(
        response: HttpResponse,
        failureMessage: String,
    ) {
        val body = response.bodyAsText()
        val envelope = parseEnvelope(body)
        if (response.status.value == HttpUnauthorized) {
            val errorData = envelope.toAuthErrorData(json)
            throw AuthException(
                message = envelope.message.ifBlank { "로그인이 만료되었습니다." },
                isUnauthorized = true,
                code = envelope.code,
                email = errorData?.email,
                failedLoginCount = errorData?.failedLoginCount,
                resetCodeSent = errorData?.resetCodeSent,
            )
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            val errorData = envelope.toAuthErrorData(json)
            throw AuthException(
                message = envelope.message.ifBlank { failureMessage },
                code = envelope.code,
                email = errorData?.email,
                failedLoginCount = errorData?.failedLoginCount,
                resetCodeSent = errorData?.resetCodeSent,
            )
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw AuthException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }
}

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}

internal class AuthException(
    message: String,
    val isUnauthorized: Boolean = false,
    val code: String? = null,
    val email: String? = null,
    val failedLoginCount: Int? = null,
    val resetCodeSent: Boolean? = null,
) : Exception(message)

private fun ApiEnvelope.toAuthErrorData(json: Json): AuthErrorDataResponse? {
    val element = data ?: return null
    return runCatching {
        json.decodeFromJsonElement<AuthErrorDataResponse>(element)
    }.getOrNull()
}

internal data class AuthTokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val user: AuthUser? = null,
)

internal data class AuthUser(
    val id: String,
    val email: String?,
    val nickname: String,
)

internal sealed interface KakaoLoginResult {
    data class LoggedIn(
        val tokenData: AuthTokenData,
    ) : KakaoLoginResult

    data class NicknameRequired(
        val data: KakaoNicknameRequired,
    ) : KakaoLoginResult

    data class AccountLinkRequired(
        val data: KakaoAccountLinkRequired,
    ) : KakaoLoginResult

    data class Failure(
        val message: String,
    ) : KakaoLoginResult
}

@Serializable
internal data class KakaoAccountLinkRequired(
    val email: String,
    val provider: String,
    val linkToken: String,
    val expiresInSeconds: Long,
)

@Serializable
internal data class KakaoNicknameRequired(
    val signupToken: String,
    val provider: String,
    val suggestedNickname: String? = null,
)

@Serializable
private data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
private data class LogoutRequest(
    val refreshToken: String? = null,
)

@Serializable
private data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String,
)

@Serializable
private data class EmailVerificationRequest(
    val email: String,
)

@Serializable
private data class EmailVerificationConfirmRequest(
    val email: String,
    val verificationCode: String? = null,
    val verificationToken: String? = null,
)

@Serializable
private data class PasswordResetRequest(
    val email: String,
)

@Serializable
private data class PasswordResetConfirmRequest(
    val email: String,
    val resetToken: String? = null,
    val verificationCode: String? = null,
    val newPassword: String,
    val newPasswordConfirm: String,
)

@Serializable
private data class KakaoLoginRequest(
    val kakaoAccessToken: String,
    val agreedToTerms: Boolean = true,
)

@Serializable
private data class KakaoAccountLinkRequest(
    val linkToken: String,
    val email: String,
    val password: String,
    val agreedToLink: Boolean,
)

@Serializable
private data class KakaoNicknameRequest(
    val signupToken: String,
    val nickname: String,
)

private const val HttpOk = 200
private const val HttpCreated = 201
private const val HttpAccepted = 202
private const val HttpUnauthorized = 401
private const val HttpConflict = 409

@Serializable
internal data class EmailAvailability(
    val email: String,
    val available: Boolean,
)

@Serializable
internal data class SignupData(
    val userId: String,
    val email: String,
    val nickname: String,
    val emailVerificationRequired: Boolean,
    val emailVerificationSent: Boolean,
)

@Serializable
internal data class EmailVerificationSent(
    val email: String,
    val expiresInSeconds: Long,
)

@Serializable
internal data class PasswordResetRequested(
    val email: String,
    val expiresInSeconds: Long,
)

@Serializable
private data class AuthTokenDataResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val user: AuthUserResponse? = null,
) {
    fun toDomain(): AuthTokenData = AuthTokenData(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        accessTokenExpiresIn = accessTokenExpiresIn,
        refreshTokenExpiresIn = refreshTokenExpiresIn,
        user = user?.toDomain(),
    )
}

@Serializable
private data class AuthUserResponse(
    val id: String,
    val email: String? = null,
    val nickname: String,
    val dotoriBalance: Int = 0,
    val emailVerified: Boolean = false,
    val status: String = "",
    val providers: List<String> = emptyList(),
) {
    fun toDomain(): AuthUser = AuthUser(
        id = id,
        email = email,
        nickname = nickname,
    )
}

@Serializable
private data class AuthErrorDataResponse(
    val email: String? = null,
    val failedLoginCount: Int? = null,
    val resetCodeSent: Boolean? = null,
)
