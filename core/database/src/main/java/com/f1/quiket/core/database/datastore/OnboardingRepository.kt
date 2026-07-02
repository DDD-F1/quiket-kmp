package com.f1.quiket.core.database.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val isFirstLaunch: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_FIRST_LAUNCH] = false
        }
    }
}