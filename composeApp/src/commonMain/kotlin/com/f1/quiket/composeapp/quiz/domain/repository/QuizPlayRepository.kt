package com.f1.quiket.composeapp.quiz.domain.repository

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

internal interface QuizPlayRepository {
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
