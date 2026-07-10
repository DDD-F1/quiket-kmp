package com.f1.quiket.composeapp.auth.data.repository

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.data.remote.AuthRemoteDataSource
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository

internal class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun checkEmailAvailability(email: String): EmailAvailability =
        remoteDataSource.checkEmailAvailability(email)

    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = remoteDataSource.signup(
        email = email,
        password = password,
        passwordConfirm = passwordConfirm,
        nickname = nickname,
    )

    override suspend fun resendEmailVerification(email: String): EmailVerificationSent =
        remoteDataSource.resendEmailVerification(email)

    override suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String,
    ): AuthTokenData = remoteDataSource.confirmEmailVerification(
        email = email,
        verificationCode = verificationCode,
    )

    override suspend fun login(email: String, password: String): AuthTokenData =
        remoteDataSource.login(email = email, password = password)

    override suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult =
        remoteDataSource.kakaoLogin(kakaoAccessToken = kakaoAccessToken)

    override suspend fun appleLogin(
        identityToken: String,
        authorizationCode: String?,
        fullName: String?,
    ): AppleLoginResult = remoteDataSource.appleLogin(
        identityToken = identityToken,
        authorizationCode = authorizationCode,
        fullName = fullName,
    )

    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData =
        remoteDataSource.completeKakaoNickname(signupToken = signupToken, nickname = nickname)

    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = remoteDataSource.linkKakaoAccount(
        linkToken = linkToken,
        email = email,
        password = password,
    )

    override suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData =
        remoteDataSource.completeAppleNickname(signupToken = signupToken, nickname = nickname)

    override suspend fun linkAppleAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = remoteDataSource.linkAppleAccount(
        linkToken = linkToken,
        email = email,
        password = password,
    )

    override suspend fun refreshToken(refreshToken: String): AuthTokenData =
        remoteDataSource.refreshToken(refreshToken)

    override suspend fun logout(session: SessionSnapshot) {
        remoteDataSource.logout(session)
    }

    override suspend fun getMe(session: SessionSnapshot): AuthUser =
        remoteDataSource.getMe(session)

    override suspend fun requestPasswordReset(email: String): PasswordResetRequested =
        remoteDataSource.requestPasswordReset(email)

    override suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        remoteDataSource.confirmPasswordReset(
            email = email,
            verificationCode = verificationCode,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }
}
