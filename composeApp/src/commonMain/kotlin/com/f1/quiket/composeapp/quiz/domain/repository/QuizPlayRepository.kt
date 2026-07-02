package com.f1.quiket.composeapp.quiz.domain.repository

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
