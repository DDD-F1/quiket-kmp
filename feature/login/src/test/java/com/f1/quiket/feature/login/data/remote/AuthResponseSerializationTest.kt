package com.f1.quiket.feature.login.data.remote

import com.f1.quiket.core.network.model.ApiResponse
import com.f1.quiket.feature.login.data.mapper.toDomain
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.junit.Test

class AuthResponseSerializationTest {
    @Test
    fun authTokenResponse_deserializes() {
        val response = decode(
            """
            {
              "success": true,
              "code": "AUTH_LOGIN_SUCCESS",
              "message": "Login complete.",
              "data": {
                "accessToken": "access-token",
                "refreshToken": "refresh-token",
                "tokenType": "Bearer",
                "accessTokenExpiresIn": 3600,
                "refreshTokenExpiresIn": 1209600,
                "user": {
                  "id": "018f8c2e-5f73-7b6a-b9f0-3f55e7f7c901",
                  "email": "user@example.com",
                  "nickname": "tester",
                  "dotoriBalance": 0,
                  "emailVerified": true,
                  "status": "active",
                  "providers": ["local", "kakao"]
                }
              }
            }
            """.trimIndent(),
            AuthTokenDataResponse.serializer(),
        )

        assertThat(response.data?.accessToken).isEqualTo("access-token")
        assertThat(response.data?.user?.providers).containsExactly("local", "kakao").inOrder()
    }

    @Test
    fun authTokenResponse_withoutEmail_deserializesForKakaoOnlyUser() {
        val response = decode(
            """
            {
              "success": true,
              "code": "AUTH_LOGIN_SUCCESS",
              "message": "Kakao login complete.",
              "data": {
                "accessToken": "access-token",
                "refreshToken": "refresh-token",
                "tokenType": "Bearer",
                "accessTokenExpiresIn": 3600,
                "refreshTokenExpiresIn": 1209600,
                "user": {
                  "id": "018f8c2e-5f73-7b6a-b9f0-3f55e7f7c901",
                  "nickname": "tester",
                  "dotoriBalance": 0,
                  "status": "active",
                  "providers": ["kakao"]
                }
              }
            }
            """.trimIndent(),
            AuthTokenDataResponse.serializer(),
        )

        val user = response.data?.toDomain()?.user

        assertThat(user?.email).isEmpty()
        assertThat(user?.emailVerified).isFalse()
        assertThat(user?.providers?.map { it.name }).containsExactly("Kakao")
    }

    @Test
    fun signupResponse_deserializes() {
        val response = decode(
            """
            {
              "success": true,
              "code": "AUTH_SIGNUP_SUCCESS",
              "message": "Signup complete.",
              "data": {
                "userId": "018f8c2e-5f73-7b6a-b9f0-3f55e7f7c901",
                "email": "user@example.com",
                "nickname": "tester",
                "emailVerificationRequired": true,
                "emailVerificationSent": true
              }
            }
            """.trimIndent(),
            SignupDataResponse.serializer(),
        )

        assertThat(response.data?.email).isEqualTo("user@example.com")
        assertThat(response.data?.emailVerificationRequired).isTrue()
    }

    @Test
    fun errorResponse_deserializesFieldErrors() {
        val response = decode(
            """
            {
              "success": false,
              "code": "COMMON_VALIDATION_ERROR",
              "message": "Invalid request.",
              "data": {
                "fieldErrors": [
                  {
                    "field": "email",
                    "reason": "Invalid email"
                  }
                ]
              }
            }
            """.trimIndent(),
            ErrorDataResponse.serializer(),
        )

        assertThat(response.data?.fieldErrors?.first()?.field).isEqualTo("email")
        assertThat(response.data?.fieldErrors?.first()?.reason).isEqualTo("Invalid email")
    }

    @Test
    fun kakaoNicknameRequiredResponse_deserializes() {
        val response = decode(
            """
            {
              "success": true,
              "code": "AUTH_NICKNAME_REQUIRED",
              "message": "Nickname required.",
              "data": {
                "signupToken": "signup-token",
                "provider": "kakao",
                "suggestedNickname": "kakao-user"
              }
            }
            """.trimIndent(),
            KakaoNicknameRequiredDataResponse.serializer(),
        )

        assertThat(response.data?.signupToken).isEqualTo("signup-token")
        assertThat(response.data?.suggestedNickname).isEqualTo("kakao-user")
    }

    @Test
    fun kakaoAccountLinkRequiredResponse_deserializes() {
        val response = decode(
            """
            {
              "success": false,
              "code": "AUTH_OAUTH_ACCOUNT_LINK_REQUIRED",
              "message": "Link required.",
              "data": {
                "email": "user@example.com",
                "provider": "kakao",
                "linkToken": "link-token",
                "expiresInSeconds": 600
              }
            }
            """.trimIndent(),
            KakaoAccountLinkRequiredDataResponse.serializer(),
        )

        assertThat(response.data?.email).isEqualTo("user@example.com")
        assertThat(response.data?.linkToken).isEqualTo("link-token")
    }

    private fun <T> decode(
        body: String,
        serializer: KSerializer<T>,
    ): ApiResponse<T> = json.decodeFromString(
        ApiResponse.serializer(serializer),
        body,
    )

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }
    }
}
