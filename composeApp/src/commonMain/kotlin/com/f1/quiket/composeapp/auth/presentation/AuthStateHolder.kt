package com.f1.quiket.composeapp.auth.presentation

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthUser
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.domain.model.EmailAvailability
import com.f1.quiket.composeapp.auth.domain.model.EmailVerificationSent
import com.f1.quiket.composeapp.auth.domain.model.PasswordResetRequested
import com.f1.quiket.composeapp.auth.domain.model.SignupData
import com.f1.quiket.composeapp.auth.domain.usecase.CheckEmailAvailabilityUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ClearAuthUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.CompletedLogin
import com.f1.quiket.composeapp.auth.domain.usecase.CompleteLoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.CompleteKakaoNicknameUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ConfirmPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.GetCurrentUserUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.KakaoLoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LinkKakaoAccountUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LoginUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.LogoutUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ReadSessionUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.ResendEmailVerificationUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.RequestPasswordResetUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SaveOnboardingCompletedUseCase
import com.f1.quiket.composeapp.auth.domain.usecase.SignupUseCase

internal class AuthStateHolder(
    private val readSessionUseCase: ReadSessionUseCase,
    private val saveOnboardingCompletedUseCase: SaveOnboardingCompletedUseCase,
    private val clearAuthUseCase: ClearAuthUseCase,
    private val completeLoginUseCase: CompleteLoginUseCase,
    private val checkEmailAvailabilityUseCase: CheckEmailAvailabilityUseCase,
    private val loginUseCase: LoginUseCase,
    private val kakaoLoginUseCase: KakaoLoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val resendEmailVerificationUseCase: ResendEmailVerificationUseCase,
    private val confirmEmailVerificationUseCase: ConfirmEmailVerificationUseCase,
    private val completeKakaoNicknameUseCase: CompleteKakaoNicknameUseCase,
    private val linkKakaoAccountUseCase: LinkKakaoAccountUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
) {
    suspend fun readSession(): SessionSnapshot =
        readSessionUseCase()

    suspend fun saveOnboardingCompleted() {
        saveOnboardingCompletedUseCase()
    }

    suspend fun clearAuth() {
        clearAuthUseCase()
    }

    suspend fun completeLogin(
        tokenData: AuthTokenData,
        fallbackNickname: String = "사용자",
    ): CompletedLogin =
        completeLoginUseCase(
            tokenData = tokenData,
            fallbackNickname = fallbackNickname,
        )

    suspend fun login(email: String, password: String): AuthTokenData =
        loginUseCase(email = email, password = password)

    suspend fun checkEmailAvailability(email: String): EmailAvailability =
        checkEmailAvailabilityUseCase(email)

    suspend fun kakaoLogin(kakaoAccessToken: String): KakaoLoginResult =
        kakaoLoginUseCase(kakaoAccessToken = kakaoAccessToken)

    suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = signupUseCase(
        email = email,
        password = password,
        passwordConfirm = passwordConfirm,
        nickname = nickname,
    )

    suspend fun resendEmailVerification(email: String): EmailVerificationSent =
        resendEmailVerificationUseCase(email)

    suspend fun confirmEmailVerification(email: String, verificationCode: String): AuthTokenData =
        confirmEmailVerificationUseCase(
            email = email,
            verificationCode = verificationCode,
        )

    suspend fun completeKakaoNickname(signupToken: String, nickname: String): AuthTokenData =
        completeKakaoNicknameUseCase(
            signupToken = signupToken,
            nickname = nickname,
        )

    suspend fun linkKakaoAccount(linkToken: String, email: String, password: String): AuthTokenData =
        linkKakaoAccountUseCase(
            linkToken = linkToken,
            email = email,
            password = password,
        )

    suspend fun requestPasswordReset(email: String): PasswordResetRequested =
        requestPasswordResetUseCase(email)

    suspend fun confirmPasswordReset(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        confirmPasswordResetUseCase(
            email = email,
            verificationCode = verificationCode,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }

    suspend fun getCurrentUser(): AuthUser =
        getCurrentUserUseCase()

    suspend fun logout(): SessionSnapshot =
        logoutUseCase()
}
