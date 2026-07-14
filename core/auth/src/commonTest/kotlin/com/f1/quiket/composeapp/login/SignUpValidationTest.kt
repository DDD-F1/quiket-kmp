package com.f1.quiket.composeapp.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignUpValidationTest {
    @Test
    fun emailValidationTrimsInputAndRejectsMalformedAddresses() {
        assertTrue(isValidEmail("  user+qa@quiket.co.kr  "))
        assertFalse(isValidEmail("user@quiket"))
        assertFalse(isValidEmail("user quiket@example.com"))
    }

    @Test
    fun passwordRequiresLengthLetterAndNumber() {
        assertTrue(isValidAuthPassword("quiket12"))
        assertTrue(isValidAuthPassword("Quiket12!"))
        assertFalse(isValidAuthPassword("short1"))
        assertFalse(isValidAuthPassword("onlyletters"))
        assertFalse(isValidAuthPassword("12345678"))
        assertFalse(isValidAuthPassword("quiket 12"))
        assertFalse(isValidAuthPassword("quiket12/"))
    }

    @Test
    fun nicknameAllowsOnlyKoreanOrEnglishWithinLengthBoundary() {
        assertTrue(isValidNickname("퀴켓"))
        assertTrue(isValidNickname("Quiket"))
        assertFalse(isValidNickname("Q"))
        assertFalse(isValidNickname("퀴켓1"))
        assertFalse(isValidNickname("abcdefghijklmn"))
    }

    @Test
    fun timerTextUsesMinuteSecondFormat() {
        assertEquals("00:00", 0.toTimerText())
        assertEquals("01:05", 65.toTimerText())
        assertEquals("10:00", VerificationTimeoutSeconds.toTimerText())
    }
}
