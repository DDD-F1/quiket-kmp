package com.f1.quiket.feature.review.navigation

import com.f1.quiket.core.navigation.QuiketDestination

data object ReviewDestination : QuiketDestination {
    override val route: String = "main/review"
}
