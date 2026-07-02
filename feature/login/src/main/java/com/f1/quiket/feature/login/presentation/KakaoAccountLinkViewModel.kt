package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KakaoAccountLinkViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val kakaoAuthDraftStore: KakaoAuthDraftStore,
) : MviViewModel<KakaoAccountLinkState, KakaoAccountLinkIntent, KakaoAccountLinkEffect>(
    initialState = KakaoAccountLinkState(
        email = kakaoAuthDraftStore.get().linkEmail,
    ),
) {
    override fun handleIntent(intent: KakaoAccountLinkIntent) {
        when (intent) {
            is KakaoAccountLinkIntent.PasswordChanged -> updateState {
                copy(password = intent.value, passwordErrorMessage = null)
            }

            KakaoAccountLinkIntent.TogglePasswordVisibility -> updateState {
                copy(isPasswordVisible = !isPasswordVisible)
            }

            KakaoAccountLinkIntent.Submit -> submit()
        }
    }

    private fun submit() {
        launch {
            updateState { copy(isSubmitting = true, passwordErrorMessage = null) }
            val draft = kakaoAuthDraftStore.get()
            when (
                val result = repository.linkKakaoAccount(
                    linkToken = draft.linkToken,
                    email = draft.linkEmail,
                    password = currentState.password,
                    agreedToLink = true,
                    deviceId = deviceInfoProvider.deviceId,
                    deviceName = deviceInfoProvider.deviceName,
                )
            ) {
                is AuthResult.Success -> {
                    kakaoAuthDraftStore.clear()
                    updateState { copy(isSubmitting = false) }
                    sendEffect(KakaoAccountLinkEffect.NavigateToMain)
                }

                is AuthResult.Failure -> updateState {
                    copy(
                        isSubmitting = false,
                        passwordErrorMessage = result.message,
                    )
                }
            }
        }
    }
}
