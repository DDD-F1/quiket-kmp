package com.f1.quiket.core.designsystem.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.f1.quiket.core.designsystem.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

val QuiketTypography = Typography(

    // Header
    headlineLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 36.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 36.sp * 1.4
    ),

    // Title1
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 28.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 28.sp * 1.4
    ),

    // Title 2
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontSize = 24.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 24.sp * 1.4
    ),

    // Title 3
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = 20.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 20.sp * 1.4
    ),

    // Body 1
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontSize = 18.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 18.sp * 1.5
    ),

    // Body 2
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontSize = 16.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 16.sp * 1.5
    ),

    // Body 3
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontSize = 14.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 14.sp * 1.5
    ),

    // Caption Medium -> labelMedium
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 12.sp * 1.5
    ),
    // Caption Regular -> labelSmall
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = (-0.004).em,
        lineHeight = 12.sp * 1.5
    )
)

@Preview
@Composable
fun TypographyPreview() {
    QuiketTheme {
        Column {
            Text("Header", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
            Text("Title1", style = MaterialTheme.typography.titleLarge)
            Text("Title2", style = MaterialTheme.typography.titleMedium)
            Text("Title3", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text("Body", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text("Body2", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal))
            Text("Body3", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text("Caption1", style = MaterialTheme.typography.labelMedium)
            Text("Caption2", style = MaterialTheme.typography.labelSmall)
        }
    }
}
