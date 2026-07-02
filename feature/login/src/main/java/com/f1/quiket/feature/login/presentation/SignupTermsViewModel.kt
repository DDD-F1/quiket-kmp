package com.f1.quiket.feature.login.presentation

import com.f1.quiket.core.common.mvi.MviViewModel
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupTermsViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val signupDraftStore: SignupDraftStore,
) : MviViewModel<SignupTermsUiState, SignupTermsIntent, SignupTermsEffect>(
    initialState = signupDraftStore.get().let { draft ->
        SignupTermsUiState(
            termsState = SignUpTermsState(
                serviceTermsAgreed = draft.serviceTermsAgreed,
                privacyTermsAgreed = draft.privacyTermsAgreed,
                marketingTermsAgreed = draft.marketingTermsAgreed,
            ),
        )
    },
) {
    override fun handleIntent(intent: SignupTermsIntent) {
        when (intent) {
            SignupTermsIntent.ToggleAllTerms -> updateTerms {
                val next = !allAgreed
                copy(
                    serviceTermsAgreed = next,
                    privacyTermsAgreed = next,
                    marketingTermsAgreed = next,
                )
            }

            SignupTermsIntent.ToggleServiceTerms -> updateTerms {
                copy(serviceTermsAgreed = !serviceTermsAgreed)
            }

            SignupTermsIntent.TogglePrivacyTerms -> updateTerms {
                copy(privacyTermsAgreed = !privacyTermsAgreed)
            }

            SignupTermsIntent.ToggleMarketingTerms -> updateTerms {
                copy(marketingTermsAgreed = !marketingTermsAgreed)
            }

            SignupTermsIntent.AgreeServiceTerms -> updateTerms {
                copy(serviceTermsAgreed = true)
            }

            SignupTermsIntent.AgreePrivacyTerms -> updateTerms {
                copy(privacyTermsAgreed = true)
            }

            SignupTermsIntent.Submit -> submit()
        }
    }

    private fun updateTerms(reducer: SignUpTermsState.() -> SignUpTermsState) {
        updateState {
            val nextTerms = termsState.reducer()
            signupDraftStore.update {
                copy(
                    serviceTermsAgreed = nextTerms.serviceTermsAgreed,
                    privacyTermsAgreed = nextTerms.privacyTermsAgreed,
                    marketingTermsAgreed = nextTerms.marketingTermsAgreed,
                )
            }
            copy(termsState = nextTerms)
        }
    }

    private fun submit() {
        val draft = signupDraftStore.get()
        if (!draft.requiredTermsAgreed) {
            launch { sendEffect(SignupTermsEffect.ShowMessage("필수 약관에 동의해주세요.")) }
            return
        }

        launch {
            updateState { copy(isSubmitting = true) }
            when (
                val result = repository.signup(
                    email = draft.email,
                    password = draft.password,
                    passwordConfirm = draft.passwordConfirm,
                    nickname = draft.nickname,
                )
            ) {
                is AuthResult.Success -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(SignupTermsEffect.NavigateToEmailVerification)
                }

                is AuthResult.Failure -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(SignupTermsEffect.ShowMessage(result.message))
                }
            }
        }
    }
}
