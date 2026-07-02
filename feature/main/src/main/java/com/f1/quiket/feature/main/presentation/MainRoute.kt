package com.f1.quiket.feature.main.presentation

import androidx.compose.runtime.Composable

@Composable
fun MainRoute(onLogout: () -> Unit) {
    MainScreen(onLogout = onLogout)
}
