package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.network.auth.TokenPair
import com.f1.quiket.core.testing.MainDispatcherRule
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.domain.model.AuthProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.AuthTokenData
import com.f1.quiket.feature.login.domain.model.AuthUser
import com.f1.quiket.feature.login.domain.model.EmailAvailability
import com.f1.quiket.feature.login.domain.model.EmailVerificationSent
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.model.PasswordResetRequested
import com.f1.quiket.feature.login.domain.model.SignupData
import com.f1.quiket.feature.login.domain.model.UserStatus
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_success_emitsNavigateToMainAndPassesDeviceHeaders() = runTest {
        val repository = FakeAuthRepository().apply {
            loginResult = AuthResult.Success(authTokenData())
        }
        val viewModel = LoginEmailViewModel(
            repository = repository,
            deviceInfoProvider = FakeDeviceInfoProvider,
            passwordResetDraftStore = PasswordResetDraftStore(),
            signupDraftStore = SignupDraftStore(),
        )
        val effect = async { viewModel.effect.first() }

        viewModel.onIntent(LoginEmailIntent.EmailChanged(" user@example.com "))
        viewModel.onIntent(LoginEmailIntent.PasswordChanged("Password123!"))
        viewModel.onIntent(LoginEmailIntent.Login)
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(LoginEmailEffect.NavigateToMain)
        assertThat(repository.lastLoginEmail).isEqualTo("user@example.com")
        assertThat(repository.lastLoginDeviceId).isEqualTo("device-id")
        assertThat(repository.lastLoginDeviceName).isEqualTo("Pixel")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun login_failureWithFailedCount_setsPasswordErrorMessage() = runTest {
        val repository = FakeAuthRepository().apply {
            loginResult = AuthResult.Failure(
                code = "AUTH_LOGIN_FAILED",
                message = "Login failed.",
                failedLoginCount = 2,
            )
        }
        val viewModel = LoginEmailViewModel(
            repository = repository,
            deviceInfoProvider = FakeDeviceInfoProvider,
            passwordResetDraftStore = PasswordResetDraftStore(),
            signupDraftStore = SignupDraftStore(),
        )

        viewModel.onIntent(LoginEmailIntent.EmailChanged("user@example.com"))
        viewModel.onIntent(LoginEmailIntent.PasswordChanged("wrong-password"))
        viewModel.onIntent(LoginEmailIntent.Login)
        advanceUntilIdle()

        assertThat(viewModel.state.value.passwordErrorMessage).isEqualTo("비밀번호를 다시 입력해주세요 (2/5)")
    }

    @Test
    fun login_accountLocked_showsResetDialogAndNavigatesResetOnClick() = runTest {
        val passwordResetDraftStore = PasswordResetDraftStore()
        val repository = FakeAuthRepository().apply {
            loginResult = AuthResult.Failure(
                code = "AUTH_ACCOUNT_LOCKED",
                message = "Account locked.",
                email = "locked@example.com",
                resetCodeSent = true,
            )
        }
        val viewModel = LoginEmailViewModel(
            repository = repository,
            deviceInfoProvider = FakeDeviceInfoProvider,
            passwordResetDraftStore = passwordResetDraftStore,
            signupDraftStore = SignupDraftStore(),
        )

        viewModel.onIntent(LoginEmailIntent.EmailChanged("user@example.com"))
        viewModel.onIntent(LoginEmailIntent.PasswordChanged("wrong-password"))
        viewModel.onIntent(LoginEmailIntent.Login)
        advanceUntilIdle()

        assertThat(viewModel.state.value.showPasswordResetRequiredDialog).isTrue()
        assertThat(passwordResetDraftStore.get().email).isEqualTo("locked@example.com")
        assertThat(passwordResetDraftStore.get().resetCodeSent).isTrue()

        val effect = async { viewModel.effect.first() }
        viewModel.onIntent(LoginEmailIntent.PasswordResetRequiredClick)
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(LoginEmailEffect.NavigateToPasswordReset("locked@example.com"))
        assertThat(viewModel.state.value.showPasswordResetRequiredDialog).isFalse()
    }

    @Test
    fun login_emailNotVerified_storesEmailAndNavigatesVerification() = runTest {
        val signupDraftStore = SignupDraftStore()
        val repository = FakeAuthRepository().apply {
            loginResult = AuthResult.Failure(
                code = "AUTH_EMAIL_NOT_VERIFIED",
                message = "Email verification required.",
                email = "verify@example.com",
            )
            resendEmailVerificationResult = AuthResult.Success(
                EmailVerificationSent(
                    email = "verify@example.com",
                    expiresInSeconds = 600,
                ),
            )
        }
        val viewModel = LoginEmailViewModel(
            repository = repository,
            deviceInfoProvider = FakeDeviceInfoProvider,
            passwordResetDraftStore = PasswordResetDraftStore(),
            signupDraftStore = signupDraftStore,
        )
        val effect = async { viewModel.effect.first() }

        viewModel.onIntent(LoginEmailIntent.EmailChanged("user@example.com"))
        viewModel.onIntent(LoginEmailIntent.PasswordChanged("Password123!"))
        viewModel.onIntent(LoginEmailIntent.Login)
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(LoginEmailEffect.NavigateToEmailVerification("verify@example.com"))
        assertThat(signupDraftStore.get().email).isEqualTo("verify@example.com")
        assertThat(repository.lastResendEmail).isEqualTo("verify@example.com")
    }

    @Test
    fun passwordResetEmailVerification_prefilledWithoutSentCode_requiresRequestCode() = runTest {
        val passwordResetDraftStore = PasswordResetDraftStore().apply {
            update { copy(email = "locked@example.com", resetCodeSent = false) }
        }
        val viewModel = PasswordResetEmailVerificationViewModel(
            repository = FakeAuthRepository(),
            passwordResetDraftStore = passwordResetDraftStore,
        )

        assertThat(viewModel.state.value.email).isEqualTo("locked@example.com")
        assertThat(viewModel.state.value.isVerificationRequested).isFalse()
        assertThat(viewModel.state.value.showVerificationSentMessage).isFalse()
    }

    @Test
    fun passwordResetEmailVerification_prefilledWithSentCode_entersCodeStep() = runTest {
        val passwordResetDraftStore = PasswordResetDraftStore().apply {
            update { copy(email = "locked@example.com", resetCodeSent = true) }
        }
        val viewModel = PasswordResetEmailVerificationViewModel(
            repository = FakeAuthRepository(),
            passwordResetDraftStore = passwordResetDraftStore,
        )

        assertThat(viewModel.state.value.email).isEqualTo("locked@example.com")
        assertThat(viewModel.state.value.isVerificationRequested).isTrue()
        assertThat(viewModel.state.value.showVerificationSentMessage).isTrue()
    }

    @Test
    fun signupCodeVerification_success_clearsDraftAndEmitsNavigateToMain() = runTest {
        val signupDraftStore = SignupDraftStore().apply {
            update { copy(email = "signup@example.com") }
        }
        val repository = FakeAuthRepository().apply {
            confirmEmailVerificationResult = AuthResult.Success(authTokenData())
        }
        val viewModel = SignupCodeVerificationViewModel(
            repository = repository,
            deviceInfoProvider = FakeDeviceInfoProvider,
            signupDraftStore = signupDraftStore,
        )
        val effect = async { viewModel.effect.first() }

        viewModel.onIntent(SignupCodeVerificationIntent.VerificationCodeChanged("123456"))
        viewModel.onIntent(SignupCodeVerificationIntent.CodeActionClick)
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(SignupCodeVerificationEffect.NavigateToMain)
        assertThat(repository.lastConfirmEmail).isEqualTo("signup@example.com")
        assertThat(repository.lastConfirmCode).isEqualTo("123456")
        assertThat(repository.lastConfirmDeviceId).isEqualTo("device-id")
        assertThat(signupDraftStore.get()).isEqualTo(SignupDraft())
    }

    @Test
    fun passwordResetNewPassword_success_usesEmailAndCodeThenNavigatesLogin() = runTest {
        val passwordResetDraftStore = PasswordResetDraftStore().apply {
            update {
                copy(
                    email = "reset@example.com",
                    verificationCode = "654321",
                )
            }
        }
        val repository = FakeAuthRepository().apply {
            confirmPasswordResetResult = AuthResult.Success(Unit)
        }
        val viewModel = PasswordResetNewPasswordViewModel(
            repository = repository,
            passwordResetDraftStore = passwordResetDraftStore,
        )
        val effect = async { viewModel.effect.first() }

        viewModel.onIntent(PasswordResetNewPasswordIntent.PasswordChanged("Password123!"))
        viewModel.onIntent(PasswordResetNewPasswordIntent.PasswordConfirmChanged("Password123!"))
        viewModel.onIntent(PasswordResetNewPasswordIntent.Submit)
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(PasswordResetNewPasswordEffect.NavigateToLogin)
        assertThat(repository.lastPasswordResetEmail).isEqualTo("reset@example.com")
        assertThat(repository.lastPasswordResetCode).isEqualTo("654321")
        assertThat(repository.lastPasswordResetNewPassword).isEqualTo("Password123!")
        assertThat(passwordResetDraftStore.get()).isEqualTo(PasswordResetDraft())
    }
}

private object FakeDeviceInfoProvider : DeviceInfoProvider {
    override val deviceId: String = "device-id"
    override val deviceName: String = "Pixel"
}

private class FakeAuthRepository : AuthRepository {
    var loginResult: AuthResult<AuthTokenData> = AuthResult.Failure("UNHANDLED", "Unhandled")
    var confirmEmailVerificationResult: AuthResult<AuthTokenData> = AuthResult.Failure("UNHANDLED", "Unhandled")
    var confirmPasswordResetResult: AuthResult<Unit> = AuthResult.Failure("UNHANDLED", "Unhandled")
    var resendEmailVerificationResult: AuthResult<EmailVerificationSent> = AuthResult.Failure("UNHANDLED", "Unhandled")

    var lastLoginEmail: String? = null
    var lastLoginDeviceId: String? = null
    var lastLoginDeviceName: String? = null
    var lastResendEmail: String? = null
    var lastConfirmEmail: String? = null
    var lastConfirmCode: String? = null
    var lastConfirmDeviceId: String? = null
    var lastPasswordResetEmail: String? = null
    var lastPasswordResetCode: String? = null
    var lastPasswordResetNewPassword: String? = null

    override suspend fun login(
        email: String,
        password: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> {
        lastLoginEmail = email
        lastLoginDeviceId = deviceId
        lastLoginDeviceName = deviceName
        return loginResult
    }

    override suspend fun confirmEmailVerification(
        email: String,
        verificationCode: String?,
        verificationToken: String?,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> {
        lastConfirmEmail = email
        lastConfirmCode = verificationCode
        lastConfirmDeviceId = deviceId
        return confirmEmailVerificationResult
    }

    override suspend fun confirmPasswordReset(
        email: String,
        newPassword: String,
        newPasswordConfirm: String,
        resetToken: String?,
        verificationCode: String?,
    ): AuthResult<Unit> {
        lastPasswordResetEmail = email
        lastPasswordResetCode = verificationCode
        lastPasswordResetNewPassword = newPassword
        return confirmPasswordResetResult
    }

    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
    ): AuthResult<SignupData> = unhandled()

    override suspend fun checkEmailAvailability(email: String): AuthResult<EmailAvailability> = unhandled()

    override suspend fun resendEmailVerification(email: String): AuthResult<EmailVerificationSent> {
        lastResendEmail = email
        return resendEmailVerificationResult
    }

    override suspend fun refreshToken(
        refreshToken: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = unhandled()

    override suspend fun logout(refreshToken: String?): AuthResult<Unit> = unhandled()

    override suspend fun requestPasswordReset(email: String): AuthResult<PasswordResetRequested> = unhandled()

    override suspend fun kakaoLogin(
        kakaoAccessToken: String,
        agreedToTerms: Boolean,
        deviceId: String?,
        deviceName: String?,
    ): KakaoLoginResult = KakaoLoginResult.Failure(unhandled())

    override suspend fun linkKakaoAccount(
        linkToken: String,
        email: String,
        password: String,
        agreedToLink: Boolean,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = unhandled()

    override suspend fun completeKakaoNickname(
        signupToken: String,
        nickname: String,
        deviceId: String?,
        deviceName: String?,
    ): AuthResult<AuthTokenData> = unhandled()

    override suspend fun getMe(): AuthResult<AuthUser> = unhandled()

    private fun unhandled(): AuthResult.Failure = AuthResult.Failure("UNHANDLED", "Unhandled")
}

private fun authTokenData() = AuthTokenData(
    tokenPair = TokenPair(
        accessToken = "access",
        refreshToken = "refresh",
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 1_209_600,
    ),
    user = AuthUser(
        id = "user-id",
        email = "user@example.com",
        nickname = "tester",
        dotoriBalance = 0,
        emailVerified = true,
        status = UserStatus.Active,
        providers = listOf(AuthProvider.Local),
    ),
)
