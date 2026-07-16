package com.f1.quiket.composeapp.auth

import android.content.Context
import android.content.SharedPreferences

object AndroidSessionContext {
    private const val GeneralPreferencesName = "quiket_cmp_session"
    private const val SecurePreferencesName = "quiket_cmp_secure_session"

    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun contextOrNull(): Context? = applicationContext

    fun context(): Context =
        checkNotNull(applicationContext) {
            "AndroidSessionContext.init(context) must be called before reading session state."
        }

    fun generalPreferences(): SharedPreferences =
        context().getSharedPreferences(GeneralPreferencesName, Context.MODE_PRIVATE)

    fun securePreferences(): SharedPreferences =
        context().getSharedPreferences(SecurePreferencesName, Context.MODE_PRIVATE)
}
