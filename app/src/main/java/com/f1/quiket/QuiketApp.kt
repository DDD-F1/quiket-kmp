package com.f1.quiket

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.f1.quiket.core.designsystem.theme.QuiketTheme
import com.f1.quiket.feature.login.navigation.LoginDestination
import com.f1.quiket.feature.login.navigation.loginGraph
import com.f1.quiket.feature.login.navigation.navigateToLoginEmail
import com.f1.quiket.feature.login.navigation.navigateToLoginEmailAfterPasswordReset
import com.f1.quiket.feature.login.navigation.navigateToKakaoAccountLink
import com.f1.quiket.feature.login.navigation.navigateToKakaoNickname
import com.f1.quiket.feature.login.navigation.navigateToPasswordResetEmailVerification
import com.f1.quiket.feature.login.navigation.navigateToPasswordResetNewPassword
import com.f1.quiket.feature.login.navigation.navigateToSignUpCodeVerification
import com.f1.quiket.feature.login.navigation.navigateToSignUpEmailVerification
import com.f1.quiket.feature.login.navigation.navigateToSignUpNickname
import com.f1.quiket.feature.login.navigation.navigateToSignUpTerms
import com.f1.quiket.feature.main.navigation.MainDestination
import com.f1.quiket.feature.main.presentation.MainRoute
import com.f1.quiket.feature.onboarding.navigation.OnboardingDestination
import com.f1.quiket.feature.onboarding.navigation.onboardingGraph
import com.f1.quiket.feature.splash.navigation.SplashDestination
import com.f1.quiket.feature.splash.navigation.splashGraph
import kotlinx.coroutines.launch

@Composable
fun QuiketApp() {
    QuiketTheme {
        val context = LocalContext.current.applicationContext
        val navController = rememberNavController()
        val coroutineScope = rememberCoroutineScope()
        val appSessionViewModel: AppSessionViewModel = hiltViewModel()
        val sessionState by appSessionViewModel.sessionState.collectAsStateWithLifecycle()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRootRoute = backStackEntry?.destination?.route

        LaunchedEffect(sessionState, currentRootRoute) {
            if (
                sessionState == AppSessionState.SignedOut &&
                currentRootRoute == MainDestination.route
            ) {
                navigateToRoot(navController, LoginDestination.route, MainDestination.route)
            }
        }

        NavHost(
            navController = navController,
            startDestination = SplashDestination.route,
        ) {
            splashGraph(
                onDecideNext = {
                    coroutineScope.launch {
                        val nextRoute = when {
                            !context.isOnboardingCompleted() -> OnboardingDestination.route
                            appSessionViewModel.hasValidSession() -> MainDestination.route
                            else -> LoginDestination.route
                        }
                        navigateToRoot(navController, nextRoute, SplashDestination.route)
                    }
                },
            )

            onboardingGraph(
                onComplete = {
                    context.setOnboardingCompleted()
                    navigateToRoot(navController, LoginDestination.route, OnboardingDestination.route)
                },
            )

            loginGraph(
                onBackClick = {
                    navController.navigateUp()
                },
                onEmailLoginClick = {
                    navController.navigateToLoginEmail()
                },
                onSignUpClick = {
                    navController.navigateToSignUpEmailVerification()
                },
                onSignUpEmailVerified = {
                    navController.navigateToSignUpNickname()
                },
                onSignUpNicknameComplete = {
                    navController.navigateToSignUpTerms()
                },
                onSignUpSubmitted = {
                    navController.navigateToSignUpCodeVerification()
                },
                onKakaoNicknameRequired = {
                    navController.navigateToKakaoNickname()
                },
                onKakaoAccountLinkRequired = {
                    navController.navigateToKakaoAccountLink()
                },
                onForgotPasswordClick = {
                    navController.navigateToPasswordResetEmailVerification()
                },
                onPasswordResetEmailVerified = {
                    navController.navigateToPasswordResetNewPassword()
                },
                onPasswordResetComplete = {
                    navController.navigateToLoginEmailAfterPasswordReset()
                },
                onLoginSuccess = {
                    navigateToRoot(navController, MainDestination.route, LoginDestination.route)
                },
            )

            composable(route = MainDestination.route) {
                MainRoute(
                    onLogout = {
                        coroutineScope.launch {
                            appSessionViewModel.logout()
                            navigateToRoot(navController, LoginDestination.route, MainDestination.route)
                        }
                    },
                )
            }
        }
    }
}

private const val QUIKET_PREFERENCES_NAME = "quiket_preferences"
private const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"

private fun Context.isOnboardingCompleted(): Boolean =
    getSharedPreferences(QUIKET_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .getBoolean(ONBOARDING_COMPLETED_KEY, false)

private fun Context.setOnboardingCompleted() {
    getSharedPreferences(QUIKET_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(ONBOARDING_COMPLETED_KEY, true)
        .apply()
}

private fun navigateToRoot(
    navController: NavHostController,
    route: String,
    popUpToRoute: String,
) {
    if (navController.currentDestination?.route == route) return

    navController.navigate(route) {
        popUpTo(popUpToRoute) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
