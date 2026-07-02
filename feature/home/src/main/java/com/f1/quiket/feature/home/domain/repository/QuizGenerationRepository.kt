package com.f1.quiket.feature.home.domain.repository

import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizGenerationAccepted
import com.f1.quiket.feature.home.domain.model.QuizGenerationProgress
import com.f1.quiket.feature.home.domain.model.QuizScope

interface QuizGenerationRepository {
    suspend fun getQuizScope(subjectId: String): NetworkResult<QuizScope>

    suspend fun createQuizSession(request: QuizCreate): NetworkResult<QuizGenerationAccepted>

    suspend fun getGenerationStatus(quizSessionId: String): NetworkResult<QuizGenerationProgress>
}
