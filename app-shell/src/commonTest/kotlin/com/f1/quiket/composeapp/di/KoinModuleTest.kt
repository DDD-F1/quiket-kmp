package com.f1.quiket.composeapp.di

import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.main.presentation.MainStateHolder
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.check.checkModules

class KoinModuleTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun appShellModuleProvidesRootStateHolders() {
        val app = startKoin {
            modules(appShellModule)
        }

        with(app.koin) {
            assertNotNull(get<AuthStateHolder>())
            assertNotNull(get<MainStateHolder>())
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun appShellModuleResolvesEveryDefinition() {
        val app = startKoin {
            modules(appShellModule)
        }

        app.checkModules()
    }
}
