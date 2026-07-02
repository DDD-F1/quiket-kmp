package com.f1.quiket.feature.main.presentation

import androidx.annotation.DrawableRes
import com.f1.quiket.core.designsystem.R
import com.f1.quiket.core.navigation.QuiketDestination
import com.f1.quiket.feature.history.navigation.HistoryDestination
import com.f1.quiket.feature.home.navigation.HomeDestination
import com.f1.quiket.feature.mypage.navigation.MyPageDestination
import com.f1.quiket.feature.review.navigation.ReviewDestination

enum class MainTab(
    val destination: QuiketDestination,
    val label: String,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int
) {
    Home(
        destination = HomeDestination,
        label = "홈",
        selectedIconRes = R.drawable.ic_bottom_home_primary,
        unselectedIconRes = R.drawable.ic_bottom_home_gray
    ),
    History(
        destination = HistoryDestination,
        label = "기록",
        selectedIconRes = R.drawable.ic_bottom_record_primary,
        unselectedIconRes = R.drawable.ic_bottom_record_gray
    ),
    Review(
        destination = ReviewDestination,
        label = "오답노트",
        selectedIconRes = R.drawable.ic_bottom_review_primary,
        unselectedIconRes = R.drawable.ic_bottom_review_gray
    ),
    MyPage(
        destination = MyPageDestination,
        label = "마이",
        selectedIconRes = R.drawable.ic_bottom_my_primary,
        unselectedIconRes = R.drawable.ic_bottom_my_gray
    ),
}
