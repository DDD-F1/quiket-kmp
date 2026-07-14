package com.f1.quiket.composeapp.auth.data.remote

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData

internal interface AuthRemoteDataSource {
    suspend fun checkEmailAvailability(email: String): EmailAvailability
    suspend fun signup(email: String, password: String, passwordConfirm: String, nickname: String): SignupData
    suspend fun resendEmailVerification(email: String): EmailVerificationSent
    suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData
    suspend fun login(email: String, password: String): AuthTokenData
    suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult
    suspend fun appleLogin(identityToken: String, authorizationCode: String?, fullName: String?): AppleLoginResult
    suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData
    suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData
    suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData
    suspend fun linkAppleAccount(linkToken: String, email: String, password: String): AuthTokenData
    suspend fun refreshToken(refreshToken: String): AuthTokenData
    suspend fun logout(session: SessionSnapshot)
    suspend fun getMe(session: SessionSnapshot): AuthUser
    suspend fun requestPasswordReset(email: String): PasswordResetRequested
    suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    )
}

internal class AuthRemoteDataSourceImpl(
    private val client: AuthClient,
) : AuthRemoteDataSource {
    override suspend fun checkEmailAvailability(email: String): EmailAvailability =
        client.checkEmailAvailability(email)

    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = client.signup(
        email = email,
        password = password,
        passwordConfirm = passwordConfirm,
        nickname = nickname,
    )

    override suspend fun resendEmailVerification(email: String): EmailVerificationSent =
        client.resendEmailVerification(email)

    override suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String,
    ): AuthTokenData = client.confirmEmailVerification(
        email = email,
        verificationCode = verificationCode,
    )

    override suspend fun login(email: String, password: String): AuthTokenData =
        client.login(email = email, password = password)

    override suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult =
        client.kakaoLogin(kakaoAccessToken = kakaoAccessToken)

    override suspend fun appleLogin(
        identityToken: String,
        authorizationCode: String?,
        fullName: String?,
    ): AppleLoginResult = client.appleLogin(
        identityToken = identityToken,
        authorizationCode = authorizationCode,
        fullName = fullName,
    )

    override suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData =
        client.completeKakaoNickname(signupToken = signupToken, nickname = nickname)

    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = client.linkKakaoAccount(
        linkToken = linkToken,
        email = email,
        password = password,
    )

    override suspend fun completeAppleNickname(signupToken: String, nickname: String): AuthTokenData =
        client.completeAppleNickname(signupToken = signupToken, nickname = nickname)

    override suspend fun linkAppleAccount(
        linkToken: String,
        email: String,
        password: String,
    ): AuthTokenData = client.linkAppleAccount(
        linkToken = linkToken,
        email = email,
        password = password,
    )

    override suspend fun refreshToken(refreshToken: String): AuthTokenData =
        client.refreshToken(refreshToken)

    override suspend fun logout(session: SessionSnapshot) {
        client.logout(session)
    }

    override suspend fun getMe(session: SessionSnapshot): AuthUser =
        client.getMe(session)

    override suspend fun requestPasswordReset(email: String): PasswordResetRequested =
        client.requestPasswordReset(email)

    override suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        client.confirmPasswordReset(
            email = email,
            verificationCode = verificationCode,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }
}
