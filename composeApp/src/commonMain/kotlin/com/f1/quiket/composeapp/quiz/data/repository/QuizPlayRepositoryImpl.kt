package com.f1.quiket.composeapp.quiz.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.quiz.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.QuizPlaySession
import com.f1.quiket.composeapp.quiz.QuizPlayType
import com.f1.quiket.composeapp.quiz.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.QuizScope
import com.f1.quiket.composeapp.quiz.QuizSession
import com.f1.quiket.composeapp.quiz.QuizSubmitResult
import com.f1.quiket.composeapp.quiz.data.remote.QuizPlayRemoteDataSource
import com.f1.quiket.composeapp.quiz.domain.repository.QuizPlayRepository

internal class QuizPlayRepositoryImpl(
    private val remoteDataSource: QuizPlayRemoteDataSource,
) : QuizPlayRepository {
    override suspend fun getQuizSession(session: SessionSnapshot, quizSessionId: String): QuizSession =
        remoteDataSource.getQuizSession(session = session, quizSessionId = quizSessionId)

    override suspend fun createQuizSession(
        session: SessionSnapshot,
        request: QuizCreateRequest,
    ): QuizGenerationAccepted =
        remoteDataSource.createQuizSession(session = session, request = request)

    override suspend fun getQuizScope(session: SessionSnapshot, subjectId: String): QuizScope =
        remoteDataSource.getQuizScope(session = session, subjectId = subjectId)

    override suspend fun getQuizGenerationStatus(
        session: SessionSnapshot,
        quizSessionId: String,
    ): QuizGenerationProgress =
        remoteDataSource.getQuizGenerationStatus(session = session, quizSessionId = quizSessionId)

    override suspend fun startQuizPlaySession(
        session: SessionSnapshot,
        quizSessionId: String,
        clientSessionId: String,
        playType: QuizPlayType,
    ): QuizPlaySession =
        remoteDataSource.startQuizPlaySession(
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
        remoteDataSource.retryAllQuestions(
            session = session,
            resultId = resultId,
            clientSessionId = clientSessionId,
        )

    override suspend fun retryWrongQuestions(
        session: SessionSnapshot,
        resultId: String,
        clientSessionId: String,
    ): QuizPlaySession =
        remoteDataSource.retryWrongQuestions(
            session = session,
            resultId = resultId,
            clientSessionId = clientSessionId,
        )

    override suspend fun submitQuizResult(
        session: SessionSnapshot,
        request: QuizResultSubmit,
    ): QuizSubmitResult =
        remoteDataSource.submitQuizResult(session = session, request = request)
}
