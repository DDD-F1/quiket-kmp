package com.f1.quiket.feature.mypage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.f1.quiket.feature.mypage.presentation.AccountSettingRoute
import com.f1.quiket.feature.mypage.presentation.AlarmSettingRoute
import com.f1.quiket.feature.mypage.presentation.InquiryRoute
import com.f1.quiket.feature.mypage.presentation.MyPageRoute
import com.f1.quiket.feature.mypage.presentation.MyPageSettingRoute
import com.f1.quiket.feature.mypage.presentation.PrivacyPolicyScreen
import com.f1.quiket.feature.mypage.presentation.TermsScreen

fun NavGraphBuilder.myPageGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
) {
    composable(route = MyPageDestination.route) {
        MyPageRoute(
            onNavigateToSettings = {
                navController.navigate(MyPageSettingDestination.route)
            },
        )
    }
    composable(route = MyPageSettingDestination.route) {
        MyPageSettingRoute(
            onNavigateBack = { navController.popBackStack() },
            onLogout = onLogout,
            onNavigateToAccountSetting = { navController.navigate(AccountSettingDestination.route) },
            onNavigateToAlarmSetting = { navController.navigate(AlarmSettingDestination.route) },
            onNavigateToInquiry = { navController.navigate(InquiryDestination.route) },
            onNavigateToTerms = { navController.navigate(TermsDestination.route) },
            onNavigateToPrivacyPolicy = { navController.navigate(PrivacyPolicyDestination.route) },
        )
    }
    composable(route = AccountSettingDestination.route) {
        AccountSettingRoute(
            onNavigateBack = { navController.popBackStack() },
            onAccountDeleted = onLogout,
        )
    }
    composable(route = AlarmSettingDestination.route) {
        AlarmSettingRoute(
            onNavigateBack = { navController.popBackStack() },
        )
    }
    composable(route = InquiryDestination.route) {
        InquiryRoute(
            onNavigateBack = { navController.popBackStack() },
        )
    }
    composable(route = TermsDestination.route) {
        TermsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable(route = PrivacyPolicyDestination.route) {
        PrivacyPolicyScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}