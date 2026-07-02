package com.f1.quiket.feature.mypage.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Orange100
import com.f1.quiket.core.designsystem.theme.Orange300
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.mypage.R
import com.f1.quiket.feature.mypage.presentation.RoomItem

// ─── 레벨별 배경색 ─────────────────────────────────────────────────────────────
//
// MyPageScreen의 Column background와 CharacterRoomSection 배경을 동일하게 유지하기 위해
// 이 함수를 단일 진실의 원천(Single Source of Truth)으로 사용합니다.
// 배경색 변경 시 이 함수만 수정하면 양쪽이 자동으로 동기화됩니다.

fun getRoomBackgroundColor(level: Int): Color = when (level) {
    1 -> Orange100
    2 -> Color(0xFFDFF2E1)
    3 -> Color(0xFFFFF9C4)
    4 -> Color(0xFFE3F2FD)
    5 -> Color(0xFFD6EAF8)
    else -> Orange100
}

// ─── 아이템 시각 설정 ───────────────────────────────────────────────────────────
//
// 새 아이템 추가 방법:
//   1. RoomItem enum에 값 추가
//   2. MyPageViewModel.resolveUnlockedItems()에 레벨 조건 추가
//   3. 아래 roomItemVisuals 맵에 RoomItemVisual 설정 추가
//
// Layer.BEHIND_CHARACTER : 큐링이 뒤에 그려짐
// Layer.IN_FRONT_OF_CHARACTER : 큐링이 앞에 그려짐

private enum class Layer { BEHIND_CHARACTER, IN_FRONT_OF_CHARACTER }

private data class RoomItemVisual(
    @DrawableRes val drawableRes: Int,
    val alignment: Alignment,
    val width: Dp,
    val height: Dp,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val layer: Layer = Layer.BEHIND_CHARACTER,
)

private val roomItemVisuals: Map<RoomItem, RoomItemVisual> = mapOf(
    RoomItem.RUG to RoomItemVisual(
        drawableRes = R.drawable.ic_item_carpet,
        alignment = Alignment.BottomCenter,
        width = 240.dp,
        height = 64.dp,
        offsetY = (-4).dp,
    ),
    RoomItem.SOFA to RoomItemVisual(
        drawableRes = R.drawable.ic_item_sofa,
        alignment = Alignment.BottomStart,
        width = 150.dp,
        height = 150.dp,
        offsetX = 40.dp,
        offsetY = (-40).dp,
    ),
    RoomItem.PLANT to RoomItemVisual(
        drawableRes = R.drawable.ic_item_flower,
        alignment = Alignment.BottomStart,
        width = 56.dp,
        height = 56.dp,
        offsetX = 80.dp,
        offsetY = (-20).dp,
    ),
    RoomItem.CLOCK to RoomItemVisual(
        drawableRes = R.drawable.ic_item_clock,
        alignment = Alignment.TopCenter,
        width = 56.dp,
        height = 56.dp,
        offsetX = 40.dp,
        offsetY = 10.dp,
    ),
)

// ─── Composable ────────────────────────────────────────────────────────────────

@Composable
fun CharacterRoomSection(
    level: Int,
    unlockedItems: Set<RoomItem>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(getRoomBackgroundColor(level)),
    ) {
        RoomItems(unlockedItems = unlockedItems, layer = Layer.BEHIND_CHARACTER)

        // 캐릭터 + 말풍선
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_my_qring),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(120.dp),
            )
            Icon(
                painter = painterResource(R.drawable.ic_speech_balloon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 50.dp, y = (-30).dp),
            )
        }

        RoomItems(unlockedItems = unlockedItems, layer = Layer.IN_FRONT_OF_CHARACTER)
    }
}

@Composable
private fun BoxScope.RoomItems(unlockedItems: Set<RoomItem>, layer: Layer) {
    roomItemVisuals
        .filter { (item, visual) -> item in unlockedItems && visual.layer == layer }
        .forEach { (_, visual) ->
            Icon(
                painter = painterResource(visual.drawableRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(visual.alignment)
                    .offset(x = visual.offsetX, y = visual.offsetY)
                    .size(width = visual.width, height = visual.height),
            )
        }
}

// ─── Speech Bubble ─────────────────────────────────────────────────────────────

@Composable
private fun RoomSpeechBubble(
    unlockedItems: Set<RoomItem>,
    modifier: Modifier = Modifier,
) {
    val message = when {
        RoomItem.PLANT !in unlockedItems -> "화분을 모아서\n방꾸미기를 해야지!"
        RoomItem.RUG !in unlockedItems -> "카펫을 모아서\n방꾸미기를 해야지!"
        RoomItem.SOFA !in unlockedItems -> "소파를 모아서\n방꾸미기를 해야지!"
        RoomItem.CLOCK !in unlockedItems -> "시계를 모아서\n방꾸미기를 해야지!"
        else -> "방 꾸미기 완성!\n최고의 큐링이야!"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Orange300)
            .border(width = 1.dp, color = Orange300, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = message,
            fontSize = 11.sp,
            color = Brown950,
            lineHeight = 16.sp,
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Room — Lv.1 첫걸음 다람쥐")
@Composable
private fun RoomLv1Preview() {
    QuiketTheme {
        CharacterRoomSection(level = 1, unlockedItems = setOf(RoomItem.BASIC_ROOM))
    }
}

@Preview(showBackground = true, name = "Room — Lv.2 결심한 다람쥐")
@Composable
private fun RoomLv2Preview() {
    QuiketTheme {
        CharacterRoomSection(level = 2, unlockedItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT))
    }
}

@Preview(showBackground = true, name = "Room — Lv.3 펜굴리는 다람쥐")
@Composable
private fun RoomLv3Preview() {
    QuiketTheme {
        CharacterRoomSection(level = 3, unlockedItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG))
    }
}

@Preview(showBackground = true, name = "Room — Lv.4 노력형 다람쥐")
@Composable
private fun RoomLv4Preview() {
    QuiketTheme {
        CharacterRoomSection(level = 4, unlockedItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG, RoomItem.SOFA))
    }
}

@Preview(showBackground = true, name = "Room — Lv.5 열공 다람쥐")
@Composable
private fun RoomLv5Preview() {
    QuiketTheme {
        CharacterRoomSection(level = 5, unlockedItems = setOf(RoomItem.BASIC_ROOM, RoomItem.PLANT, RoomItem.RUG, RoomItem.SOFA, RoomItem.CLOCK))
    }
}