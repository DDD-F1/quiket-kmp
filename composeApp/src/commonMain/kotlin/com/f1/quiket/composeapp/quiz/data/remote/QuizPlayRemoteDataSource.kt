package com.f1.quiket.composeapp.quiz.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.quiz.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.QuizPlayClient
import com.f1.quiket.composeapp.quiz.QuizPlaySession
import com.f1.quiket.composeapp.quiz.QuizPlayType
import com.f1.quiket.composeapp.quiz.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.QuizScope
import com.f1.quiket.composeapp.quiz.QuizSession
import com.f1.quiket.composeapp.quiz.QuizSubmitResult

internal interface QuizPlayRemoteDataSource {
    suspend fun getQuizSession(session: SessionSnapshot, quizSessionId: String): QuizSession
    suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted
    suspend fun getQuizScope(session: SessionSnapshot, subjectId: String): QuizScope
    suspend fun getQuizGenerationStatus(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizGenerationProgress
    suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType = QuizPlayType.First,
    ): QuizPlaySession
    suspend fun retryAllQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession
    suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession
    suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult
}

internal class QuizPlayRemoteDataSourceImpl(
    private val client: QuizPlayClient,
) : QuizPlayRemoteDataSource {
    override suspend fun getQuizSession(session: SessionSnapshot, quizSessionId: String): QuizSession =
        client.getQuizSession(session = session, quizSessionId = quizSessionId)

    override suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted =
        client.createQuizSession(session = session, request = request)

    override suspend fun getQuizScope(session: SessionSnapshot, subjectId: String): QuizScope =
        client.getQuizScope(session = session, subjectId = subjectId)

    override suspend fun getQuizGenerationStatus(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizGenerationProgress =
        client.getQuizGenerationStatus(session = session, quizSessionId = quizSessionId)

    override suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType,
    ): QuizPlaySession =
        client.startQuizPlaySession(
            session = session,
            quizSessionId = quizSessionId,
            clientSessionId = clientSessionId,
            playType = playType,
        )

    override suspend fun retryAllQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession =
        client.retryAllQuestions(
            session = session,
            resultId = resultId,
            clientSessionId = clientSessionId,
        )

    override suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession =
        client.retryWrongQuestions(
            session = session,
            resultId = resultId,
            clientSessionId = clientSessionId,
        )

    override suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult =
        client.submitQuizResult(session = session, request = request)
}
