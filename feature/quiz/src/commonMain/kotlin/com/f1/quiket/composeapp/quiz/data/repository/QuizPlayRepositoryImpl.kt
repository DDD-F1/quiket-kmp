package com.f1.quiket.composeapp.quiz.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.quiz.domain.model.QuizCreateRequest
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationAccepted
import com.f1.quiket.composeapp.quiz.domain.model.QuizGenerationProgress
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlaySession
import com.f1.quiket.composeapp.quiz.domain.model.QuizPlayType
import com.f1.quiket.composeapp.quiz.domain.model.QuizResultSubmit
import com.f1.quiket.composeapp.quiz.domain.model.QuizScope
import com.f1.quiket.composeapp.quiz.domain.model.QuizSession
import com.f1.quiket.composeapp.quiz.domain.model.QuizSubmitResult
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
