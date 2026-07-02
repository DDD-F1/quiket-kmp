package com.f1.quiket.feature.history.navigation

import com.f1.quiket.core.navigation.QuiketDestination

data object HistoryDestination : QuiketDestination {
    override val route: String = "main/history"
}
