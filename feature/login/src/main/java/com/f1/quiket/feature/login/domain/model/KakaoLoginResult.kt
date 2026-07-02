package com.f1.quiket.feature.login.domain.model

sealed interface KakaoLoginResult {
    data class LoggedIn(
        val authTokenData: AuthTokenData,
    ) : KakaoLoginResult

    data class NicknameRequired(
        val data: KakaoNicknameRequired,
    ) : KakaoLoginResult

    data class AccountLinkRequired(
        val data: KakaoAccountLinkRequired,
    ) : KakaoLoginResult

    data class Failure(
        val failure: AuthResult.Failure,
    ) : KakaoLoginResult
}
