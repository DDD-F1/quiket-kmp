package com.f1.quiket.feature.mypage.data.mapper

import com.f1.quiket.feature.mypage.data.remote.AccountDeleteRequest
import com.f1.quiket.feature.mypage.data.remote.EmailVerificationSentDataResponse
import com.f1.quiket.feature.mypage.data.remote.FcmTokenUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.FeedbackCreateRequest
import com.f1.quiket.feature.mypage.data.remote.FeedbackDataResponse
import com.f1.quiket.feature.mypage.data.remote.GamificationDataResponse
import com.f1.quiket.feature.mypage.data.remote.MyEmailChangeConfirmRequest
import com.f1.quiket.feature.mypage.data.remote.MyEmailChangeRequest
import com.f1.quiket.feature.mypage.data.remote.MyProfileDataResponse
import com.f1.quiket.feature.mypage.data.remote.NicknameUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.NotificationSettingsDataResponse
import com.f1.quiket.feature.mypage.data.remote.NotificationSettingsUpdateRequest
import com.f1.quiket.feature.mypage.data.remote.PasswordChangeRequest
import com.f1.quiket.feature.mypage.domain.model.AccountDelete
import com.f1.quiket.feature.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.feature.mypage.domain.model.Feedback
import com.f1.quiket.feature.mypage.domain.model.FeedbackCategory
import com.f1.quiket.feature.mypage.domain.model.FeedbackCreate
import com.f1.quiket.feature.mypage.domain.model.Gamification
import com.f1.quiket.feature.mypage.domain.model.MyProfile
import com.f1.quiket.feature.mypage.domain.model.NotificationSettings
import com.f1.quiket.feature.mypage.domain.model.PasswordChange

fun GamificationDataResponse.toDomain(): Gamification = Gamification(
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

fun MyProfileDataResponse.toDomain(): MyProfile = MyProfile(
    id = id,
    email = email.orEmpty(),
    nickname = nickname,
    dotoriBalance = dotoriBalance,
    emailVerified = emailVerified,
    status = status,
    providers = providers,
    xpTotal = xpTotal,
    currentLevel = currentLevel,
    createdAt = createdAt,
)

fun String.toNicknameUpdateRequest(): NicknameUpdateRequest = NicknameUpdateRequest(
    nickname = this,
)

fun String.toMyEmailChangeRequest(): MyEmailChangeRequest = MyEmailChangeRequest(
    newEmail = this,
)

fun EmailVerificationSentDataResponse.toDomain(): EmailVerificationSent = EmailVerificationSent(
    email = email,
    expiresInSeconds = expiresInSeconds,
)

fun toMyEmailChangeConfirmRequest(
    newEmail: String,
    verificationCode: String,
): MyEmailChangeConfirmRequest = MyEmailChangeConfirmRequest(
    newEmail = newEmail,
    verificationCode = verificationCode,
)

fun PasswordChange.toRequest(): PasswordChangeRequest = PasswordChangeRequest(
    currentPassword = currentPassword,
    newPassword = newPassword,
    newPasswordConfirm = newPasswordConfirm,
)

fun AccountDelete.toRequest(): AccountDeleteRequest = AccountDeleteRequest(
    password = password,
    agreedToDelete = agreedToDelete,
)

fun NotificationSettingsDataResponse.toDomain(): NotificationSettings = NotificationSettings(
    fcmTokenRegistered = fcmTokenRegistered,
    activityEnabled = activityEnabled,
    updateEnabled = updateEnabled,
    reviewEnabled = reviewEnabled,
)

fun NotificationSettings.toRequest(): NotificationSettingsUpdateRequest =
    NotificationSettingsUpdateRequest(
        activityEnabled = activityEnabled,
        updateEnabled = updateEnabled,
        reviewEnabled = reviewEnabled,
    )

fun String.toFcmTokenUpdateRequest(): FcmTokenUpdateRequest = FcmTokenUpdateRequest(
    fcmToken = this,
)

fun FeedbackCreate.toRequest(): FeedbackCreateRequest = FeedbackCreateRequest(
    category = category.wireValue,
    body = body,
    replyEmail = replyEmail,
    appVersion = appVersion,
    osVersion = osVersion,
    deviceModel = deviceModel,
)

fun FeedbackDataResponse.toDomain(): Feedback = Feedback(
    id = id,
    category = category.toFeedbackCategory(),
    body = body,
    replyEmail = replyEmail,
    appVersion = appVersion,
    osVersion = osVersion,
    deviceModel = deviceModel,
    createdAt = createdAt,
)

private fun String.toFeedbackCategory(): FeedbackCategory =
    FeedbackCategory.entries.firstOrNull { category -> category.wireValue == this }
        ?: FeedbackCategory.Unknown
