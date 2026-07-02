package com.f1.quiket.feature.mypage.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.mypage.domain.model.AccountDelete
import com.f1.quiket.feature.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.feature.mypage.domain.model.Feedback
import com.f1.quiket.feature.mypage.domain.model.FeedbackCreate
import com.f1.quiket.feature.mypage.domain.model.Gamification
import com.f1.quiket.feature.mypage.domain.model.MyProfile
import com.f1.quiket.feature.mypage.domain.model.NotificationSettings
import com.f1.quiket.feature.mypage.domain.model.PasswordChange

interface MyPageRepository {
    suspend fun getMyGamification(): NetworkResult<Gamification>

    suspend fun getMyProfile(): NetworkResult<MyProfile>

    suspend fun updateMyNickname(nickname: String): NetworkResult<MyProfile>

    suspend fun requestMyEmailChange(newEmail: String): NetworkResult<EmailVerificationSent>

    suspend fun confirmMyEmailChange(
        newEmail: String,
        verificationCode: String,
    ): NetworkResult<MyProfile>

    suspend fun updateMyPassword(request: PasswordChange): NetworkResult<Unit>

    suspend fun deleteMyAccount(request: AccountDelete): NetworkResult<Unit>

    suspend fun getNotificationSettings(): NetworkResult<NotificationSettings>

    suspend fun updateNotificationSettings(settings: NotificationSettings): NetworkResult<NotificationSettings>

    suspend fun updateFcmToken(fcmToken: String): NetworkResult<Unit>

    suspend fun createFeedback(request: FeedbackCreate): NetworkResult<Feedback>
}
