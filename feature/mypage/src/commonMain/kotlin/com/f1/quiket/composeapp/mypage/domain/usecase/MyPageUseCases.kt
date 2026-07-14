package com.f1.quiket.composeapp.mypage.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.mypage.domain.model.Feedback
import com.f1.quiket.composeapp.mypage.domain.model.FeedbackCreate
import com.f1.quiket.composeapp.mypage.domain.model.MyPageData
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.model.NotificationSettings
import com.f1.quiket.composeapp.mypage.domain.repository.MyPageRepository

class MyPageUseCases internal constructor(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: MyPageRepository,
) {
    suspend fun getMyPage(): MyPageData =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getMyPage(session)
        }

    suspend fun getMyProfile(): MyProfile =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getMyProfile(session)
        }

    suspend fun updateMyNickname(nickname: String): MyProfile =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateMyNickname(session = session, nickname = nickname)
        }

    suspend fun requestMyEmailChange(newEmail: String): EmailVerificationSent =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.requestMyEmailChange(session = session, newEmail = newEmail)
        }

    suspend fun confirmMyEmailChange(
        newEmail: String,
        verificationCode: String,
    ): MyProfile =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.confirmMyEmailChange(
                session = session,
                newEmail = newEmail,
                verificationCode = verificationCode,
            )
        }

    suspend fun updateMyPassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateMyPassword(
                session = session,
                currentPassword = currentPassword,
                newPassword = newPassword,
                newPasswordConfirm = newPasswordConfirm,
            )
        }
    }

    suspend fun deleteMyAccount(password: String?) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.deleteMyAccount(session = session, password = password)
        }
    }

    suspend fun getNotificationSettings(): NotificationSettings =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getNotificationSettings(session)
        }

    suspend fun updateNotificationSettings(settings: NotificationSettings): NotificationSettings =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateNotificationSettings(session = session, settings = settings)
        }

    suspend fun updateFcmToken(fcmToken: String) {
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.updateFcmToken(session = session, fcmToken = fcmToken)
        }
    }

    suspend fun createFeedback(feedback: FeedbackCreate): Feedback =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.createFeedback(session = session, feedback = feedback)
        }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is MyPageException && error.isUnauthorized
}
