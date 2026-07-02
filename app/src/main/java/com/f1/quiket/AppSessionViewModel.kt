package com.f1.quiket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.f1.quiket.core.session.SessionRepository
import com.f1.quiket.core.session.UserSessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class AppSessionState {
    Checking,
    SignedIn,
    SignedOut,
}

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    val sessionState: StateFlow<AppSessionState> = sessionRepository.observeSessionStatus()
        .map { status ->
            when (status) {
                UserSessionStatus.SignedIn -> AppSessionState.SignedIn
                UserSessionStatus.SignedOut -> AppSessionState.SignedOut
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSessionState.Checking,
        )

    suspend fun hasValidSession(): Boolean = sessionRepository.hasValidSession()

    suspend fun logout() = sessionRepository.logout()
}
