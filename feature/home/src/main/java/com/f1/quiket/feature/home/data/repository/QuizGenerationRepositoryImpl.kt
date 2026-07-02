package com.f1.quiket.feature.home.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.home.data.mapper.toDomain
import com.f1.quiket.feature.home.data.mapper.toRequest
import com.f1.quiket.feature.home.data.remote.QuizGenerationApi
import com.f1.quiket.feature.home.domain.model.QuizCreate
import com.f1.quiket.feature.home.domain.model.QuizGenerationAccepted
import com.f1.quiket.feature.home.domain.model.QuizGenerationProgress
import com.f1.quiket.feature.home.domain.model.QuizScope
import com.f1.quiket.feature.home.domain.repository.QuizGenerationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class QuizGenerationRepositoryImpl @Inject constructor(
    private val api: QuizGenerationApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : QuizGenerationRepository {
    override suspend fun getQuizScope(subjectId: String): NetworkResult<QuizScope> =
        withContext(dispatchers.io) {
            responseHandler.execute(
                call = { api.getQuizScope(subjectId = subjectId) },
                mapper = { response -> response.toDomain() },
            )
        }

    override suspend fun createQuizSession(
        request: QuizCreate,
    ): NetworkResult<QuizGenerationAccepted> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.createQuizSession(request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun getGenerationStatus(
        quizSessionId: String,
    ): NetworkResult<QuizGenerationProgress> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getQuizGenerationStatus(quizSessionId = quizSessionId) },
            mapper = { response -> response.toDomain() },
        )
    }
}
