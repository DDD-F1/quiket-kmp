package com.f1.quiket.composeapp.auth.domain.repository

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.SessionSnapshot

interface SessionRepository {
    suspend fun read(): SessionSnapshot
    suspend fun saveOnboardingCompleted()
    suspend fun saveHomeGuideCompleted()
    suspend fun saveAuth(tokenData: AuthTokenData)
    suspend fun clearAuth()
}
