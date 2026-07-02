package com.f1.quiket.composeapp.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.pretendard_bold
import quiket.composeapp.generated.resources.pretendard_medium
import quiket.composeapp.generated.resources.pretendard_regular
import quiket.composeapp.generated.resources.pretendard_semibold

private val QuiketLightColorScheme = lightColorScheme(
    primary = QuiketBrown950,
    secondary = QuiketOrange500,
    surface = QuiketGray100,
    background = QuiketBrown50,
)

@Composable
private fun quiketTypography(): Typography {
    val pretendard = FontFamily(
        Font(Res.font.pretendard_regular, FontWeight.Normal),
        Font(Res.font.pretendard_medium, FontWeight.Medium),
        Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
    )

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = pretendard,
            fontSize = 36.sp,
            lineHeight = 50.4.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = pretendard,
            fontSize = 28.sp,
            lineHeight = 39.2.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = pretendard,
            fontSize = 28.sp,
            lineHeight = 39.2.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = pretendard,
            fontSize = 24.sp,
            lineHeight = 33.6.sp,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = pretendard,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = pretendard,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = pretendard,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = pretendard,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
    )
}

@Composable
fun QuiketTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = QuiketLightColorScheme,
        typography = quiketTypography(),
        content = content,
    )
}
