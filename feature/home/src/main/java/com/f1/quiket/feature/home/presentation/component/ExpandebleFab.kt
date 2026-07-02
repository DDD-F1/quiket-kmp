package com.f1.quiket.feature.home.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.theme.Brown50
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.home.R
import com.f1.quiket.feature.home.presentation.model.FabAction

@Composable
fun ExpandableFab(
    isExpanded: Boolean,
    onFabClick: () -> Unit,
    onItemClick: (FabAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Surface(
                color = Color.Transparent,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {

                    SmallFab(
                        R.drawable.ic_small_floting_test, "시험 등록하기"
                    ) {
                        onItemClick(FabAction.ScheduleExam)
                    }

                    SmallFab(
                        R.drawable.ic_small_floting_quiz, "퀴즈 만들기"
                    ) {
                        onItemClick(FabAction.CreateQuiz)
                    }

                    SmallFab(
                        R.drawable.ic_small_floting_upload, "자료 업로드"
                    ) {
                        onItemClick(FabAction.Upload)
                    }

                    SmallFab(
                        R.drawable.ic_small_floting_add, "과목 추가"
                    ) {
                        onItemClick(FabAction.AddSubject)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FloatingActionButton(
            onClick = onFabClick,
            containerColor = Brown950,
            shape = RoundedCornerShape(1000.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isExpanded) R.drawable.ic_floating_close else R.drawable.ic_floating_plus
                ),
                contentDescription = null
            )
        }
    }
}

@Preview(name = "FAB — 접힌 상태", showBackground = true, backgroundColor = 0xFFF5EFE6)
@Composable
private fun ExpandableFabCollapsedPreview() {
    QuiketTheme {
        Box(
            modifier = Modifier
                .background(Brown50)
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            ExpandableFab(
                isExpanded = false,
                onFabClick = {},
                onItemClick = {},
            )
        }
    }
}

@Preview(name = "FAB — 펼쳐진 상태", showBackground = false)
@Composable
private fun ExpandableFabExpandedPreview() {
    QuiketTheme {
        var isExpanded by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .background(Brown50)
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            ExpandableFab(
                isExpanded = isExpanded,
                onFabClick = { isExpanded = !isExpanded },
                onItemClick = {},
            )
        }
    }
}

@Composable
fun SmallFab(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 5.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = White
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, Brown950, CircleShape)
                .background(White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Brown950,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}