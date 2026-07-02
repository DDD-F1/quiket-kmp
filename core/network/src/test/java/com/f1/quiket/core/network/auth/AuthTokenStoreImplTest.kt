package com.f1.quiket.core.network.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AuthTokenStoreImplTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var store: AuthTokenStoreImpl

    @Before
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            File(temporaryFolder.root, "auth_tokens.preferences_pb")
        }

        store = AuthTokenStoreImpl(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun saveTokenPair_persistsTokenPairForGetTokenPair() = runTest {
        val expected = tokenPair()

        store.saveTokenPair(expected)

        assertThat(store.getTokenPair()).isEqualTo(expected)
    }

    @Test
    fun observeTokenPair_emitsInitialSavedAndClearedValues() = runTest {
        val expected = tokenPair()

        store.observeTokenPair().test {
            assertThat(awaitItem()).isNull()

            store.saveTokenPair(expected)
            assertThat(awaitItem()).isEqualTo(expected)

            store.clear()
            assertThat(awaitItem()).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clear_removesPersistedTokenPair() = runTest {
        store.saveTokenPair(tokenPair())

        store.clear()

        assertThat(store.getTokenPair()).isNull()
    }

    private fun tokenPair() = TokenPair(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        accessTokenExpiresIn = 3600,
        refreshTokenExpiresIn = 1_209_600,
    )
}
