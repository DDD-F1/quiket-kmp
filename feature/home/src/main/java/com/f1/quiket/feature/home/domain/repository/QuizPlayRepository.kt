package com.f1.quiket.feature.home.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.QuizPlaySession
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizResult
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.f1.quiket.feature.home.domain.model.QuizSession

interface QuizPlayRepository {
    suspend fun getQuizSession(quizSessionId: String): NetworkResult<QuizSession>

    suspend fun startQuizPlaySession(
        quizSessionId: String,
        request: QuizPlayStart,
    ): NetworkResult<QuizPlaySession>

    suspend fun submitQuizResult(request: QuizResultSubmit): NetworkResult<QuizResult>

    suspend fun getQuizResult(resultId: String): NetworkResult<QuizResult>

    suspend fun retryAllQuestions(
        resultId: String,
        request: QuizRetry,
    ): NetworkResult<QuizPlaySession>

    suspend fun retryWrongQuestions(
        resultId: String,
        request: QuizRetry,
    ): NetworkResult<QuizPlaySession>
}
