package com.f1.quiket.core.database.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey

val Context.dataStore by preferencesDataStore(name = "app_prefs")

object PreferenceKeys {
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
}