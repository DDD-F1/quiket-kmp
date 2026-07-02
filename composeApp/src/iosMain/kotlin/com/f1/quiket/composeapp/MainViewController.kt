package com.f1.quiket.composeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.f1.quiket.composeapp.di.initKoin

fun MainViewController() = MainViewController { completion ->
    completion(null, "카카오 로그인을 사용할 수 없습니다.")
}

fun MainViewController(
    kakaoLoginLauncher: KakaoLoginLauncher,
) = run {
    initKoin()
    ComposeUIViewController {
        QuiketApp(kakaoLoginLauncher = kakaoLoginLauncher)
    }
}
