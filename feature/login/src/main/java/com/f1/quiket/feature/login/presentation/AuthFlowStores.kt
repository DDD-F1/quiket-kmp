package com.f1.quiket.feature.login.presentation

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupDraftStore @Inject constructor() {
    private var draft = SignupDraft()

    fun get(): SignupDraft = draft

    fun update(reducer: SignupDraft.() -> SignupDraft) {
        draft = draft.reducer()
    }

    fun clear() {
        draft = SignupDraft()
    }
}

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

@Singleton
class PasswordResetDraftStore @Inject constructor() {
    private var draft = PasswordResetDraft()

    fun get(): PasswordResetDraft = draft

    fun update(reducer: PasswordResetDraft.() -> PasswordResetDraft) {
        draft = draft.reducer()
    }

    fun clear() {
        draft = PasswordResetDraft()
    }
}

data class PasswordResetDraft(
    val email: String = "",
    val verificationCode: String = "",
    val resetCodeSent: Boolean = false,
)

@Singleton
class KakaoAuthDraftStore @Inject constructor() {
    private var draft = KakaoAuthDraft()

    fun get(): KakaoAuthDraft = draft

    fun update(reducer: KakaoAuthDraft.() -> KakaoAuthDraft) {
        draft = draft.reducer()
    }

    fun clear() {
        draft = KakaoAuthDraft()
    }
}

data class KakaoAuthDraft(
    val signupToken: String = "",
    val suggestedNickname: String? = null,
    val linkToken: String = "",
    val linkEmail: String = "",
)
