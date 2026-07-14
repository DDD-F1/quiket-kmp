package com.f1.quiket.composeapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiSupportContractTest {
    @Test
    fun decodesSuccessfulEnvelopeData() = runTest {
        val response = response(
            status = HttpStatusCode.OK,
            body = """{"success":true,"code":"OK","message":"","data":{"value":"ready"}}""",
        )

        val data = response.decodeApiData<TestData>(
            json = apiJson,
            missingMessage = "missing",
            failureMessage = "failed",
        )

        assertEquals(TestData("ready"), data)
    }

    @Test
    fun rejectsUnsuccessfulEnvelopeOnSuccessfulHttpStatus() = runTest {
        val response = response(
            status = HttpStatusCode.OK,
            body = """{"success":false,"code":"VALIDATION","message":"잘못된 요청","data":null}""",
        )

        val error = assertFailsWith<ApiException> {
            response.requireApiSuccess(apiJson, failureMessage = "fallback")
        }

        assertEquals(200, error.statusCode)
        assertEquals("VALIDATION", error.code)
        assertEquals("잘못된 요청", error.message)
        assertFalse(error.isUnauthorized)
    }

    @Test
    fun preservesUnauthorizedMetadata() = runTest {
        val response = response(
            status = HttpStatusCode.Unauthorized,
            body = """{"success":false,"code":"UNAUTHORIZED","message":"","data":null}""",
        )

        val error = assertFailsWith<ApiException> {
            response.requireApiSuccess(apiJson, failureMessage = "fallback")
        }

        assertEquals(401, error.statusCode)
        assertEquals("UNAUTHORIZED", error.code)
        assertEquals("로그인이 만료되었습니다.", error.message)
        assertTrue(error.isUnauthorized)
    }

    @Test
    fun preservesHttpFailureMetadata() = runTest {
        val response = response(
            status = HttpStatusCode.BadRequest,
            body = """{"success":false,"code":"BAD_REQUEST","message":"","data":null}""",
        )

        val error = assertFailsWith<ApiException> {
            response.requireApiSuccess(apiJson, failureMessage = "요청 실패")
        }

        assertEquals(400, error.statusCode)
        assertEquals("BAD_REQUEST", error.code)
        assertEquals("요청 실패", error.message)
    }

    @Test
    fun rejectsMalformedEnvelope() = runTest {
        val response = response(HttpStatusCode.OK, body = "not-json")

        val error = assertFailsWith<ApiException> {
            response.requireApiSuccess(
                json = apiJson,
                failureMessage = "요청 실패",
                envelopeFailureMessage = "envelope failure",
            )
        }

        assertEquals("envelope failure", error.message)
    }

    @Test
    fun rejectsMissingData() = runTest {
        val response = response(
            status = HttpStatusCode.OK,
            body = """{"success":true,"code":"OK","message":"","data":null}""",
        )

        val error = assertFailsWith<ApiException> {
            response.decodeApiData<TestData>(
                json = apiJson,
                missingMessage = "data missing",
                failureMessage = "request failed",
            )
        }

        assertEquals("data missing", error.message)
        assertEquals("OK", error.code)
    }

    @Test
    fun rejectsMalformedData() = runTest {
        val response = response(
            status = HttpStatusCode.OK,
            body = """{"success":true,"code":"OK","message":"","data":{"unexpected":1}}""",
        )

        val error = assertFailsWith<ApiException> {
            response.decodeApiData<TestData>(
                json = apiJson,
                missingMessage = "data missing",
                failureMessage = "request failed",
                dataFailureMessage = "data failure",
            )
        }

        assertEquals("data failure", error.message)
        assertEquals("OK", error.code)
    }

    private suspend fun response(
        status: HttpStatusCode,
        body: String,
    ) = HttpClient(
        MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        },
    ) {
        expectSuccess = false
    }.get("https://example.test/api")

    @Serializable
    private data class TestData(
        val value: String,
    )
}
