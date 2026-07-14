package com.f1.quiket.composeapp.result.data.remote

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.data.remote.QuizResultClient

internal interface QuizResultRemoteDataSource {
    suspend fun getQuizResult(session: SessionSnapshot, resultId: String): QuizResult
}

internal class QuizResultRemoteDataSourceImpl(
    private val client: QuizResultClient,
) : QuizResultRemoteDataSource {
    override suspend fun getQuizResult(session: SessionSnapshot, resultId: String): QuizResult =
        client.getQuizResult(session = session, resultId = resultId)
}
