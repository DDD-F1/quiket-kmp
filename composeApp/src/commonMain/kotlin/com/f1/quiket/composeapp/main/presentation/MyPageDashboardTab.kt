package com.f1.quiket.composeapp.main.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketOrange500
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_item_carpet
import quiket.composeapp.generated.resources.ic_item_clock
import quiket.composeapp.generated.resources.ic_item_flower
import quiket.composeapp.generated.resources.ic_item_sofa
import quiket.composeapp.generated.resources.ic_my_acorn
import quiket.composeapp.generated.resources.ic_my_fire
import quiket.composeapp.generated.resources.ic_my_lock
import quiket.composeapp.generated.resources.ic_my_qring
import quiket.composeapp.generated.resources.ic_my_store
import quiket.composeapp.generated.resources.ic_qring_profile
import quiket.composeapp.generated.resources.ic_speech_balloon
import quiket.composeapp.generated.resources.ic_topbar_setting

@Composable
internal fun MyPageDashboardTab(
    nickname: String?,
    state: MyPageUiState,
    onRetry: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myPageData = (state as? MyPageUiState.Success)?.data
    val displayNickname = myPageData?.profile?.nickname?.takeIf { it.isNotBlank() }
        ?: nickname
        ?: "사용자"
    val level = (myPageData?.gamification?.currentLevel ?: myPageData?.profile?.currentLevel ?: 1)
        .coerceIn(1, 10)
    val dotori = myPageData?.gamification?.dotoriBalance ?: myPageData?.profile?.dotoriBalance ?: 0
    val xpTotal = myPageData?.gamification?.xpTotal ?: myPageData?.profile?.xpTotal ?: 0
    val progress = ((myPageData?.gamification?.levelProgressPct ?: levelProgressPct(level, xpTotal)) / 100f)
        .coerceIn(0f, 1f)
    val unlockedItems = remember(level) { unlockedRoomItems(level) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        MyPageDashboardTopBar(
            onSettingsClick = onSettingsClick,
        )

        when (state) {
            MyPageUiState.Idle,
            MyPageUiState.Loading,
            -> {
                Spacer(modifier = Modifier.height(18.dp))
                MyPageInfoCard(
                    title = "프로필을 불러오는 중",
                    rows = listOf("학습 정보를 확인하고 있어요."),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            is MyPageUiState.Error -> {
                Spacer(modifier = Modifier.height(18.dp))
                HomeErrorCard(
                    message = state.message,
                    onRetry = onRetry,
                    title = "프로필을 불러오지 못했어요",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            is MyPageUiState.Success -> {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(roomBackgroundColor(level)),
                ) {
                    LevelProfileCard(
                        nickname = displayNickname,
                        level = level,
                        progress = progress,
                    )
                    CharacterRoomSection(
                        level = level,
                        unlockedItems = unlockedItems,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                MyPageStatsRow(
                    acornCount = dotori,
                    streakDays = 0,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                AcornShopButton(
                    isLocked = true,
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MyPageDashboardTopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "마이페이지",
            color = QuiketGray950,
            fontSize = 17.sp,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(Res.drawable.ic_topbar_setting),
            contentDescription = "설정",
            tint = QuiketGray700,
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSettingsClick),
        )
    }
}

@Composable
private fun LevelProfileCard(
    nickname: String,
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    var progressAnimationStarted by remember(progress) { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (progressAnimationStarted) progress else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "levelProgress",
    )

    LaunchedEffect(progress) {
        progressAnimationStarted = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(QuiketWhite),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_qring_profile),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(level = level)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = nickname.ifBlank { "User" },
                        color = QuiketGray950,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = QuiketGray950,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(horizontal = 15.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(QuiketGray100),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(QuiketOrange500),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun LevelBadge(
    level: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(1.dp, QuiketOrange500, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF1E8))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Lv.$level",
            color = QuiketOrange500,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun CharacterRoomSection(
    level: Int,
    unlockedItems: Set<RoomItem>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(roomBackgroundColor(level)),
    ) {
        if (RoomItem.Rug in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_carpet),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
                    .size(width = 240.dp, height = 64.dp),
            )
        }
        if (RoomItem.Sofa in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_sofa),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(width = 150.dp, height = 150.dp),
            )
        }
        if (RoomItem.Plant in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_flower),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 80.dp, y = (-20).dp)
                    .size(56.dp),
            )
        }
        if (RoomItem.Clock in unlockedItems) {
            Image(
                painter = painterResource(Res.drawable.ic_item_clock),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 40.dp, y = 10.dp)
                    .size(56.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_my_qring),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
            Image(
                painter = painterResource(Res.drawable.ic_speech_balloon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 50.dp, y = (-30).dp),
            )
        }
    }
}

@Composable
private fun MyPageStatsRow(
    acornCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MyPageStatCard(
            icon = Res.drawable.ic_my_acorn,
            label = "보유 도토리",
            value = "${acornCount}개",
            modifier = Modifier.weight(1f),
        )
        MyPageStatCard(
            icon = Res.drawable.ic_my_fire,
            label = "연속 학습",
            value = "${streakDays}일",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyPageStatCard(
    icon: DrawableResource,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(1.dp, QuiketGray100, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(QuiketWhite)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(QuiketBrown950)
                .padding(8.dp),
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = QuiketGray700,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = QuiketBrown950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun AcornShopButton(
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, QuiketGray100, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(QuiketWhite)
                .clickable(enabled = !isLocked, role = Role.Button, onClick = onClick)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_my_store),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "도토리 상점",
                    color = QuiketGray950,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_my_lock),
                    contentDescription = "잠금",
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

private enum class RoomItem {
    BasicRoom,
    Plant,
    Rug,
    Sofa,
    Clock,
}

private fun unlockedRoomItems(level: Int): Set<RoomItem> = buildSet {
    add(RoomItem.BasicRoom)
    if (level >= 2) add(RoomItem.Plant)
    if (level >= 3) add(RoomItem.Rug)
    if (level >= 4) add(RoomItem.Sofa)
    if (level >= 5) add(RoomItem.Clock)
}

private fun roomBackgroundColor(level: Int): Color = when (level.coerceIn(1, 5)) {
    1 -> Color(0xFFFFEDD5)
    2 -> Color(0xFFDFF2E1)
    3 -> Color(0xFFFFF9C4)
    4 -> Color(0xFFE3F2FD)
    else -> Color(0xFFD6EAF8)
}

private fun levelProgressPct(level: Int, totalXp: Int): Int {
    val currentRequired = levelRequiredXp(level)
    val nextRequired = nextLevelRequiredXp(level) ?: return 100
    val progress = (totalXp - currentRequired).coerceAtLeast(0).toFloat() /
        (nextRequired - currentRequired).coerceAtLeast(1).toFloat()
    return (progress.coerceIn(0f, 1f) * 100).toInt()
}

private fun levelRequiredXp(level: Int): Int = when (level.coerceIn(1, 10)) {
    1 -> 0
    2 -> 100
    3 -> 350
    4 -> 850
    5 -> 1750
    6 -> 3250
    7 -> 5750
    8 -> 9750
    9 -> 16250
    else -> 26250
}

private fun nextLevelRequiredXp(level: Int): Int? = when (level.coerceIn(1, 10)) {
    1 -> 100
    2 -> 350
    3 -> 850
    4 -> 1750
    5 -> 3250
    6 -> 5750
    7 -> 9750
    8 -> 16250
    9 -> 26250
    else -> null
}

@Composable
private fun MyPageInfoCard(
    title: String,
    rows: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = QuiketGray50,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            rows.forEach { row ->
                Text(
                    text = row,
                    color = QuiketGray600,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
