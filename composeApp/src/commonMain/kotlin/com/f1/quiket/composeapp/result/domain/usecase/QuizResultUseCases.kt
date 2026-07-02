package com.f1.quiket.composeapp.result.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.result.QuizResult
import com.f1.quiket.composeapp.result.QuizResultException
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository

internal class QuizResultUseCases(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: QuizResultRepository,
) {
    suspend fun getQuizResult(resultId: String): QuizResult =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getQuizResult(session = session, resultId = resultId)
        }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is QuizResultException && error.isUnauthorized
}
