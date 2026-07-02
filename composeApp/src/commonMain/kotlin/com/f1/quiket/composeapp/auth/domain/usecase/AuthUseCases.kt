package com.f1.quiket.composeapp.auth.domain.usecase

import com.f1.quiket.composeapp.auth.AuthTokenData
import com.f1.quiket.composeapp.auth.AuthUser
import com.f1.quiket.composeapp.auth.KakaoLoginResult
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.EmailAvailability
import com.f1.quiket.composeapp.auth.EmailVerificationSent
import com.f1.quiket.composeapp.auth.PasswordResetRequested
import com.f1.quiket.composeapp.auth.SignupData
import com.f1.quiket.composeapp.auth.domain.repository.AuthRepository
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository

internal data class CompletedLogin(
    val nickname: String,
    val homeGuideCompleted: Boolean,
)

internal class ReadSessionUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): SessionSnapshot =
        sessionRepository.read()
}

internal class SaveOnboardingCompletedUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() {
        sessionRepository.saveOnboardingCompleted()
    }
}

internal class SaveHomeGuideCompletedUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() {
        sessionRepository.saveHomeGuideCompleted()
    }
}

internal class ClearAuthUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() {
        sessionRepository.clearAuth()
    }
}

internal class CompleteLoginUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        tokenData: AuthTokenData,
        fallbackNickname: String = "사용자",
    ): CompletedLogin {
        sessionRepository.saveAuth(tokenData)
        val session = sessionRepository.read()
        return CompletedLogin(
            nickname = tokenData.user?.nickname ?: fallbackNickname,
            homeGuideCompleted = session.homeGuideCompleted,
        )
    }
}

internal class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AuthTokenData =
        authRepository.login(email = email, password = password)
}

internal class CheckEmailAvailabilityUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): EmailAvailability =
        authRepository.checkEmailAvailability(email)
}

internal class KakaoLoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(kakaoAccessToken: String): KakaoLoginResult =
        authRepository.kakaoLogin(kakaoAccessToken = kakaoAccessToken)
}

internal class SignupUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): SignupData = authRepository.signup(
        email = email,
        password = password,
        passwordConfirm = passwordConfirm,
        nickname = nickname,
    )
}

internal class ResendEmailVerificationUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): EmailVerificationSent =
        authRepository.resendEmailVerification(email)
}

internal class ConfirmEmailVerificationUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, verificationCode: String): AuthTokenData =
        authRepository.confirmEmailVerification(
            email = email,
            verificationCode = verificationCode,
        )
}

internal class CompleteKakaoNicknameUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(signupToken: String, nickname: String): AuthTokenData =
        authRepository.completeKakaoNickname(
            signupToken = signupToken,
            nickname = nickname,
        )
}

internal class LinkKakaoAccountUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(linkToken: String, email: String, password: String): AuthTokenData =
        authRepository.linkKakaoAccount(
            linkToken = linkToken,
            email = email,
            password = password,
        )
}

internal class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): PasswordResetRequested =
        authRepository.requestPasswordReset(email)
}

internal class ConfirmPasswordResetUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        verificationCode: String,
        newPassword: String,
        newPasswordConfirm: String,
    ) {
        authRepository.confirmPasswordReset(
            email = email,
            verificationCode = verificationCode,
            newPassword = newPassword,
            newPasswordConfirm = newPasswordConfirm,
        )
    }
}

internal class GetCurrentUserUseCase(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AuthUser =
        authenticatedCallRunner.run { session ->
            authRepository.getMe(session)
        }
}

internal class LogoutUseCase(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): SessionSnapshot {
        val session = sessionRepository.read()
        sessionRepository.clearAuth()
        if (session.isLoggedIn) {
            runCatching {
                authRepository.logout(session)
            }
        }
        return session
    }
}
