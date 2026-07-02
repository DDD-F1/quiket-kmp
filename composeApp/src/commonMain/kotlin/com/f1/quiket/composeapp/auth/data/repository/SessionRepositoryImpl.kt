package com.f1.quiket.composeapp.auth.data.repository

import com.f1.quiket.composeapp.auth.AuthTokenData
import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.auth.SessionStore
import com.f1.quiket.composeapp.auth.domain.repository.SessionRepository

internal class SessionRepositoryImpl : SessionRepository {
    override suspend fun read(): SessionSnapshot =
        SessionStore.read()

    override suspend fun saveOnboardingCompleted() {
        SessionStore.saveOnboardingCompleted()
    }

    override suspend fun saveHomeGuideCompleted() {
        SessionStore.saveHomeGuideCompleted()
    }

    override suspend fun saveAuth(tokenData: AuthTokenData) {
        SessionStore.saveAuth(tokenData)
    }

    override suspend fun clearAuth() {
        SessionStore.clearAuth()
    }
}
