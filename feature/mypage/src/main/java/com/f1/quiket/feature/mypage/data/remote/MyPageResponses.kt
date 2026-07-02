package com.f1.quiket.feature.mypage.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class GamificationDataResponse(
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
)

@Serializable
data class MyProfileDataResponse(
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
)

@Serializable
data class EmailVerificationSentDataResponse(
    val email: String,
    val expiresInSeconds: Long,
)

@Serializable
data class NotificationSettingsDataResponse(
    val fcmTokenRegistered: Boolean? = null,
    val activityEnabled: Boolean,
    val updateEnabled: Boolean,
    val reviewEnabled: Boolean,
)

@Serializable
data class FeedbackDataResponse(
    val id: String,
    val category: String,
    val body: String,
    val replyEmail: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
    val createdAt: String,
)
