package com.f1.quiket.composeapp.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.ic_alert
import quiket.composeapp.generated.resources.ic_home_note
import quiket.composeapp.generated.resources.ic_quiket_logo

@Composable
internal fun QuiketTopBar(
    onNoteIconClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_quiket_logo),
            contentDescription = "Home Quiket Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(width = 90.dp, height = 28.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        TopIconButton(
            icon = Res.drawable.ic_home_note,
            contentDescription = "Home Quiket Note",
            onClick = onNoteIconClick,
        )
        TopIconButton(
            icon = Res.drawable.ic_alert,
            contentDescription = "Home Quiket Alert",
            onClick = {},
        )
    }
}

@Composable
private fun TopIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                this.onClick {
                    onClick()
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp),
        )
    }
}
