package com.f1.quiket.core.session

import kotlinx.coroutines.flow.Flow

enum class UserSessionStatus {
    SignedIn,
    SignedOut,
}

interface SessionRepository {
    fun observeSessionStatus(): Flow<UserSessionStatus>

    suspend fun hasValidSession(): Boolean

    suspend fun logout()
}
