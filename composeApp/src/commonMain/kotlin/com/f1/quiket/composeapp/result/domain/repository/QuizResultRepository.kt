package com.f1.quiket.composeapp.result.domain.repository

import com.f1.quiket.composeapp.auth.SessionSnapshot
import com.f1.quiket.composeapp.result.QuizResult

internal interface QuizResultRepository {
    suspend fun getQuizResult(session: SessionSnapshot, resultId: String): QuizResult
}
