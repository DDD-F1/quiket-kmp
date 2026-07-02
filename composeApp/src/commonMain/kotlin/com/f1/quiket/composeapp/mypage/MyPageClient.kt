package com.f1.quiket.composeapp.mypage

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.network.ApiEnvelope
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

internal class MyPageClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
) {
    suspend fun getMyPage(session: SessionSnapshot): MyPageData {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)

        val profile = getMyProfile(authorization)
        val gamification = getMyGamification(authorization)
        return MyPageData(
            profile = profile,
            gamification = gamification,
        )
    }

    suspend fun getMyProfile(session: SessionSnapshot): MyProfile {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        return getMyProfile(authorization)
    }

    suspend fun updateMyNickname(session: SessionSnapshot, nickname: String): MyProfile {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.patch("${baseUrl.ensureTrailingSlash()}my/profile/nickname") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(NicknameUpdateRequest(nickname = nickname))
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "닉네임을 변경하지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<MyProfileDataResponse>(data).toDomain() }
    }

    suspend fun requestMyEmailChange(session: SessionSnapshot, newEmail: String): EmailVerificationSent {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}my/email/change-requests") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(MyEmailChangeRequest(newEmail = newEmail))
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "인증 메일을 보내지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<EmailVerificationSentDataResponse>(data).toDomain() }
    }

    suspend fun confirmMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
        verificationCode: String,
    ): MyProfile {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}my/email/change-confirm") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                MyEmailChangeConfirmRequest(
                    newEmail = newEmail,
                    verificationCode = verificationCode,
                ),
            )
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "이메일을 변경하지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<MyProfileDataResponse>(data).toDomain() }
    }

    suspend fun updateMyPassword(
        session: SessionSnapshot,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.patch("${baseUrl.ensureTrailingSlash()}my/password") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(
                PasswordChangeRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    newPasswordConfirm = newPasswordConfirm,
                ),
            )
        }
        parseEmptyResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "비밀번호를 변경하지 못했습니다.",
        )
    }

    suspend fun deleteMyAccount(session: SessionSnapshot, password: String?) {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}my/account/deletion") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(AccountDeleteRequest(password = password, agreedToDelete = true))
        }
        parseEmptyResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "회원 탈퇴를 처리하지 못했습니다.",
        )
    }

    suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}my/notifications") {
            header("Authorization", authorization)
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "알림 설정을 불러오지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<NotificationSettingsDataResponse>(data).toDomain() }
    }

    suspend fun updateNotificationSettings(
        session: SessionSnapshot,
        settings: NotificationSettings,
    ): NotificationSettings {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}my/notifications") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(settings.toRequest())
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "알림 설정을 저장하지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<NotificationSettingsDataResponse>(data).toDomain() }
    }

    suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String) {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}my/fcm-token") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(FcmTokenUpdateRequest(fcmToken = fcmToken))
        }
        parseEmptyResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "알림 토큰을 등록하지 못했습니다.",
        )
    }

    suspend fun createFeedback(session: SessionSnapshot, feedback: FeedbackCreate): Feedback {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}feedbacks") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(feedback.toRequest())
        }
        return parseDataResponse(
            body = response.bodyAsText(),
            statusCode = response.status.value,
            defaultErrorMessage = "문의를 제출하지 못했습니다.",
        ) { data -> json.decodeFromJsonElement<FeedbackDataResponse>(data).toDomain() }
    }

    private suspend fun getMyProfile(authorization: String): MyProfile {
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}my/profile") {
            header("Authorization", authorization)
        }
        val envelope = parseEnvelope(response.bodyAsText())
        if (response.status.value == 401) {
            throw MyPageException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw MyPageException(envelope.message.ifBlank { "프로필을 불러오지 못했습니다." })
        }
        val data = envelope.data ?: throw MyPageException("프로필 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<MyProfileDataResponse>(data).toDomain()
        }.getOrElse {
            throw MyPageException("프로필 응답을 해석하지 못했습니다.")
        }
    }

    private suspend fun getMyGamification(authorization: String): Gamification {
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}gamification/me") {
            header("Authorization", authorization)
        }
        val envelope = parseEnvelope(response.bodyAsText())
        if (response.status.value == 401) {
            throw MyPageException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (response.status.value !in 200..299 || !envelope.success) {
            throw MyPageException(envelope.message.ifBlank { "학습 정보를 불러오지 못했습니다." })
        }
        val data = envelope.data ?: throw MyPageException("학습 정보 응답에 데이터가 없습니다.")
        return runCatching {
            json.decodeFromJsonElement<GamificationDataResponse>(data).toDomain()
        }.getOrElse {
            throw MyPageException("학습 정보 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEnvelope(body: String): ApiEnvelope =
        runCatching {
            json.decodeFromString<ApiEnvelope>(body)
        }.getOrElse { error ->
            if (error is SerializationException) {
                throw MyPageException("서버 응답을 해석하지 못했습니다.")
            }
            throw error
        }

    private inline fun <T> parseDataResponse(
        body: String,
        statusCode: Int,
        defaultErrorMessage: String,
        mapper: (kotlinx.serialization.json.JsonElement) -> T,
    ): T {
        val envelope = parseEnvelope(body)
        if (statusCode == 401) {
            throw MyPageException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (statusCode !in 200..299 || !envelope.success) {
            throw MyPageException(envelope.message.ifBlank { defaultErrorMessage })
        }
        val data = envelope.data ?: throw MyPageException("서버 응답에 데이터가 없습니다.")
        return runCatching { mapper(data) }.getOrElse {
            throw MyPageException("서버 응답을 해석하지 못했습니다.")
        }
    }

    private fun parseEmptyResponse(
        body: String,
        statusCode: Int,
        defaultErrorMessage: String,
    ) {
        val envelope = parseEnvelope(body)
        if (statusCode == 401) {
            throw MyPageException(envelope.message.ifBlank { "로그인이 만료되었습니다." }, isUnauthorized = true)
        }
        if (statusCode !in 200..299 || !envelope.success) {
            throw MyPageException(envelope.message.ifBlank { defaultErrorMessage })
        }
    }
}

internal class MyPageException(
    message: String,
    val isUnauthorized: Boolean = false,
) : Exception(message)

internal data class MyPageData(
    val profile: MyProfile,
    val gamification: Gamification,
)

internal data class MyProfile(
    val id: String,
    val email: String?,
    val nickname: String,
    val dotoriBalance: Int,
    val emailVerified: Boolean,
    val status: String,
    val providers: List<String>,
    val xpTotal: Int?,
    val currentLevel: Int?,
    val createdAt: String?,
)

internal data class EmailVerificationSent(
    val email: String,
    val expiresInSeconds: Long,
)

internal data class Gamification(
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val currentLevelName: String?,
    val maxLevel: Int,
    val nextLevel: Int?,
    val nextLevelName: String?,
    val currentLevelMinXp: Int?,
    val nextLevelRequiredXp: Int?,
    val levelProgressPct: Int?,
)

internal data class NotificationSettings(
    val fcmTokenRegistered: Boolean?,
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
)

internal data class FeedbackCreate(
    val category: FeedbackCategory,
    val body: String,
    val replyEmail: String? = null,
)

internal data class Feedback(
    val id: String,
    val category: FeedbackCategory,
    val body: String,
    val replyEmail: String?,
    val createdAt: String,
)

internal enum class FeedbackCategory(
    val wireValue: String,
    val displayLabel: String,
) {
    Feature(wireValue = "feature", displayLabel = "기능 제안"),
    Bug(wireValue = "bug", displayLabel = "버그 신고"),
    Inquiry(wireValue = "inquiry", displayLabel = "문의"),
    Other(wireValue = "other", displayLabel = "기타"),
    Unknown(wireValue = "unknown", displayLabel = "기타"),
}

@Serializable
private data class NicknameUpdateRequest(
    val nickname: String,
)

@Serializable
private data class MyEmailChangeRequest(
    val newEmail: String,
)

@Serializable
private data class MyEmailChangeConfirmRequest(
    val newEmail: String,
    val verificationCode: String,
)

@Serializable
private data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String,
)

@Serializable
private data class AccountDeleteRequest(
    val password: String? = null,
    val agreedToDelete: Boolean,
)

@Serializable
private data class NotificationSettingsUpdateRequest(
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
)

@Serializable
private data class FcmTokenUpdateRequest(
    val fcmToken: String,
)

@Serializable
private data class FeedbackCreateRequest(
    val category: String,
    val body: String,
    val replyEmail: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
)

@Serializable
private data class MyProfileDataResponse(
    val id: String,
    val email: String? = null,
    val nickname: String,
    val dotoriBalance: Int,
    val emailVerified: Boolean = false,
    val status: String,
    val providers: List<String> = emptyList(),
    val xpTotal: Int? = null,
    val currentLevel: Int? = null,
    val createdAt: String? = null,
) {
    fun toDomain(): MyProfile = MyProfile(
        id = id,
        email = email,
        nickname = nickname,
        dotoriBalance = dotoriBalance,
        emailVerified = emailVerified,
        status = status,
        providers = providers,
        xpTotal = xpTotal,
        currentLevel = currentLevel,
        createdAt = createdAt,
    )
}

@Serializable
private data class EmailVerificationSentDataResponse(
    val email: String,
    val expiresInSeconds: Long,
) {
    fun toDomain(): EmailVerificationSent = EmailVerificationSent(
        email = email,
        expiresInSeconds = expiresInSeconds,
    )
}

@Serializable
private data class GamificationDataResponse(
    val dotoriBalance: Int,
    val xpTotal: Int,
    val currentLevel: Int,
    val currentLevelName: String? = null,
    val maxLevel: Int,
    val nextLevel: Int? = null,
    val nextLevelName: String? = null,
    val currentLevelMinXp: Int? = null,
    val nextLevelRequiredXp: Int? = null,
    val levelProgressPct: Int? = null,
) {
    fun toDomain(): Gamification = Gamification(
        dotoriBalance = dotoriBalance,
        xpTotal = xpTotal,
        currentLevel = currentLevel,
        currentLevelName = currentLevelName,
        maxLevel = maxLevel,
        nextLevel = nextLevel,
        nextLevelName = nextLevelName,
        currentLevelMinXp = currentLevelMinXp,
        nextLevelRequiredXp = nextLevelRequiredXp,
        levelProgressPct = levelProgressPct,
    )
}

@Serializable
private data class NotificationSettingsDataResponse(
    val fcmTokenRegistered: Boolean? = null,
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
) {
    fun toDomain(): NotificationSettings = NotificationSettings(
        fcmTokenRegistered = fcmTokenRegistered,
        activityEnabled = activityEnabled,
        updateEnabled = updateEnabled,
        reviewEnabled = reviewEnabled,
    )
}

@Serializable
private data class FeedbackDataResponse(
    val id: String,
    val category: String,
    val body: String,
    val replyEmail: String? = null,
    val createdAt: String,
) {
    fun toDomain(): Feedback = Feedback(
        id = id,
        category = category.toFeedbackCategory(),
        body = body,
        replyEmail = replyEmail,
        createdAt = createdAt,
    )
}

private fun FeedbackCreate.toRequest(): FeedbackCreateRequest = FeedbackCreateRequest(
    category = category.wireValue,
    body = body,
    replyEmail = replyEmail,
)

private fun NotificationSettings.toRequest(): NotificationSettingsUpdateRequest = NotificationSettingsUpdateRequest(
    activityEnabled = activityEnabled,
    updateEnabled = updateEnabled,
    reviewEnabled = reviewEnabled,
)

private fun String.toFeedbackCategory(): FeedbackCategory =
    FeedbackCategory.entries.firstOrNull { it.wireValue == this } ?: FeedbackCategory.Unknown

private fun SessionSnapshot.authorizationHeader(): String? {
    val token = accessToken?.takeIf { it.isNotBlank() } ?: return null
    val type = tokenType?.takeIf { it.isNotBlank() } ?: "Bearer"
    return "$type $token"
}
