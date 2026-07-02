package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.data.kakao.KakaoLoginClient
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.model.KakaoLoginResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val kakaoLoginClient: KakaoLoginClient,
    private val repository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val kakaoAuthDraftStore: KakaoAuthDraftStore,
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(
    initialState = LoginState(),
) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.KakaoLoginClick -> loginWithKakao(intent)
        }
    }

    private fun loginWithKakao(intent: LoginIntent.KakaoLoginClick) {
        if (currentState.isKakaoLoading) return

        launch {
            updateState { copy(isKakaoLoading = true) }

            when (val tokenResult = kakaoLoginClient.login(intent.context)) {
                is AuthResult.Success -> handleServerKakaoLogin(tokenResult.data)
                is AuthResult.Failure -> {
                    updateState { copy(isKakaoLoading = false) }
                    sendEffect(LoginEffect.ShowMessage(tokenResult.message))
                }
            }
        }
    }

    private suspend fun handleServerKakaoLogin(kakaoAccessToken: String) {
        when (
            val result = repository.kakaoLogin(
                kakaoAccessToken = kakaoAccessToken,
                agreedToTerms = true,
                deviceId = deviceInfoProvider.deviceId,
                deviceName = deviceInfoProvider.deviceName,
            )
        ) {
            is KakaoLoginResult.LoggedIn -> {
                kakaoAuthDraftStore.clear()
                updateState { copy(isKakaoLoading = false) }
                sendEffect(LoginEffect.NavigateToMain)
            }

            is KakaoLoginResult.NicknameRequired -> {
                kakaoAuthDraftStore.update {
                    copy(
                        signupToken = result.data.signupToken,
                        suggestedNickname = result.data.suggestedNickname,
                    )
                }
                updateState { copy(isKakaoLoading = false) }
                sendEffect(LoginEffect.NavigateToKakaoNickname)
            }

            is KakaoLoginResult.AccountLinkRequired -> {
                kakaoAuthDraftStore.update {
                    copy(
                        linkToken = result.data.linkToken,
                        linkEmail = result.data.email,
                    )
                }
                updateState { copy(isKakaoLoading = false) }
                sendEffect(LoginEffect.NavigateToKakaoAccountLink)
            }

            is KakaoLoginResult.Failure -> {
                updateState { copy(isKakaoLoading = false) }
                sendEffect(LoginEffect.ShowMessage(result.failure.message))
            }
        }
    }
}
