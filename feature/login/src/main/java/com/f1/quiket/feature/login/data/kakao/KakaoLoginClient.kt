package com.f1.quiket.feature.login.data.kakao

import android.content.Context
import com.f1.quiket.feature.login.domain.model.AuthResult

interface KakaoLoginClient {
    suspend fun login(context: Context): AuthResult<String>
}
