package com.f1.quiket.composeapp.login

import android.app.Activity
import com.f1.quiket.composeapp.BuildConfig
import com.f1.quiket.composeapp.KakaoLoginCompletion
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class AndroidKakaoLoginLauncher(
    private val activity: Activity,
) {
    fun launch(completion: KakaoLoginCompletion) {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            completion(null, "Kakao Native App Key가 설정되지 않았습니다.")
            return
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                token?.accessToken
                    ?.takeIf(String::isNotBlank)
                    ?.let { completion(it, null) }
                    ?: when {
                        error.isLoginCancellation() -> completion(null, "카카오 로그인이 취소되었습니다.")
                        else -> loginWithKakaoAccount(completion)
                    }
            }
        } else {
            loginWithKakaoAccount(completion)
        }
    }

    private fun loginWithKakaoAccount(completion: KakaoLoginCompletion) {
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            val accessToken = token?.accessToken
            if (!accessToken.isNullOrBlank()) {
                completion(accessToken, null)
            } else {
                completion(
                    null,
                    if (error.isLoginCancellation()) {
                        "카카오 로그인이 취소되었습니다."
                    } else {
                        error?.localizedMessage ?: "카카오 로그인을 완료하지 못했습니다. 다시 시도해 주세요."
                    },
                )
            }
        }
    }

    private fun Throwable?.isLoginCancellation(): Boolean =
        this is ClientError && reason == ClientErrorCause.Cancelled
}
