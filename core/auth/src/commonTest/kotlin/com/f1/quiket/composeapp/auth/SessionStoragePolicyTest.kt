package com.f1.quiket.composeapp.auth

import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SessionStoragePolicyTest {
    @Test
    fun `resolveLegacyAuthState returns empty when no legacy auth is present`() {
        val state = resolveLegacyAuthState(
            accessToken = null,
            refreshToken = null,
            tokenType = null,
            nickname = null,
            accessTokenExpiresIn = null,
            refreshTokenExpiresIn = null,
        )

        assertEquals(LegacyAuthState.Empty, state)
    }

    @Test
    fun `resolveLegacyAuthState returns migratable payload when legacy auth is complete`() {
        val state = resolveLegacyAuthState(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            tokenType = "Bearer",
            nickname = "quiket",
            accessTokenExpiresIn = 3600L,
            refreshTokenExpiresIn = 7200L,
        )

        val migratable = assertIs<LegacyAuthState.Migratable>(state)
        assertEquals(
            PersistedAuthPayload(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                tokenType = "Bearer",
                nickname = "quiket",
                accessTokenExpiresIn = 3600L,
                refreshTokenExpiresIn = 7200L,
            ),
            migratable.payload,
        )
    }

    @Test
    fun `resolveLegacyAuthState returns invalid when token metadata is partial`() {
        val state = resolveLegacyAuthState(
            accessToken = "access-token",
            refreshToken = null,
            tokenType = "Bearer",
            nickname = "quiket",
            accessTokenExpiresIn = 3600L,
            refreshTokenExpiresIn = null,
        )

        assertEquals(LegacyAuthState.Invalid, state)
    }

    @Test
    fun `resolveLegacyAuthState trims blanks and treats blank auth values as invalid`() {
        val state = resolveLegacyAuthState(
            accessToken = "   ",
            refreshToken = "refresh-token",
            tokenType = "Bearer",
            nickname = "  nickname  ",
            accessTokenExpiresIn = 3600L,
            refreshTokenExpiresIn = 7200L,
        )

        assertEquals(LegacyAuthState.Invalid, state)
    }

    @Test
    fun `resolveLegacyAuthState defaults missing expiries to zero during migration`() {
        val state = resolveLegacyAuthState(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            tokenType = "Bearer",
            nickname = null,
            accessTokenExpiresIn = null,
            refreshTokenExpiresIn = null,
        )

        val migratable = assertIs<LegacyAuthState.Migratable>(state)
        assertEquals(0L, migratable.payload.accessTokenExpiresIn)
        assertEquals(0L, migratable.payload.refreshTokenExpiresIn)
    }

    @Test
    fun `toPersistedAuthPayload preserves stored nickname when token response omits user`() {
        val payload = AuthTokenData(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            tokenType = "Bearer",
            accessTokenExpiresIn = 3600L,
            refreshTokenExpiresIn = 7200L,
            user = null,
        ).toPersistedAuthPayload(existingNickname = "saved-nickname")

        assertEquals("saved-nickname", payload.nickname)
    }
}
