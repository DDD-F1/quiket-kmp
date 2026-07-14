package com.f1.quiket.composeapp.network

import kotlin.test.Test
import kotlin.test.assertEquals

class UserFacingErrorsTest {
    @Test
    fun preservesMeaningfulServerMessage() {
        val error = IllegalStateException("잘못된 입력값입니다.")

        assertEquals(
            "잘못된 입력값입니다.",
            error.toUserFacingMessage("요청에 실패했습니다."),
        )
    }

    @Test
    fun replacesLowLevelNetworkMessageWithRetryGuide() {
        val error = IllegalStateException("failed to connect to api.quiket.test")

        assertEquals(
            "요청에 실패했습니다. 네트워크 상태를 확인한 뒤 다시 시도해주세요.",
            error.toUserFacingMessage("요청에 실패했습니다."),
        )
    }

    @Test
    fun detectsNetworkFailureInCauseChain() {
        val error = IllegalStateException(
            "request wrapper",
            IllegalStateException("network is unreachable"),
        )

        assertEquals(
            "불러오지 못했습니다. 네트워크 상태를 확인한 뒤 다시 시도해주세요.",
            error.toUserFacingMessage("불러오지 못했습니다."),
        )
    }

    @Test
    fun blankFailureUsesFallback() {
        assertEquals(
            "저장하지 못했습니다.",
            IllegalStateException("  ").toUserFacingMessage("저장하지 못했습니다."),
        )
    }
}
