package com.f1.quiket.feature.mypage.domain.model

data class Gamification(
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

data class MyProfile(
    val id: String,
    val email: String,
    val nickname: String,
    val dotoriBalance: Int,
    val emailVerified: Boolean,
    val status: String,
    val providers: List<String>,
    val xpTotal: Int?,
    val currentLevel: Int?,
    val createdAt: String?,
)

data class EmailVerificationSent(
    val email: String,
    val expiresInSeconds: Long,
)

data class PasswordChange(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String,
)

data class AccountDelete(
    val password: String? = null,
    val agreedToDelete: Boolean,
)

data class NotificationSettings(
    val fcmTokenRegistered: Boolean?,
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
)

data class FeedbackCreate(
    val category: FeedbackCategory,
    val body: String,
    val replyEmail: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
)

data class Feedback(
    val id: String,
    val category: FeedbackCategory,
    val body: String,
    val replyEmail: String?,
    val appVersion: String?,
    val osVersion: String?,
    val deviceModel: String?,
    val createdAt: String,
)

enum class FeedbackCategory(
    val wireValue: String,
) {
    Feature("feature"),
    Bug("bug"),
    Inquiry("inquiry"),
    Other("other"),
    Unknown("unknown"),
}
