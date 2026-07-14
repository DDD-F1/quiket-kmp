package com.f1.quiket.composeapp.quiz.domain.usecase

import com.f1.quiket.composeapp.auth.domain.usecase.AuthenticatedCallRunner
import com.f1.quiket.composeapp.quiz.domain.model.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayException
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlaySession
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.domain.model.QuizSession
import com.f1.quiket.composeapp.quiz.domain.model.QuizSubmitResult
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository

internal class QuizPlayUseCases(
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val repository: QuizPlayRepository,
) {
    suspend fun getQuizSession(quizSessionId: String): QuizSession =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getQuizSession(session = session, quizSessionId = quizSessionId)
        }

    suspend fun createQuizSession(request: QuizCreateRequest): QuizGenerationAccepted =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.createQuizSession(session = session, request = request)
        }

    suspend fun getQuizScope(subjectId: String): QuizScope =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getQuizScope(session = session, subjectId = subjectId)
        }

    suspend fun getQuizGenerationStatus(quizSessionId: String): QuizGenerationProgress =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.getQuizGenerationStatus(session = session, quizSessionId = quizSessionId)
        }

    suspend fun startQuizPlaySession(
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType = QuizPlayType.First,
    ): QuizPlaySession =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.startQuizPlaySession(
                session = session,
                quizSessionId = quizSessionId,
                clientSessionId = clientSessionId,
                playType = playType,
            )
        }

    suspend fun retryAllQuestions(
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.retryAllQuestions(
                session = session,
                resultId = resultId,
                clientSessionId = clientSessionId,
            )
        }

    suspend fun retryWrongQuestions(
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.retryWrongQuestions(
                session = session,
                resultId = resultId,
                clientSessionId = clientSessionId,
            )
        }

    suspend fun submitQuizResult(request: QuizResultSubmit): QuizSubmitResult =
        authenticatedCallRunner.run(isUnauthorized = ::isUnauthorized) { session ->
            repository.submitQuizResult(session = session, request = request)
        }

    private fun isUnauthorized(error: Throwable): Boolean =
        error is QuizPlayException && error.isUnauthorized
}
