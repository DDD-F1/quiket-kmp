package com.f1.quiket.composeapp.subject

import com.f1.quiket.composeapp.subject.domain.model.*

import org.koin.compose.koinInject
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketBrown950
import com.f1.quiket.composeapp.designsystem.QuiketGray100
import com.f1.quiket.composeapp.designsystem.QuiketGray300
import com.f1.quiket.composeapp.designsystem.QuiketGray400
import com.f1.quiket.composeapp.designsystem.QuiketGray50
import com.f1.quiket.composeapp.designsystem.QuiketGray600
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketGray800
import com.f1.quiket.composeapp.designsystem.QuiketGray950
import com.f1.quiket.composeapp.designsystem.QuiketNegative
import com.f1.quiket.composeapp.designsystem.QuiketPrimaryButton
import com.f1.quiket.composeapp.designsystem.QuiketTextField
import com.f1.quiket.composeapp.designsystem.QuiketWhite
import com.f1.quiket.composeapp.subject.presentation.TextLectureUploadStateHolder
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import com.f1.quiket.core.designsystem.resources.Res as DesignSystemRes
import com.f1.quiket.core.designsystem.resources.ic_acorn
import com.f1.quiket.core.designsystem.resources.ic_info
import com.f1.quiket.core.designsystem.resources.ic_qring_profile
import com.f1.quiket.feature.subject.resources.Res
import com.f1.quiket.feature.subject.resources.ic_upload_image
import com.f1.quiket.feature.subject.resources.ic_upload_ok
import com.f1.quiket.feature.subject.resources.ic_upload_pdf

@Composable
internal fun UploadTabRow(
    selectedTab: UploadTab,
    enabled: Boolean,
    onTabClick: (UploadTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        UploadTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .then(
                        if (enabled) {
                            Modifier.clickable(role = Role.Button) { onTabClick(tab) }
                        } else {
                            Modifier
                        },
                    ),
                color = if (selected) QuiketWhite else QuiketGray100,
                contentColor = if (selected) QuiketGray950 else QuiketGray800,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = tab.label,
                        color = if (selected) QuiketGray950 else QuiketGray800,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun PartClassifySection(
    selectedMethod: PartClassifyMethod,
    manualPartNames: List<String>,
    enabled: Boolean,
    onMethodChange: (PartClassifyMethod) -> Unit,
    onManualPartNamesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showManualDialog by remember { mutableStateOf(false) }

    if (showManualDialog) {
        ManualPartDialog(
            initialPartNames = manualPartNames,
            onDismiss = { showManualDialog = false },
            onApply = { names ->
                onManualPartNamesChange(names)
                showManualDialog = false
            },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "파트 분류 방법",
                color = QuiketGray950,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Image(
                painter = painterResource(DesignSystemRes.drawable.ic_info),
                contentDescription = "파트 분류 방법 안내",
                modifier = Modifier.size(16.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PartClassifyCard(
                title = "AI에게 맡기기",
                description = "자료를 자동으로 나눠요",
                selected = selectedMethod == PartClassifyMethod.Ai,
                enabled = enabled,
                onClick = { onMethodChange(PartClassifyMethod.Ai) },
                modifier = Modifier.weight(1f),
            )
            PartClassifyCard(
                title = "직접 분류하기",
                description = if (manualPartNames.isEmpty()) {
                    "파트명을 직접 정해요"
                } else {
                    "${manualPartNames.size}개 파트 입력됨"
                },
                selected = selectedMethod == PartClassifyMethod.Manual,
                enabled = enabled,
                onClick = { showManualDialog = true },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun PartClassifyCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (enabled) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = if (selected) QuiketBrown50 else QuiketGray50,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) QuiketBrown950 else QuiketGray100,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                color = if (enabled) QuiketGray950 else QuiketGray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = if (enabled) QuiketGray600 else QuiketGray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun ManualPartDialog(
    initialPartNames: List<String>,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    var partNames by remember(initialPartNames) {
        mutableStateOf(initialPartNames.ifEmpty { listOf("") })
    }
    val trimmedPartNames = partNames.map(String::trim).filter(String::isNotBlank)
    val canAdd = partNames.size < MaxManualPartCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clickable(onClick = {}),
                color = QuiketWhite,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "파트명 직접 입력",
                                color = QuiketGray950,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "분류하고 싶은 파트명을 순서대로 입력해주세요",
                                color = QuiketGray700,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = "닫기",
                            color = QuiketGray600,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button, onClick = onDismiss)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 336.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        partNames.forEachIndexed { index, partName ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(QuiketGray100),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = QuiketGray700,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                                QuiketTextField(
                                    value = partName,
                                    onValueChange = { value ->
                                        partNames = partNames.toMutableList().also { it[index] = value }
                                    },
                                    hint = "파트명을 입력해주세요",
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        keyboardType = KeyboardType.Text,
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "삭제",
                                    color = if (partNames.size > 1) QuiketNegative else QuiketGray400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (partNames.size > 1) {
                                                Modifier.clickable(role = Role.Button) {
                                                    partNames = partNames.toMutableList().also { it.removeAt(index) }
                                                }
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (canAdd) {
                                    Modifier.clickable(role = Role.Button) { partNames = partNames + "" }
                                } else {
                                    Modifier
                                },
                            ),
                        color = QuiketGray50,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, QuiketGray300),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+ 파트 추가하기 (${partNames.size}/$MaxManualPartCount)",
                                color = if (canAdd) QuiketGray700 else QuiketGray400,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button, onClick = onDismiss),
                            color = QuiketWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, QuiketBrown950),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "취소",
                                    color = QuiketGray950,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        QuiketPrimaryButton(
                            text = "적용",
                            enabled = trimmedPartNames.isNotEmpty(),
                            onClick = { onApply(trimmedPartNames) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
