package com.f1.quiket.composeapp.result.data.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.result.domain.model.QuizResult
import com.f1.quiket.composeapp.result.data.remote.QuizResultRemoteDataSource
import com.f1.quiket.composeapp.result.domain.repository.QuizResultRepository

internal class QuizResultRepositoryImpl(
    private val remoteDataSource: QuizResultRemoteDataSource,
) : QuizResultRepository {
    override suspend fun getQuizResult(session: SessionSnapshot, resultId: String): QuizResult =
        remoteDataSource.getQuizResult(session = session, resultId = resultId)
}
