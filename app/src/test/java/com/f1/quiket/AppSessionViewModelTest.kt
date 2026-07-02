package com.f1.quiket

import com.f1.quiket.core.session.SessionRepository
import com.f1.quiket.core.session.UserSessionStatus
import com.f1.quiket.core.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppSessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sessionState_whenSessionRepositoryEmitsSignedOut_mapsSignedOut() = runTest {
        val viewModel = AppSessionViewModel(
            sessionRepository = FakeSessionRepository(),
        )
        val states = mutableListOf<AppSessionState>()

        backgroundScope.launch {
            viewModel.sessionState.toList(states)
        }
        runCurrent()

        assertThat(states).containsExactly(
            AppSessionState.Checking,
            AppSessionState.SignedOut,
        ).inOrder()
    }

    @Test
    fun sessionState_whenSessionRepositoryChanges_mapsLatestState() = runTest {
        val sessionRepository = FakeSessionRepository(UserSessionStatus.SignedIn)
        val viewModel = AppSessionViewModel(sessionRepository)
        val states = mutableListOf<AppSessionState>()

        backgroundScope.launch {
            viewModel.sessionState.toList(states)
        }
        runCurrent()
        sessionRepository.emit(UserSessionStatus.SignedOut)
        runCurrent()

        assertThat(states).containsExactly(
            AppSessionState.Checking,
            AppSessionState.SignedIn,
            AppSessionState.SignedOut,
        ).inOrder()
    }

    @Test
    fun hasValidSession_delegatesToSessionRepository() = runTest {
        val sessionRepository = FakeSessionRepository(hasValidSessionResult = true)
        val viewModel = AppSessionViewModel(sessionRepository)

        val result = viewModel.hasValidSession()

        assertThat(result).isTrue()
        assertThat(sessionRepository.hasValidSessionCallCount).isEqualTo(1)
    }

    @Test
    fun logout_delegatesToSessionRepository() = runTest {
        val sessionRepository = FakeSessionRepository()
        val viewModel = AppSessionViewModel(sessionRepository)

        viewModel.logout()

        assertThat(sessionRepository.logoutCallCount).isEqualTo(1)
    }

    private class FakeSessionRepository(
        initialStatus: UserSessionStatus = UserSessionStatus.SignedOut,
        private val hasValidSessionResult: Boolean = false,
    ) : SessionRepository {
        private val status = MutableStateFlow(initialStatus)
        var hasValidSessionCallCount = 0
            private set
        var logoutCallCount = 0
            private set

        override fun observeSessionStatus(): Flow<UserSessionStatus> = status

        override suspend fun hasValidSession(): Boolean {
            hasValidSessionCallCount += 1
            return hasValidSessionResult
        }

        override suspend fun logout() {
            logoutCallCount += 1
        }

        fun emit(nextStatus: UserSessionStatus) {
            status.value = nextStatus
        }
    }
}
