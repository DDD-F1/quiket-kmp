package com.f1.quiket.core.database.datastore

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    fun getFirstLaunch(): Flow<Boolean> = repository.isFirstLaunch

    suspend fun setDone() = repository.setFirstLaunchDone()
}