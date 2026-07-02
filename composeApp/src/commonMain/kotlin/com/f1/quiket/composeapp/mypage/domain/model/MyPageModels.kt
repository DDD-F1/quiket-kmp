package com.f1.quiket.composeapp.mypage.domain.model

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
