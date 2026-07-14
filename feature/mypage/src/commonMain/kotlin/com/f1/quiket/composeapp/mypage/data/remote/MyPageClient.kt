package com.f1.quiket.composeapp.mypage.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.domain.model.Feedback
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCategory
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCreate
import com.f1.quiket.composeapp.mypage.domain.model.Gamification
import com.f1.quiket.composeapp.mypage.domain.model.MyPageData
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings
import com.f1.quiket.composeapp.network.ApiException
import com.f1.quiket.composeapp.network.decodeApiData
import com.f1.quiket.composeapp.network.ensureTrailingSlash
import com.f1.quiket.composeapp.network.requireApiSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
        return response.decodeMyPageData<MyProfileDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "닉네임을 변경하지 못했습니다.",
        ).toDomain()
    }

    suspend fun requestMyEmailChange(session: SessionSnapshot, newEmail: String): EmailVerificationSent {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}my/email/change-requests") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(MyEmailChangeRequest(newEmail = newEmail))
        }
        return response.decodeMyPageData<EmailVerificationSentDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "인증 메일을 보내지 못했습니다.",
        ).toDomain()
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
        return response.decodeMyPageData<MyProfileDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "이메일을 변경하지 못했습니다.",
        ).toDomain()
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
        response.requireMyPageSuccess(failureMessage = "비밀번호를 변경하지 못했습니다.")
    }

    suspend fun deleteMyAccount(session: SessionSnapshot, password: String?) {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}my/account/deletion") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(AccountDeleteRequest(password = password, agreedToDelete = true))
        }
        response.requireMyPageSuccess(failureMessage = "회원 탈퇴를 처리하지 못했습니다.")
    }

    suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}my/notifications") {
            header("Authorization", authorization)
        }
        return response.decodeMyPageData<NotificationSettingsDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "알림 설정을 불러오지 못했습니다.",
        ).toDomain()
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
        return response.decodeMyPageData<NotificationSettingsDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "알림 설정을 저장하지 못했습니다.",
        ).toDomain()
    }

    suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String) {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.put("${baseUrl.ensureTrailingSlash()}my/fcm-token") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(FcmTokenUpdateRequest(fcmToken = fcmToken))
        }
        response.requireMyPageSuccess(failureMessage = "알림 토큰을 등록하지 못했습니다.")
    }

    suspend fun createFeedback(session: SessionSnapshot, feedback: FeedbackCreate): Feedback {
        val authorization = session.authorizationHeader()
            ?: throw MyPageException("로그인이 필요합니다.", isUnauthorized = true)
        val response = httpClient.post("${baseUrl.ensureTrailingSlash()}feedbacks") {
            header("Authorization", authorization)
            contentType(ContentType.Application.Json)
            setBody(feedback.toRequest())
        }
        return response.decodeMyPageData<FeedbackDataResponse>(
            missingMessage = "서버 응답에 데이터가 없습니다.",
            failureMessage = "문의를 제출하지 못했습니다.",
        ).toDomain()
    }

    private suspend fun getMyProfile(authorization: String): MyProfile {
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}my/profile") {
            header("Authorization", authorization)
        }
        return response.decodeMyPageData<MyProfileDataResponse>(
            missingMessage = "프로필 응답에 데이터가 없습니다.",
            failureMessage = "프로필을 불러오지 못했습니다.",
            dataFailureMessage = "프로필 응답을 해석하지 못했습니다.",
        ).toDomain()
    }

    private suspend fun getMyGamification(authorization: String): Gamification {
        val response = httpClient.get("${baseUrl.ensureTrailingSlash()}gamification/me") {
            header("Authorization", authorization)
        }
        return response.decodeMyPageData<GamificationDataResponse>(
            missingMessage = "학습 정보 응답에 데이터가 없습니다.",
            failureMessage = "학습 정보를 불러오지 못했습니다.",
            dataFailureMessage = "학습 정보 응답을 해석하지 못했습니다.",
        ).toDomain()
    }

    private suspend inline fun <reified T> HttpResponse.decodeMyPageData(
        missingMessage: String,
        failureMessage: String,
        dataFailureMessage: String = "서버 응답을 해석하지 못했습니다.",
    ): T = try {
        decodeApiData(
            json = json,
            missingMessage = missingMessage,
            failureMessage = failureMessage,
            dataFailureMessage = dataFailureMessage,
        )
    } catch (error: ApiException) {
        throw error.toMyPageException()
    }

    private suspend fun HttpResponse.requireMyPageSuccess(failureMessage: String) {
        try {
            requireApiSuccess(json = json, failureMessage = failureMessage)
        } catch (error: ApiException) {
            throw error.toMyPageException()
        }
    }

    private fun ApiException.toMyPageException(): MyPageException = MyPageException(
        message = message.orEmpty(),
        isUnauthorized = isUnauthorized,
    )
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
