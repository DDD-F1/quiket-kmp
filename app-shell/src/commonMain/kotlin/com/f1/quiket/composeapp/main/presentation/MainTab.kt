package com.f1.quiket.composeapp.main.presentation

import org.jetbrains.compose.resources.DrawableResource
import com.f1.quiket.appshell.resources.Res
import com.f1.quiket.appshell.resources.ic_bottom_home_gray
import com.f1.quiket.appshell.resources.ic_bottom_home_primary
import com.f1.quiket.appshell.resources.ic_bottom_my_gray
import com.f1.quiket.appshell.resources.ic_bottom_my_primary
import com.f1.quiket.appshell.resources.ic_bottom_record_gray
import com.f1.quiket.appshell.resources.ic_bottom_record_primary
import com.f1.quiket.appshell.resources.ic_bottom_review_gray
import com.f1.quiket.appshell.resources.ic_bottom_review_primary

internal enum class MainTab(
    val label: String,
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource,
) {
    Home(
        label = "홈",
        selectedIcon = Res.drawable.ic_bottom_home_primary,
        unselectedIcon = Res.drawable.ic_bottom_home_gray,
    ),
    History(
        label = "기록",
        selectedIcon = Res.drawable.ic_bottom_record_primary,
        unselectedIcon = Res.drawable.ic_bottom_record_gray,
    ),
    Review(
        label = "오답노트",
        selectedIcon = Res.drawable.ic_bottom_review_primary,
        unselectedIcon = Res.drawable.ic_bottom_review_gray,
    ),
    MyPage(
        label = "마이",
        selectedIcon = Res.drawable.ic_bottom_my_primary,
        unselectedIcon = Res.drawable.ic_bottom_my_gray,
    ),
}
