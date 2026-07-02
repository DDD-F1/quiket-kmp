package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Green800
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R

@Composable
fun SubjectDetailTopBar(
    title: String,
    isStarred: Boolean,
    showMenu: Boolean,
    onBackClick: () -> Unit,
    onStarClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEditSubjectType: () -> Unit = {},
    onEditSubjectName: () -> Unit = {},
    onEditChapterName: () -> Unit = {},
    onDeleteSubjectClick: () -> Unit = {},
    onDeleteChapterClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Green800)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(R.drawable.ic_detail_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onStarClick) {
            Icon(
                painter = if (isStarred) painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_star_on) else painterResource(
                    com.f1.quiket.core.designsystem.R.drawable.ic_star_off
                ),
                contentDescription = "즐겨찾기",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
        Box {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_detail_etc),
                    contentDescription = "메뉴",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                modifier = Modifier.padding(4.dp),
                 shape = RoundedCornerShape(12.dp),
                expanded = showMenu,
                onDismissRequest = onMenuDismiss,
                containerColor = White,
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_edit),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            "과목 유형 수정",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray950
                        )
                    },
                    onClick = { onMenuDismiss(); onEditSubjectType() },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_edit),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            "과목명 수정",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray950
                        )
                    },
                    onClick = { onMenuDismiss(); onEditSubjectName() },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_edit),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            "챕터명 수정",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray950
                        )
                    },
                    onClick = { onMenuDismiss(); onEditChapterName() },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_remove),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            "과목 삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Negative
                        )
                    },
                    onClick = { onMenuDismiss(); onDeleteSubjectClick() },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_detail_remove),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    },
                    text = {
                        Text(
                            "챕터 삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Negative
                        )
                    },
                    onClick = { onMenuDismiss(); onDeleteChapterClick() },
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Previews
// ────────────────────────────────────────────────────────────────────────────

@Preview(name = "TopBar — 기본", showBackground = true, backgroundColor = 0xFF2D1F1D)
@Composable
private fun SubjectDetailTopBarPreview() {
    QuiketTheme {
        SubjectDetailTopBar(
            title = "SQLD",
            isStarred = false,
            showMenu = false,
            onBackClick = {},
            onStarClick = {},
            onMenuClick = {},
            onMenuDismiss = {},
        )
    }
}

@Preview(name = "TopBar — 즐겨찾기 ON", showBackground = true, backgroundColor = 0xFF2D1F1D)
@Composable
private fun SubjectDetailTopBarStarredPreview() {
    QuiketTheme {
        SubjectDetailTopBar(
            title = "SQLD",
            isStarred = true,
            showMenu = false,
            onBackClick = {},
            onStarClick = {},
            onMenuClick = {},
            onMenuDismiss = {},
        )
    }
}

@Preview(name = "TopBar — 메뉴 오픈", showBackground = true, backgroundColor = 0xFF2D1F1D)
@Composable
private fun SubjectDetailTopBarMenuPreview() {
    var showMenu by remember { mutableStateOf(true) }
    QuiketTheme {
        SubjectDetailTopBar(
            title = "SQLD",
            isStarred = false,
            showMenu = showMenu,
            onBackClick = {},
            onStarClick = {},
            onMenuClick = { showMenu = true },
            onMenuDismiss = { showMenu = false },
        )
    }
}