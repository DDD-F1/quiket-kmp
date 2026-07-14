package com.f1.quiket.composeapp.login

const val EmailFormatErrorMessage = "올바른 이메일 형식으로 입력해주세요"
const val PasswordPolicyErrorMessage = "8-32자의 영문/숫자 조합으로 입력해주세요"
const val PasswordConfirmMismatchMessage = "비밀번호가 일치하지 않아요"
const val NicknameFormatErrorMessage = "2-12자의 영문/한글로 입력해주세요"
const val VerificationCodeMismatchErrorMessage = "인증번호가 일치하지 않아요"
const val VerificationExpiredErrorMessage = "인증 시간이 만료되었어요"
const val VerificationTimeoutSeconds = 600

private val EmailRegex = Regex(
    pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
)

private val AllowedPasswordSpecialCharacters = setOf(
    '~',
    '!',
    '@',
    '#',
    '$',
    '%',
    '^',
    '&',
    '*',
    '(',
    ')',
    '_',
    '-',
    '+',
    '=',
    '[',
    ']',
    '{',
    '}',
    '.',
    ',',
    '?',
)

private val NicknameRegex = Regex("^[A-Za-z가-힣]{2,12}$")

fun isValidEmail(email: String): Boolean =
    email.trim().matches(EmailRegex)

fun isValidAuthPassword(password: String): Boolean {
    if (password.length !in 8..32) return false
    if (password.any { it.isWhitespace() || it.code > 127 }) return false
    if (password.any { !it.isLetterOrDigit() && it !in AllowedPasswordSpecialCharacters }) return false

    return password.any(Char::isLetter) && password.any(Char::isDigit)
}

fun isValidNickname(nickname: String): Boolean =
    nickname.matches(NicknameRegex)

fun Int.toTimerText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "${minutes.toTwoDigits()}:${seconds.toTwoDigits()}"
}

private fun Int.toTwoDigits(): String =
    if (this < 10) "0$this" else toString()
