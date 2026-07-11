package com.f1.quiket.composeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.f1.quiket.composeapp.auth.AndroidSessionContext
import com.f1.quiket.composeapp.di.initKoin
import com.f1.quiket.composeapp.login.AndroidKakaoLoginLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSessionContext.init(applicationContext)
        initKoin()
        val kakaoLoginLauncher = AndroidKakaoLoginLauncher(this)
        setContent {
            QuiketApp(
                kakaoLoginLauncher = kakaoLoginLauncher::launch,
                isAppleLoginAvailable = false,
            )
        }
    }
}
