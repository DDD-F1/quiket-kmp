package com.f1.quiket.feature.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.f1.quiket.feature.login.presentation.KakaoAccountLinkRoute
import com.f1.quiket.feature.login.presentation.KakaoNicknameRoute
import com.f1.quiket.feature.login.presentation.LoginEmailRoute
import com.f1.quiket.feature.login.presentation.LoginRoute
import com.f1.quiket.feature.login.presentation.PasswordResetEmailVerificationRoute
import com.f1.quiket.feature.login.presentation.PasswordResetNewPasswordRoute
import com.f1.quiket.feature.login.presentation.SignUpCodeVerificationRoute
import com.f1.quiket.feature.login.presentation.SignUpEmailVerificationRoute
import com.f1.quiket.feature.login.presentation.SignUpNicknameRoute
import com.f1.quiket.feature.login.presentation.SignUpTermsRoute

fun NavHostController.navigateToLoginEmail() {
    navigate(LoginEmailDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToPasswordResetEmailVerification() {
    navigate(PasswordResetEmailVerificationDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSignUpEmailVerification() {
    navigate(SignUpEmailVerificationDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSignUpNickname() {
    navigate(SignUpNicknameDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSignUpTerms() {
    navigate(SignUpTermsDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSignUpCodeVerification() {
    navigate(SignUpCodeVerificationDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToPasswordResetNewPassword() {
    navigate(PasswordResetNewPasswordDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToKakaoNickname() {
    navigate(KakaoNicknameDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToKakaoAccountLink() {
    navigate(KakaoAccountLinkDestination.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToLoginEmailAfterPasswordReset() {
    navigate(LoginEmailDestination.route) {
        popUpTo(LoginEmailDestination.route) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.loginGraph(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignUpEmailVerified: () -> Unit,
    onSignUpNicknameComplete: () -> Unit,
    onSignUpSubmitted: () -> Unit,
    onEmailLoginClick: () -> Unit,
    onKakaoNicknameRequired: () -> Unit,
    onKakaoAccountLinkRequired: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onPasswordResetEmailVerified: () -> Unit,
    onPasswordResetComplete: () -> Unit = onBackClick,
) {
    composable(route = LoginDestination.route) {
        LoginRoute(
            onLoginSuccess = onLoginSuccess,
            onEmailLoginClick = onEmailLoginClick,
            onBackClick = onBackClick,
            onSignUpClick = onSignUpClick,
            onKakaoNicknameRequired = onKakaoNicknameRequired,
            onKakaoAccountLinkRequired = onKakaoAccountLinkRequired,
        )
    }
    composable(route = LoginEmailDestination.route) {
        LoginEmailRoute(
            onBackClick = onBackClick,
            onForgotPasswordClick = onForgotPasswordClick,
            onLoginSuccess = onLoginSuccess,
            onEmailVerificationRequired = onSignUpSubmitted,
        )
    }
    composable(route = SignUpEmailVerificationDestination.route) {
        SignUpEmailVerificationRoute(
            onBackClick = onBackClick,
            onNextClick = onSignUpEmailVerified,
        )
    }
    composable(route = SignUpNicknameDestination.route) {
        SignUpNicknameRoute(
            onBackClick = onBackClick,
            onNextClick = onSignUpNicknameComplete,
        )
    }
    composable(route = SignUpTermsDestination.route) {
        SignUpTermsRoute(
            onBackClick = onBackClick,
            onSignupSubmitted = onSignUpSubmitted,
        )
    }
    composable(route = SignUpCodeVerificationDestination.route) {
        SignUpCodeVerificationRoute(
            onBackClick = onBackClick,
            onVerificationComplete = onLoginSuccess,
        )
    }
    composable(route = PasswordResetEmailVerificationDestination.route) {
        PasswordResetEmailVerificationRoute(
            onCloseClick = onBackClick,
            onVerificationComplete = onPasswordResetEmailVerified,
        )
    }
    composable(route = PasswordResetNewPasswordDestination.route) {
        PasswordResetNewPasswordRoute(
            onCloseClick = onBackClick,
            onCompleteClick = onPasswordResetComplete,
        )
    }
    composable(route = KakaoNicknameDestination.route) {
        KakaoNicknameRoute(
            onBackClick = onBackClick,
            onComplete = onLoginSuccess,
        )
    }
    composable(route = KakaoAccountLinkDestination.route) {
        KakaoAccountLinkRoute(
            onBackClick = onBackClick,
            onComplete = onLoginSuccess,
        )
    }
}
