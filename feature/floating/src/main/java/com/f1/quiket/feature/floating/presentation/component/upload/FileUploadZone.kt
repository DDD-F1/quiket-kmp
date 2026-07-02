package com.f1.quiket.feature.floating.presentation.component.upload

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1.quiket.core.designsystem.modifier.dashedBorder
import com.f1.quiket.core.designsystem.theme.Brown950
import com.f1.quiket.core.designsystem.theme.Gray100
import com.f1.quiket.core.designsystem.theme.Gray300
import com.f1.quiket.core.designsystem.theme.Gray400
import com.f1.quiket.core.designsystem.theme.Gray50
import com.f1.quiket.core.designsystem.theme.Gray500
import com.f1.quiket.core.designsystem.theme.Gray900
import com.f1.quiket.core.designsystem.theme.Gray950
import com.f1.quiket.core.designsystem.theme.Negative
import com.f1.quiket.core.designsystem.theme.Positive
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.core.designsystem.theme.White
import com.f1.quiket.feature.floating.R
import kotlinx.coroutines.delay

// ─── 모델 ─────────────────────────────────────────────────────────────────────

enum class UploadFileStatus { UPLOADING, COMPLETED, FAILED }

data class UploadFile(
    val id: Int,
    val uri: android.net.Uri,
    val name: String,
    val sizeLabel: String,
    val status: UploadFileStatus,
)

// ─── Zone ─────────────────────────────────────────────────────────────────────

@Composable
fun FileUploadZone(
    files: SnapshotStateList<UploadFile>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        uris.forEach { uri ->
            val name = getFileDisplayName(context, uri)
            val sizeLabel = getFileSizeLabel(context, uri)
            files.add(
                UploadFile(
                    id = System.nanoTime().toInt(),
                    uri = uri,
                    name = name,
                    sizeLabel = sizeLabel,
                    status = UploadFileStatus.UPLOADING,
                )
            )
        }
    }

    // 각 UPLOADING 파일에 대해 업로드 시뮬레이션
    files.forEach { file ->
        if (file.status == UploadFileStatus.UPLOADING) {
            key(file.id) {
                LaunchedEffect(file.id) {
                    delay(1_500)
                    val idx = files.indexOfFirst { it.id == file.id }
                    if (idx >= 0) {
                        files[idx] = files[idx].copy(status = UploadFileStatus.COMPLETED)
                    }
                }
            }
        }
    }

    Column(modifier = modifier) {
        FileDropZoneEmpty(onClick = { pdfPicker.launch("application/pdf") })

        if (files.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(files) { index, file ->
                    PdfCard(
                        file = file,
                        onRemove = { files.removeAt(index) },
                        onRetry = {
                            files[index] = file.copy(status = UploadFileStatus.UPLOADING)
                        },
                    )
                }
            }
        }
    }
}

// ─── 빈 상태 드롭존 (HomeEmptySubjectButton 스타일) ──────────────────────────

@Composable
private fun FileDropZoneEmpty(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Gray50)
            .dashedBorder(
                color = Gray300,
                strokeWidth = 1.5.dp,
                cornerRadius = 16.dp,
                dashLength = 5.dp,
                gapLength = 4.dp,
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // PDF 아이콘 + 흰 원형 배경
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White),
                )
                Icon(
                    painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_upload_pdf),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "업로드할 파일 선택해주세요",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Gray900,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PDF 최대 50MB",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400,
            )
        }
    }
}

// ─── PDF 카드 ─────────────────────────────────────────────────────────────────

@Composable
fun PdfCard(
    file: UploadFile,
    onRemove: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .border(1.dp, Gray100, RoundedCornerShape(8.dp))
            .padding(bottom = 10.dp)
        ,
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // PDF 아이콘
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gray50),
            )
            Icon(
                painter = painterResource(R.drawable.ic_upload_pdf),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 파일 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Gray950,
                ),
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = file.sizeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(color = Gray500),
                )
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelSmall.copy(color = Gray400),
                )
                when (file.status) {
                    UploadFileStatus.UPLOADING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = Gray400,
                            )
                            Text(
                                text = "업로드 중",
                                style = MaterialTheme.typography.labelSmall.copy(color = Gray400),
                            )
                        }
                    }

                    UploadFileStatus.COMPLETED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_upload_ok),
                                contentDescription = null,
                                tint = Positive,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "업로드 완료",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Positive,
                                    fontWeight = FontWeight.Medium,
                                )
                            )
                        }
                    }

                    UploadFileStatus.FAILED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(com.f1.quiket.core.designsystem.R.drawable.ic_upload_fail),
                                contentDescription = null,
                                tint = Negative,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "업로드 실패",
                                style = MaterialTheme.typography.labelSmall.copy(color = Negative),
                            )
                        }
                    }
                }
            }
        }

        // 삭제 아이콘 (완료: 휴지통 / 그 외: X)
        if (file.status == UploadFileStatus.COMPLETED) {
            Icon(
                painter = painterResource(com.f1.quiket.feature.floating.R.drawable.ic_detail_remove),
                contentDescription = "삭제",
                tint = Gray400,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onRemove() },
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "취소",
                tint = Gray400,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onRemove() },
            )
        }
    } // end Row

    when (file.status) {
        UploadFileStatus.UPLOADING -> LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(start = 15.dp, end = 15.dp),
            color = Brown950,
            trackColor = Gray100,
            strokeCap = StrokeCap.Round,
            gapSize = (-10).dp,
        )
        UploadFileStatus.COMPLETED -> LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(start = 15.dp, end = 15.dp),
            color = Brown950,
            trackColor = Gray100,
            strokeCap = StrokeCap.Round,
            gapSize = (-10).dp,
            drawStopIndicator = {},
        )
        UploadFileStatus.FAILED -> LinearProgressIndicator(
            progress = { 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(start = 15.dp, end = 15.dp),
            color = Brown950,
            trackColor = Gray100,
            strokeCap = StrokeCap.Round,
            gapSize = (-10).dp,
            drawStopIndicator = {},
        )
    } // end when
    } // end Column
} // end PdfCard

// ─── 유틸 ─────────────────────────────────────────────────────────────────────

private fun getFileDisplayName(context: android.content.Context, uri: Uri): String {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: uri.lastPathSegment ?: "파일.pdf"
}

private fun getFileSizeLabel(context: android.content.Context, uri: Uri): String {
    val bytes = runCatching {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
    }.getOrNull() ?: 0L
    return when {
        bytes >= 1_048_576 -> "%.1fMB".format(bytes / 1_048_576f)
        bytes >= 1_024 -> "${bytes / 1_024}KB"
        else -> "${bytes}B"
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "파일 업로드 — 빈 상태")
@Composable
private fun FileUploadZoneEmptyPreview() {
    QuiketTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(16.dp),
        ) {
            FileUploadZone(
                files = remember { SnapshotStateList() },
            )
        }
    }
}

@Preview(showBackground = true, name = "파일 업로드 — 파일 상태")
@Composable
private fun FileUploadZoneWithFilesPreview() {
    val files = remember {
        SnapshotStateList<UploadFile>().also {
            it.add(
                UploadFile(
                    id = 1,
                    uri = android.net.Uri.EMPTY,
                    name = "SQLD_정리노트.pdf",
                    sizeLabel = "2.4MB",
                    status = UploadFileStatus.COMPLETED
                )
            )
            it.add(
                UploadFile(
                    id = 2,
                    uri = android.net.Uri.EMPTY,
                    name = "데이터모델링_강의자료.pdf",
                    sizeLabel = "1.1MB",
                    status = UploadFileStatus.FAILED
                )
            )
            it.add(
                UploadFile(
                    id = 3,
                    uri = android.net.Uri.EMPTY,
                    name = "SQL활용_실습.pdf",
                    sizeLabel = "890KB",
                    status = UploadFileStatus.UPLOADING
                )
            )
        }
    }
    QuiketTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(16.dp),
        ) {
            FileUploadZone(files = files)
        }
    }
}