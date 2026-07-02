package com.f1.quiket.composeapp.mypage.domain.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.mypage.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.Feedback
import com.f1.quiket.composeapp.mypage.FeedbackCreate
import com.f1.quiket.composeapp.mypage.MyPageData
import com.f1.quiket.composeapp.mypage.MyProfile
import com.f1.quiket.composeapp.mypage.NotificationSettings

internal interface MyPageRepository {
    suspend fun getMyPage(session: SessionSnapshot): MyPageData
    suspend fun getMyProfile(session: SessionSnapshot): MyProfile
    suspend fun updateMyNickname(session: SessionSnapshot, nickname: String): MyProfile
    suspend fun requestMyEmailChange(session: SessionSnapshot, newEmail: String): EmailVerificationSent
    suspend fun confirmMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
        verificationCode: String,
    ): MyProfile
    suspend fun updateMyPassword(
        session: SessionSnapshot,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String,
    )
    suspend fun deleteMyAccount(session: SessionSnapshot, password: String?)
    suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings
    suspend fun updateNotificationSettings(
        session: SessionSnapshot,
        settings: NotificationSettings,
    ): NotificationSettings
    suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String)
    suspend fun createFeedback(session: SessionSnapshot, feedback: FeedbackCreate): Feedback
}
