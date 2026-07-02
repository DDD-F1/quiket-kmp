package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.core.network.auth.DeviceInfoProvider
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KakaoNicknameViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val kakaoAuthDraftStore: KakaoAuthDraftStore,
) : MviViewModel<KakaoNicknameState, KakaoNicknameIntent, KakaoNicknameEffect>(
    initialState = KakaoNicknameState(
        nickname = kakaoAuthDraftStore.get().suggestedNickname.orEmpty().take(SignUpNicknameMaxLength),
    ),
) {
    override fun handleIntent(intent: KakaoNicknameIntent) {
        when (intent) {
            is KakaoNicknameIntent.NicknameChanged -> updateState {
                val next = intent.value.take(SignUpNicknameMaxLength)
                copy(
                    nickname = next,
                    nicknameErrorMessage = if (next.isNotBlank() && !isValidNickname(next.trim())) {
                        NicknameFormatErrorMessage
                    } else {
                        null
                    },
                )
            }

            KakaoNicknameIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val nickname = currentState.nickname.trim()
        if (!isValidNickname(nickname)) {
            updateState { copy(nicknameErrorMessage = NicknameFormatErrorMessage) }
            return
        }

        launch {
            updateState { copy(isSubmitting = true) }
            when (
                val result = repository.completeKakaoNickname(
                    signupToken = kakaoAuthDraftStore.get().signupToken,
                    nickname = nickname,
                    deviceId = deviceInfoProvider.deviceId,
                    deviceName = deviceInfoProvider.deviceName,
                )
            ) {
                is AuthResult.Success -> {
                    kakaoAuthDraftStore.clear()
                    updateState { copy(isSubmitting = false) }
                    sendEffect(KakaoNicknameEffect.NavigateToMain)
                }

                is AuthResult.Failure -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(KakaoNicknameEffect.ShowMessage(result.message))
                }
            }
        }
    }
}
