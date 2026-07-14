package com.f1.quiket.composeapp.auth.data.remote

import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.network.apiJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthClientKakaoLoginTest {
    @Test
    fun kakaoLoginReturnsTokensForExistingOrNewUser() = runTest {
        val client = clientFor(
            status = HttpStatusCode.Created,
            body = """
                {
                  "success": true,
                  "code": "AUTH_KAKAO_SIGNUP_SUCCESS",
                  "message": "카카오 회원가입 및 로그인이 완료되었습니다.",
                  "data": {
                    "accessToken": "access-token",
                    "refreshToken": "refresh-token",
                    "tokenType": "Bearer",
                    "accessTokenExpiresIn": 1800,
                    "refreshTokenExpiresIn": 2592000,
                    "user": {
                      "id": "user-1",
                      "email": "user@example.com",
                      "nickname": "도토리장인"
                    }
                  }
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val loggedIn = assertIs<KakaoLoginResult.LoggedIn>(result)
        assertEquals("access-token", loggedIn.tokenData.accessToken)
        assertEquals("도토리장인", loggedIn.tokenData.user?.nickname)
    }

    @Test
    fun kakaoLoginReturnsNicknameSetupForAcceptedResponse() = runTest {
        val client = clientFor(
            status = HttpStatusCode.Accepted,
            body = """
                {
                  "success": true,
                  "code": "AUTH_NICKNAME_REQUIRED",
                  "message": "닉네임 설정이 필요합니다.",
                  "data": {
                    "signupToken": "signup-token",
                    "provider": "kakao",
                    "suggestedNickname": "도토리"
                  }
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val nicknameRequired = assertIs<KakaoLoginResult.NicknameRequired>(result)
        assertEquals("signup-token", nicknameRequired.data.signupToken)
        assertEquals("도토리", nicknameRequired.data.suggestedNickname)
    }

    @Test
    fun kakaoLoginReturnsAccountLinkForConflictResponse() = runTest {
        val client = clientFor(
            status = HttpStatusCode.Conflict,
            body = """
                {
                  "success": false,
                  "code": "AUTH_OAUTH_ACCOUNT_LINK_REQUIRED",
                  "message": "동일 이메일로 가입된 계정이 있습니다. 계정 연동이 필요합니다.",
                  "data": {
                    "email": "user@example.com",
                    "provider": "kakao",
                    "linkToken": "link-token",
                    "expiresInSeconds": 600
                  }
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val linkRequired = assertIs<KakaoLoginResult.AccountLinkRequired>(result)
        assertEquals("user@example.com", linkRequired.data.email)
        assertEquals("link-token", linkRequired.data.linkToken)
    }

    @Test
    fun kakaoLoginReturnsFailureForServerError() = runTest {
        val client = clientFor(
            status = HttpStatusCode.BadRequest,
            body = """
                {
                  "success": false,
                  "code": "AUTH_KAKAO_LOGIN_FAILED",
                  "message": "카카오 로그인에 실패했습니다."
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val failure = assertIs<KakaoLoginResult.Failure>(result)
        assertEquals("카카오 로그인에 실패했습니다.", failure.message)
    }

    @Test
    fun kakaoLoginReturnsFailureForEmptyResponseData() = runTest {
        val client = clientFor(
            status = HttpStatusCode.OK,
            body = """
                {
                  "success": true,
                  "code": "AUTH_KAKAO_SIGNUP_SUCCESS",
                  "message": "카카오 회원가입 및 로그인이 완료되었습니다."
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val failure = assertIs<KakaoLoginResult.Failure>(result)
        assertEquals("카카오 로그인 응답 데이터가 비어 있습니다.", failure.message)
    }

    @Test
    fun kakaoLoginReturnsFailureForMalformedData() = runTest {
        val client = clientFor(
            status = HttpStatusCode.OK,
            body = """
                {
                  "success": true,
                  "code": "AUTH_KAKAO_SIGNUP_SUCCESS",
                  "message": "카카오 회원가입 및 로그인이 완료되었습니다.",
                  "data": {}
                }
            """.trimIndent(),
        )

        val result = client.kakaoLogin(kakaoAccessToken = "kakao-access-token")

        val failure = assertIs<KakaoLoginResult.Failure>(result)
        assertEquals("카카오 로그인 응답을 해석하지 못했습니다.", failure.message)
    }

    private fun clientFor(
        status: HttpStatusCode,
        body: String,
    ): AuthClient = AuthClient(
        httpClient = HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        ) {
            install(ContentNegotiation) {
                json(apiJson)
            }
        },
        json = apiJson,
        baseUrl = "https://api.quiket.test/api/v1",
    )
}
