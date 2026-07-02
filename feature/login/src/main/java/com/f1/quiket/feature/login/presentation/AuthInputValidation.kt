package com.f1.quiket.feature.login.presentation

internal const val PasswordPolicyErrorMessage = "8-32자의 영문/숫자 조합으로 입력해주세요"
internal const val PasswordConfirmMismatchMessage = "비밀번호가 일치하지 않아요"
internal const val NicknameFormatErrorMessage = "2-12자의 영문/한글로 입력해주세요"
internal const val VerificationCodeMismatchErrorMessage = "인증번호가 일치하지 않아요"
internal const val VerificationExpiredErrorMessage = "인증 시간이 만료되었어요"
internal const val VerificationTimeoutSeconds = 600

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

internal fun isValidAuthPassword(password: String): Boolean {
    if (password.length !in 8..32) return false
    if (password.any { it.isWhitespace() || it.code > 127 }) return false
    if (password.any { !it.isLetterOrDigit() && it !in AllowedPasswordSpecialCharacters }) return false

    return password.any(Char::isLetter) && password.any(Char::isDigit)
}

internal fun isValidNickname(nickname: String): Boolean =
    nickname.matches(NicknameRegex)

internal fun Int.toTimerText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}
