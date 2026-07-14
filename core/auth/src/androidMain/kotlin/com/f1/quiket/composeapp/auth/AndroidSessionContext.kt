package com.f1.quiket.composeapp.auth

import android.content.Context
import android.content.SharedPreferences

object AndroidSessionContext {
    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun preferences(): SharedPreferences {
        val context = checkNotNull(applicationContext) {
            "AndroidSessionContext.init(context) must be called before reading session state."
        }
        return context.getSharedPreferences("quiket_cmp_session", Context.MODE_PRIVATE)
    }
}
