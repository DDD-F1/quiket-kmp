package com.f1.quiket.composeapp.login

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignupDraftTest {
    @Test
    fun requiredTermsNeedServiceAndPrivacyConsent() {
        val draft = SignupDraft(
            serviceTermsAgreed = true,
            privacyTermsAgreed = true,
        )

        assertTrue(draft.requiredTermsAgreed)
    }

    @Test
    fun marketingConsentDoesNotReplaceRequiredConsent() {
        val draft = SignupDraft(
            serviceTermsAgreed = true,
            privacyTermsAgreed = false,
            marketingTermsAgreed = true,
        )

        assertFalse(draft.requiredTermsAgreed)
    }
}
