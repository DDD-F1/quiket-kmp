package com.f1.quiket.feature.login.data.kakao

import android.content.Context
import com.f1.quiket.feature.login.BuildConfig
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class KakaoLoginClientImpl @Inject constructor() : KakaoLoginClient {
    override suspend fun login(context: Context): AuthResult<String> {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return AuthResult.Failure(
                code = "KAKAO_KEY_MISSING",
                message = "Kakao Native App Key가 설정되지 않았습니다.",
            )
        }

        if (!UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            return loginWithKakaoAccount(context)
        }

        return when (val talkResult = loginWithKakaoTalk(context)) {
            is AuthResult.Success -> talkResult
            is AuthResult.Failure -> {
                if (talkResult.cause is ClientError &&
                    talkResult.cause.reason == ClientErrorCause.Cancelled
                ) {
                    talkResult
                } else {
                    loginWithKakaoAccount(context)
                }
            }
        }
    }

    private suspend fun loginWithKakaoTalk(context: Context): AuthResult<String> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (continuation.isActive) {
                    continuation.resume(token.toAuthResult(error))
                }
            }
        }

    private suspend fun loginWithKakaoAccount(context: Context): AuthResult<String> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (continuation.isActive) {
                    continuation.resume(token.toAuthResult(error))
                }
            }
        }

    private fun OAuthToken?.toAuthResult(error: Throwable?): AuthResult<String> {
        val accessToken = this?.accessToken
        if (!accessToken.isNullOrBlank()) {
            return AuthResult.Success(accessToken)
        }

        return AuthResult.Failure(
            code = "KAKAO_LOGIN_FAILED",
            message = error?.message ?: "Kakao 로그인에 실패했습니다.",
            cause = error,
        )
    }
}
