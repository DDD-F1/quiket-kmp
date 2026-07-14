package com.f1.quiket.composeapp.mypage.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.domain.model.Feedback
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCreate
import com.f1.quiket.composeapp.mypage.domain.model.MyPageData
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings

internal interface MyPageRemoteDataSource {
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

internal class MyPageRemoteDataSourceImpl(
    private val client: MyPageClient,
) : MyPageRemoteDataSource {
    override suspend fun getMyPage(session: SessionSnapshot): MyPageData =
        client.getMyPage(session)

    override suspend fun getMyProfile(session: SessionSnapshot): MyProfile =
        client.getMyProfile(session)

    override suspend fun updateMyNickname(session: SessionSnapshot, nickname: String): MyProfile =
        client.updateMyNickname(session = session, nickname = nickname)

    override suspend fun requestMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
    ): EmailVerificationSent =
        client.requestMyEmailChange(session = session, newEmail = newEmail)

    override suspend fun confirmMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
        verificationCode: String,
    ): MyProfile =
        client.confirmMyEmailChange(
            session = session,
            newEmail = newEmail,
            verificationCode = verificationCode,
        )

    override suspend fun updateMyPassword(
        session: SessionSnapshot,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        client.updateMyPassword(
            session = session,
            currentPassword = currentPassword,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }

    override suspend fun deleteMyAccount(session: SessionSnapshot, password: String?) {
        client.deleteMyAccount(session = session, password = password)
    }

    override suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings =
        client.getNotificationSettings(session)

    override suspend fun updateNotificationSettings(
        session: SessionSnapshot,
        settings: NotificationSettings,
    ): NotificationSettings =
        client.updateNotificationSettings(session = session, settings = settings)

    override suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String) {
        client.updateFcmToken(session = session, fcmToken = fcmToken)
    }

    override suspend fun createFeedback(
        session: SessionSnapshot,
        feedback: FeedbackCreate,
    ): Feedback =
        client.createFeedback(session = session, feedback = feedback)
}
