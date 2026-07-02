package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupNicknameViewModel @Inject constructor(
    private val signupDraftStore: SignupDraftStore,
) : MviViewModel<SignupNicknameState, SignupNicknameIntent, SignupNicknameEffect>(
    initialState = SignupNicknameState(
        nickname = signupDraftStore.get().nickname,
    ),
) {
    override fun handleIntent(intent: SignupNicknameIntent) {
        when (intent) {
            is SignupNicknameIntent.NicknameChanged -> updateState {
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

            SignupNicknameIntent.Next -> next()
        }
    }

    private fun next() {
        val nickname = currentState.nickname.trim()
        if (!isValidNickname(nickname)) {
            updateState { copy(nicknameErrorMessage = NicknameFormatErrorMessage) }
            return
        }
        signupDraftStore.update { copy(nickname = nickname) }
        launch { sendEffect(SignupNicknameEffect.NavigateNext) }
    }
}

internal const val SignUpNicknameMaxLength = 12
