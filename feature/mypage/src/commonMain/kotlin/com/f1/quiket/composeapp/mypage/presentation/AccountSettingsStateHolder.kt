package com.f1.quiket.composeapp.mypage.presentation

import com.f1.quiket.composeapp.util.runSuspendCatching

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.f1.quiket.composeapp.login.EmailFormatErrorMessage
import com.f1.quiket.composeapp.login.NicknameFormatErrorMessage
import com.f1.quiket.composeapp.login.PasswordConfirmMismatchMessage
import com.f1.quiket.composeapp.login.PasswordPolicyErrorMessage
import com.f1.quiket.composeapp.login.isValidAuthPassword
import com.f1.quiket.composeapp.login.isValidEmail
import com.f1.quiket.composeapp.login.isValidNickname
import com.f1.quiket.composeapp.mypage.domain.model.MyPageException
import com.f1.quiket.composeapp.mypage.domain.model.MyProfile
import com.f1.quiket.composeapp.mypage.domain.usecase.MyPageUseCases
import com.f1.quiket.composeapp.network.toUserFacingMessage

internal class AccountSettingsStateHolder(
    private val myPageUseCases: MyPageUseCases,
) {
    var state by mutableStateOf(AccountSettingsUiState())
        private set

    var activeDialog by mutableStateOf<AccountDialog?>(null)
        private set

    suspend fun loadProfile(onSessionExpired: () -> Unit) {
        state = state.copy(isLoading = true, message = null)
        runSuspendCatching {
            myPageUseCases.getMyProfile()
        }.onSuccess { profile ->
            state = state.copy(
                isLoading = false,
                isSaving = false,
                profile = profile,
                message = null,
            )
        }.onFailure { error ->
            handleError(error, fallbackMessage = "계정 정보를 불러오지 못했습니다.", onSessionExpired)
        }
    }

    fun showNicknameDialog(currentNickname: String) {
        clearMessage()
        activeDialog = AccountDialog.Nickname(currentNickname)
    }

    fun showEmailRequestDialog() {
        clearMessage()
        activeDialog = AccountDialog.EmailRequest
    }

    fun showPasswordDialog() {
        clearMessage()
        activeDialog = AccountDialog.Password
    }

    fun showDeleteAccountDialog() {
        clearMessage()
        activeDialog = AccountDialog.DeleteAccount
    }

    fun dismissDialog() {
        activeDialog = null
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }

    fun setMessage(message: String) {
        state = state.copy(message = message)
    }

    suspend fun updateNickname(
        nickname: String,
        onSessionExpired: () -> Unit,
    ): MyProfile? {
        val trimmedNickname = nickname.trim()
        when {
            trimmedNickname.isBlank() -> {
                setMessage("닉네임을 입력해주세요.")
                return null
            }

            !isValidNickname(trimmedNickname) -> {
                setMessage(NicknameFormatErrorMessage)
                return null
            }
        }

        state = state.copy(isSaving = true, message = null)
        return runSuspendCatching {
            myPageUseCases.updateMyNickname(nickname = trimmedNickname)
        }.fold(
            onSuccess = { profile ->
                val savedProfile = profile.copy(nickname = trimmedNickname)
                state = state.copy(
                    isSaving = false,
                    profile = savedProfile,
                    message = "닉네임이 변경되었습니다.",
                )
                activeDialog = null
                savedProfile
            },
            onFailure = { error ->
                handleError(error, fallbackMessage = "닉네임을 변경하지 못했습니다.", onSessionExpired)
                null
            },
        )
    }

    suspend fun requestEmailChange(
        email: String,
        onSessionExpired: () -> Unit,
    ) {
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isBlank() -> {
                setMessage("새 이메일을 입력해주세요.")
                return
            }

            !isValidEmail(trimmedEmail) -> {
                setMessage(EmailFormatErrorMessage)
                return
            }
        }

        state = state.copy(isSaving = true, message = null)
        runSuspendCatching {
            myPageUseCases.requestMyEmailChange(newEmail = trimmedEmail)
        }.onSuccess { sent ->
            state = state.copy(
                isSaving = false,
                message = "${sent.email}로 인증 코드를 보냈습니다.",
            )
            activeDialog = AccountDialog.EmailConfirm(sent.email)
        }.onFailure { error ->
            handleError(error, fallbackMessage = "인증 메일을 보내지 못했습니다.", onSessionExpired)
        }
    }

    suspend fun confirmEmailChange(
        email: String,
        code: String,
        onSessionExpired: () -> Unit,
    ): MyProfile? {
        if (code.length < 6) {
            setMessage("6자리 인증 코드를 입력해주세요.")
            return null
        }

        state = state.copy(isSaving = true, message = null)
        return runSuspendCatching {
            myPageUseCases.confirmMyEmailChange(
                newEmail = email,
                verificationCode = code,
            )
        }.fold(
            onSuccess = { profile ->
                state = state.copy(
                    isSaving = false,
                    profile = profile,
                    message = "이메일이 변경되었습니다.",
                )
                activeDialog = null
                profile
            },
            onFailure = { error ->
                handleError(error, fallbackMessage = "이메일을 변경하지 못했습니다.", onSessionExpired)
                null
            },
        )
    }

    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String,
        onSessionExpired: () -> Unit,
    ) {
        when {
            currentPassword.isBlank() || newPassword.isBlank() || newPasswordConfirm.isBlank() -> {
                setMessage("비밀번호를 모두 입력해주세요.")
                return
            }

            !isValidAuthPassword(newPassword) -> {
                setMessage(PasswordPolicyErrorMessage)
                return
            }

            newPassword != newPasswordConfirm -> {
                setMessage(PasswordConfirmMismatchMessage)
                return
            }
        }

        state = state.copy(isSaving = true, message = null)
        runSuspendCatching {
            myPageUseCases.updateMyPassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                newPasswordConfirm = newPasswordConfirm,
            )
        }.onSuccess {
            state = state.copy(
                isSaving = false,
                message = "비밀번호가 변경되었습니다.",
            )
            activeDialog = null
        }.onFailure { error ->
            handleError(error, fallbackMessage = "비밀번호를 변경하지 못했습니다.", onSessionExpired)
        }
    }

    suspend fun deleteAccount(
        password: String,
        onSessionExpired: () -> Unit,
    ): Boolean {
        if (state.isLocalAccount && password.isBlank()) {
            setMessage("비밀번호를 입력해주세요.")
            return false
        }

        state = state.copy(isSaving = true, message = null)
        return runSuspendCatching {
            myPageUseCases.deleteMyAccount(
                password = password.takeIf { it.isNotBlank() },
            )
        }.fold(
            onSuccess = {
                activeDialog = null
                true
            },
            onFailure = { error ->
                handleError(error, fallbackMessage = "회원 탈퇴를 처리하지 못했습니다.", onSessionExpired)
                false
            },
        )
    }

    private fun handleError(
        error: Throwable,
        fallbackMessage: String,
        onSessionExpired: () -> Unit,
    ) {
        if (error is MyPageException && error.isUnauthorized) {
            onSessionExpired()
        } else {
            state = state.copy(
                isLoading = false,
                isSaving = false,
                message = error.toUserFacingMessage(fallbackMessage),
            )
        }
    }
}
