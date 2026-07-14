package com.f1.quiket.composeapp.login

data class SignupDraft(
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val nickname: String = "",
    val serviceTermsAgreed: Boolean = false,
    val privacyTermsAgreed: Boolean = false,
    val marketingTermsAgreed: Boolean = false,
) {
    val requiredTermsAgreed: Boolean
        get() = serviceTermsAgreed && privacyTermsAgreed
}
