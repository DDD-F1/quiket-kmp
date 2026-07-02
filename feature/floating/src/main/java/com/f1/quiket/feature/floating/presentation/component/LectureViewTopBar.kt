package com.f1.quiket.feature.floating.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Gray700
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R

@Composable
fun LectureViewTopBar(
    title: String,
    onBackClick: () -> Unit,
    onTocClick: () -> Unit,
    onEditPartNameClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeletePartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(White)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 뒤로가기
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(R.drawable.ic_detail_back),
                contentDescription = "뒤로가기",
                tint = Gray700,
                modifier = Modifier.size(24.dp),
            )
        }

        // 과목명 타이틀
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Gray950,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 목차 토글 (햄버거)
        IconButton(onClick = onTocClick) {
            Icon(
                painter = painterResource(R.drawable.ic_detail_lecture_list),
                contentDescription = "목차",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }

        // 더보기 ... → DropdownMenu
        Box {
            IconButton(onClick = { showMoreMenu = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_detail_etc),
                    contentDescription = "더보기",
                    tint = Gray700,
                    modifier = Modifier.size(24.dp),
                )
            }
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                containerColor = White,
                modifier = Modifier.padding(4.dp),
                shape = RoundedCornerShape(12.dp),
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
                            "파트명 수정",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = Gray950,
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onEditPartNameClick()
                    },
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
                            "내용 수정",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = Gray950,
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onEditClick()
                    },
                )
//                DropdownMenuItem(
//                    leadingIcon = {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_detail_remove),
//                            contentDescription = null,
//                            tint = Color.Unspecified,
//                        )
//                    },
//                    text = {
//                        Text(
//                            "파트 삭제",
//                            style = MaterialTheme.typography.bodyMedium.copy(
//                                fontWeight = FontWeight.Normal
//                            ),
//                            color = Negative,
//                        )
//                    },
//                    onClick = {
//                        showMoreMenu = false
//                        onDeletePartClick()
//                    },
//                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LectureViewTopBarPreview() {
    QuiketTheme {
        LectureViewTopBar(
            title = "SQLD",
            onBackClick = {},
            onTocClick = {},
            onEditPartNameClick = {},
            onEditClick = {},
            onDeletePartClick = {},
        )
    }
}