package com.f1.quiket.composeapp.mypage.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.mypage.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.Feedback
import com.f1.quiket.composeapp.mypage.FeedbackCreate
import com.f1.quiket.composeapp.mypage.MyPageData
import com.f1.quiket.composeapp.mypage.MyProfile
import com.f1.quiket.composeapp.mypage.NotificationSettings
import com.f1.quiket.composeapp.mypage.data.remote.MyPageRemoteDataSource
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository

internal class MyPageRepositoryImpl(
    private val remoteDataSource: MyPageRemoteDataSource,
) : MyPageRepository {
    override suspend fun getMyPage(session: SessionSnapshot): MyPageData =
        remoteDataSource.getMyPage(session)

    override suspend fun getMyProfile(session: SessionSnapshot): MyProfile =
        remoteDataSource.getMyProfile(session)

    override suspend fun updateMyNickname(session: SessionSnapshot, nickname: String): MyProfile =
        remoteDataSource.updateMyNickname(session = session, nickname = nickname)

    override suspend fun requestMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
    ): EmailVerificationSent =
        remoteDataSource.requestMyEmailChange(session = session, newEmail = newEmail)

    override suspend fun confirmMyEmailChange(
        session: SessionSnapshot,
        newEmail: String,
        verificationCode: String,
    ): MyProfile =
        remoteDataSource.confirmMyEmailChange(
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
        remoteDataSource.updateMyPassword(
            session = session,
            currentPassword = currentPassword,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }

    override suspend fun deleteMyAccount(session: SessionSnapshot, password: String?) {
        remoteDataSource.deleteMyAccount(session = session, password = password)
    }

    override suspend fun getNotificationSettings(session: SessionSnapshot): NotificationSettings =
        remoteDataSource.getNotificationSettings(session)

    override suspend fun updateNotificationSettings(
        session: SessionSnapshot,
        settings: NotificationSettings,
    ): NotificationSettings =
        remoteDataSource.updateNotificationSettings(session = session, settings = settings)

    override suspend fun updateFcmToken(session: SessionSnapshot, fcmToken: String) {
        remoteDataSource.updateFcmToken(session = session, fcmToken = fcmToken)
    }

    override suspend fun createFeedback(
        session: SessionSnapshot,
        feedback: FeedbackCreate,
    ): Feedback =
        remoteDataSource.createFeedback(session = session, feedback = feedback)
}
