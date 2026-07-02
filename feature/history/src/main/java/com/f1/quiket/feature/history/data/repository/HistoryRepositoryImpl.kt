package com.f1.quiket.feature.history.data.repository

import com.f1.quiket.core.common.coroutines.AppDispatchers
import com.f1.quiket.core.network.model.NetworkResult
import com.f1.quiket.core.network.retrofit.ApiResponseHandler
import com.f1.quiket.feature.history.data.mapper.toDomain
import com.f1.quiket.feature.history.data.mapper.toRequest
import com.f1.quiket.feature.history.data.remote.HistoryApi
import com.f1.quiket.feature.history.domain.model.QuizPlaySession
import com.f1.quiket.feature.history.domain.model.QuizResult
import com.f1.quiket.feature.history.domain.model.QuizResultSubmit
import com.f1.quiket.feature.history.domain.model.QuizRetry
import com.f1.quiket.feature.history.domain.model.RecentActivityPage
import com.f1.quiket.feature.history.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val api: HistoryApi,
    private val responseHandler: ApiResponseHandler,
    private val dispatchers: AppDispatchers,
) : HistoryRepository {
    override suspend fun getRecentActivities(
        page: Int,
        size: Int,
    ): NetworkResult<RecentActivityPage> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getRecentActivities(page = page, size = size) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun submitQuizResult(
        request: QuizResultSubmit,
    ): NetworkResult<QuizResult> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.submitQuizResult(request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun getQuizResult(
        resultId: String,
    ): NetworkResult<QuizResult> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.getQuizResult(resultId = resultId) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun retryAllQuestions(
        resultId: String,
        request: QuizRetry,
    ): NetworkResult<QuizPlaySession> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.retryAllQuestions(resultId = resultId, request = request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }

    override suspend fun retryWrongQuestions(
        resultId: String,
        request: QuizRetry,
    ): NetworkResult<QuizPlaySession> = withContext(dispatchers.io) {
        responseHandler.execute(
            call = { api.retryWrongQuestions(resultId = resultId, request = request.toRequest()) },
            mapper = { response -> response.toDomain() },
        )
    }
}
