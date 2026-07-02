package com.f1.quiket.feature.mypage.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.mypage.data.mapper.toDomain
import com.f1.quiket.feature.mypage.data.mapper.toFcmTokenUpdateRequest
import com.f1.quiket.feature.mypage.data.mapper.toMyEmailChangeConfirmRequest
import com.f1.quiket.feature.mypage.data.mapper.toMyEmailChangeRequest
import com.f1.quiket.feature.mypage.data.mapper.toNicknameUpdateRequest
import com.f1.quiket.feature.mypage.data.mapper.toRequest
import com.f1.quiket.feature.mypage.data.remote.MyPageApi
import com.f1.quiket.feature.mypage.domain.model.AccountDelete
import com.f1.quiket.feature.mypage.domain.model.EmailVerificationSent
import com.f1.quiket.feature.mypage.domain.model.Feedback
import com.f1.quiket.feature.mypage.domain.model.FeedbackCreate
import com.f1.quiket.feature.mypage.domain.model.Gamification
import com.f1.quiket.feature.mypage.domain.model.MyProfile
import com.f1.quiket.feature.mypage.domain.model.NotificationSettings
import com.f1.quiket.feature.mypage.domain.model.PasswordChange
import com.f1.quiket.feature.mypage.domain.repository.MyPageRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class MyPageRepositoryImpl @Inject constructor(
    private val api: MyPageApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : MyPageRepository {
    override suspend fun getMyGamification(): NetworkResult<Gamification> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getMyGamification() },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun getMyProfile(): NetworkResult<MyProfile> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getMyProfile() },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun updateMyNickname(nickname: String): NetworkResult<MyProfile> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.updateMyNickname(nickname.toNicknameUpdateRequest()) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun requestMyEmailChange(newEmail: String): NetworkResult<EmailVerificationSent> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.requestMyEmailChange(newEmail.toMyEmailChangeRequest()) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun confirmMyEmailChange(
        newEmail: String,
        verificationCode: String,
    ): NetworkResult<MyProfile> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = {
                api.confirmMyEmailChange(
                    toMyEmailChangeConfirmRequest(
                        newEmail = newEmail,
                        verificationCode = verificationCode,
                    ),
                )
            },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun updateMyPassword(request: PasswordChange): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.updateMyPassword(request.toRequest()) }
        }

    override suspend fun deleteMyAccount(request: AccountDelete): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.deleteMyAccount(request.toRequest()) }
        }

    override suspend fun getNotificationSettings(): NetworkResult<NotificationSettings> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getNotificationSettings() },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun updateNotificationSettings(
        settings: NotificationSettings,
    ): NetworkResult<NotificationSettings> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.updateNotificationSettings(settings.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun updateFcmToken(fcmToken: String): NetworkResult<Unit> =
        withContext(dispatchers.io) {
            responseHandler.executeEmpty { api.updateFcmToken(fcmToken.toFcmTokenUpdateRequest()) }
        }

    override suspend fun createFeedback(request: FeedbackCreate): NetworkResult<Feedback> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.createFeedback(request.toRequest()) },
                mapper = { response -> response.toDomain() },
            )
        }
}
