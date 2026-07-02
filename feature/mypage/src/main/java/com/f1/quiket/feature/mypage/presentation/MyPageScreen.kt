package com.f1.quiket.feature.mypage.presentation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.mypage.component.AcornShopButton
import com.f1.quiket.feature.mypage.component.CharacterRoomSection
import com.f1.quiket.feature.mypage.component.LevelProfileSection
import com.f1.quiket.feature.mypage.component.MyPageTopBar
import com.f1.quiket.feature.mypage.component.StatsRow
import com.f1.quiket.feature.mypage.component.getRoomBackgroundColor
import com.f1.quiket.feature.mypage.data.model.CharacterLevel
import com.f1.quiket.feature.mypage.data.model.UserProfile

@Composable
fun MyPageScreen(
    state: MyPageState,
    onIntent: (MyPageIntent) -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState()),
    ) {
        MyPageTopBar(
            onSettingsClick = { onIntent(MyPageIntent.NavigateToSettings) },
        )

        // getRoomBackgroundColor를 Single Source of Truth로 사용하여
        // LevelProfileSection 영역과 CharacterRoomSection 배경색을 항상 동일하게 유지
        val roomBgColor = getRoomBackgroundColor(state.characterLevel.level)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(roomBgColor),
        ) {
            LevelProfileSection(
                nickname = state.profile.nickname,
                level = state.characterLevel.level,
                progress = state.characterLevel.progressIn(state.totalXp),
            )

            CharacterRoomSection(
                level = state.characterLevel.level,
                unlockedItems = state.unlockedRoomItems,
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatsRow(
            acornCount = state.acornCount,
            streakDays = state.streakDays,
        )

        Spacer(modifier = Modifier.height(16.dp))

        AcornShopButton(
            onClick = { onIntent(MyPageIntent.NavigateToAcornShop) },
            isLocked = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MyPage — Lv.1 첫걸음 다람쥐")
@Composable
private fun MyPageScreenLv1Preview() {
    QuiketTheme {
        MyPageScreen(
            state = MyPageState(
                isLoading = false,
                profile = UserProfile(nickname = "첫걸음 다람쥐"),
                characterLevel = CharacterLevel.FIRST_STEP,
                totalXp = 50,
                acornCount = 5,
                streakDays = 1,
                unlockedRoomItems = setOf(RoomItem.BASIC_ROOM),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MyPage — Lv.2 결심한 다람쥐")
@Composable
private fun MyPageScreenLv2Preview() {
    QuiketTheme {
        MyPageScreen(
            state = MyPageState(
                isLoading = false,
                profile = UserProfile(nickname = "결심한 다람쥐"),
                characterLevel = CharacterLevel.DETERMINED,
                totalXp = 220,
                acornCount = 22,
                streakDays = 3,
                unlockedRoomItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MyPage — Lv.3 펜굴리는 다람쥐")
@Composable
private fun MyPageScreenLv3Preview() {
    QuiketTheme {
        MyPageScreen(
            state = MyPageState(
                isLoading = false,
                profile = UserProfile(nickname = "펜굴리는 다람쥐"),
                characterLevel = CharacterLevel.PEN_ROLLING,
                totalXp = 600,
                acornCount = 60,
                streakDays = 10,
                unlockedRoomItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MyPage — Lv.4 노력형 다람쥐")
@Composable
private fun MyPageScreenLv4Preview() {
    QuiketTheme {
        MyPageScreen(
            state = MyPageState(
                isLoading = false,
                profile = UserProfile(nickname = "노력형 다람쥐"),
                characterLevel = CharacterLevel.HARDWORKING,
                totalXp = 1300,
                acornCount = 130,
                streakDays = 21,
                unlockedRoomItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG, RoomItem.SOFA),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "MyPage — Lv.5 열공 다람쥐")
@Composable
private fun MyPageScreenLv5Preview() {
    QuiketTheme {
        MyPageScreen(
            state = MyPageState(
                isLoading = false,
                profile = UserProfile(nickname = "열공 다람쥐"),
                characterLevel = CharacterLevel.STUDIOUS,
                totalXp = 2500,
                acornCount = 250,
                streakDays = 45,
                unlockedRoomItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG, RoomItem.SOFA, RoomItem.CLOCK),
            ),
            onIntent = {},
        )
    }
}