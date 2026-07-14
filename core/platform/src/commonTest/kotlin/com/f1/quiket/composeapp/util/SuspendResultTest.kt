package com.f1.quiket.composeapp.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SuspendResultTest {
    @Test
    fun returnsSuccessfulResult() = runTest {
        val result = runSuspendCatching { 42 }

        assertEquals(42, result.getOrThrow())
    }

    @Test
    fun capturesOrdinaryFailure() = runTest {
        val failure = IllegalStateException("failed")

        val result = runSuspendCatching<Int> { throw failure }

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun rethrowsCancellationException() = runTest {
        val cancellation = CancellationException("cancelled")

        val thrown = assertFailsWith<CancellationException> {
            runSuspendCatching<Int> { throw cancellation }
        }

        assertSame(cancellation, thrown)
    }
}
