package com.f1.quiket.feature.mypage.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NicknameUpdateRequest(
    val nickname: String,
)

@Serializable
data class MyEmailChangeRequest(
    val newEmail: String,
)

@Serializable
data class MyEmailChangeConfirmRequest(
    val newEmail: String,
    val verificationCode: String,
)

@Serializable
data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String,
)

@Serializable
data class AccountDeleteRequest(
    val password: String? = null,
    val agreedToDelete: Boolean,
)

@Serializable
data class NotificationSettingsUpdateRequest(
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
)

@Serializable
data class FcmTokenUpdateRequest(
    val fcmToken: String,
)

@Serializable
data class FeedbackCreateRequest(
    val category: String,
    val body: String,
    val replyEmail: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
)
