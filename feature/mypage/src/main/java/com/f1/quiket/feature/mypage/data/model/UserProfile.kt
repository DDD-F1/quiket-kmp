package com.f1.quiket.feature.mypage.data.model

data class UserProfile(
    val userId: String = "",
    val nickname: String = "",
    val totalQuizCount: Int = 0,
    val streakDays: Int = 0,
    val acornCount: Int = 0,
)