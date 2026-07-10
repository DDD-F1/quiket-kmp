package com.f1.quiket.composeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.f1.quiket.composeapp.di.initKoin

fun MainViewController() = MainViewController(
    kakaoLoginLauncher = { completion ->
        completion(null, "카카오 로그인을 사용할 수 없습니다.")
    },
    appleLoginLauncher = { completion ->
        completion(null, null, null, "Apple 로그인을 사용할 수 없습니다.")
    },
)

fun MainViewController(
    kakaoLoginLauncher: KakaoLoginLauncher,
    appleLoginLauncher: AppleLoginLauncher,
) = run {
    initKoin()
    ComposeUIViewController {
        QuiketApp(
            kakaoLoginLauncher = kakaoLoginLauncher,
            appleLoginLauncher = appleLoginLauncher,
            isAppleLoginAvailable = true,
        )
    }
}
