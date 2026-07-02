package com.f1.quiket.feature.login.data.session

import com.f1.quiket.core.network.auth.AuthTokenStore
import com.f1.quiket.core.session.SessionRepository
import com.f1.quiket.core.session.UserSessionStatus
import com.f1.quiket.feature.login.domain.model.AuthResult
import com.f1.quiket.feature.login.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AuthSessionRepository @Inject constructor(
    private val authTokenStore: AuthTokenStore,
    private val authRepository: AuthRepository,
) : SessionRepository {
    override fun observeSessionStatus(): Flow<UserSessionStatus> =
        authTokenStore.observeTokenPair()
            .map { tokenPair ->
                if (tokenPair == null) UserSessionStatus.SignedOut else UserSessionStatus.SignedIn
            }

    override suspend fun hasValidSession(): Boolean {
        authTokenStore.getTokenPair() ?: return false

        return when (val result = authRepository.getMe()) {
            is AuthResult.Success -> true
            is AuthResult.Failure -> {
                if (result.isUnauthorized()) {
                    authTokenStore.clear()
                }
                false
            }
        }
    }

    override suspend fun logout() {
        val result = authRepository.logout()
        if (result is AuthResult.Failure) {
            authTokenStore.clear()
        }
    }

    private fun AuthResult.Failure.isUnauthorized(): Boolean =
        httpCode == HTTP_UNAUTHORIZED || code.contains("UNAUTHORIZED", ignoreCase = true)

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
