package com.f1.quiket.composeapp

import org.koin.compose.koinInject
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.f1.quiket.composeapp.auth.domain.model.AppleLoginResult
import com.f1.quiket.composeapp.auth.domain.model.AuthTokenData
import com.f1.quiket.composeapp.auth.domain.model.AuthException
import com.f1.quiket.composeapp.auth.domain.model.KakaoLoginResult
import com.f1.quiket.composeapp.auth.presentation.AuthStateHolder
import com.f1.quiket.composeapp.designsystem.QuiketBrown50
import com.f1.quiket.composeapp.designsystem.QuiketGray700
import com.f1.quiket.composeapp.designsystem.QuiketTheme
import com.f1.quiket.composeapp.login.PasswordResetDraft
import com.f1.quiket.composeapp.login.OAuthAuthDraft
import com.f1.quiket.composeapp.login.SignupDraft
import com.f1.quiket.composeapp.login.OAuthAccountLinkRoute
import com.f1.quiket.composeapp.login.OAuthNicknameRoute
import com.f1.quiket.composeapp.login.LoginEmailScreen
import com.f1.quiket.composeapp.login.LoginScreen
import com.f1.quiket.composeapp.login.PasswordResetEmailVerificationRoute
import com.f1.quiket.composeapp.login.PasswordResetNewPasswordRoute
import com.f1.quiket.composeapp.login.SignUpCodeVerificationRoute
import com.f1.quiket.composeapp.login.SignUpCredentialsRoute
import com.f1.quiket.composeapp.login.SignUpNicknameRoute
import com.f1.quiket.composeapp.login.SignUpTermsRoute
import com.f1.quiket.composeapp.login.SignUpTermsState
import com.f1.quiket.composeapp.login.toOAuthDraft
import com.f1.quiket.composeapp.main.MainScreen
import com.f1.quiket.composeapp.network.toUserFacingMessage
import com.f1.quiket.composeapp.onboarding.OnboardingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import quiket.composeapp.generated.resources.Res
import quiket.composeapp.generated.resources.img_splash_acorn
import quiket.composeapp.generated.resources.img_splash_character
import quiket.composeapp.generated.resources.img_splash_note
import quiket.composeapp.generated.resources.img_splash_pad_pencil
import quiket.composeapp.generated.resources.img_splash_spark
import quiket.composeapp.generated.resources.img_splash_word_card
import quiket.composeapp.generated.resources.logo_splash
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.TimeSource

typealias KakaoLoginCompletion = (accessToken: String?, errorMessage: String?) -> Unit
typealias KakaoLoginLauncher = (KakaoLoginCompletion) -> Unit
typealias AppleLoginCompletion = (
    identityToken: String?,
    authorizationCode: String?,
    fullName: String?,
    errorMessage: String?,
) -> Unit
typealias AppleLoginLauncher = (AppleLoginCompletion) -> Unit

private const val SplashAnimationDurationMillis = 5_050L

@Composable
fun QuiketApp(
    kakaoLoginLauncher: KakaoLoginLauncher = { completion ->
        completion(null, "카카오 로그인을 사용할 수 없습니다.")
    },
    appleLoginLauncher: AppleLoginLauncher = { completion ->
        completion(null, null, null, "Apple 로그인을 사용할 수 없습니다.")
    },
    isEmailLoginAvailable: Boolean = false,
    isAppleLoginAvailable: Boolean = false,
) {
    var route by remember { mutableStateOf(Route.Loading) }
    var loggedInNickname by remember { mutableStateOf<String?>(null) }
    var signupDraft by remember { mutableStateOf(SignupDraft()) }
    var signUpCodeBackRoute by remember { mutableStateOf(Route.SignUpTerms) }
    var oauthAuthDraft by remember { mutableStateOf(OAuthAuthDraft()) }
    var isKakaoLoading by remember { mutableStateOf(false) }
    var kakaoErrorMessage by remember { mutableStateOf<String?>(null) }
    var isAppleLoading by remember { mutableStateOf(false) }
    var appleErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordResetDraft by remember { mutableStateOf(PasswordResetDraft()) }
    var isSignupSubmitting by remember { mutableStateOf(false) }
    var signupSubmitErrorMessage by remember { mutableStateOf<String?>(null) }
    var mainHomeGuideCompleted by remember { mutableStateOf(false) }
    val authStateHolder = koinInject<AuthStateHolder>()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val splashStartedAt = TimeSource.Monotonic.markNow()

        suspend fun awaitSplashMinimumDuration() {
            val remainingMillis = SplashAnimationDurationMillis - splashStartedAt.elapsedNow().inWholeMilliseconds
            if (remainingMillis > 0) {
                delay(remainingMillis)
            }
        }

        val session = authStateHolder.readSession()
        mainHomeGuideCompleted = session.homeGuideCompleted
        val fallbackRoute = if (session.onboardingCompleted) Route.Login else Route.Onboarding
        if (!session.isLoggedIn) {
            loggedInNickname = null
            awaitSplashMinimumDuration()
            route = fallbackRoute
            return@LaunchedEffect
        }

        val meResult = runCatching { authStateHolder.getCurrentUser() }
        awaitSplashMinimumDuration()
        meResult
            .onSuccess { user ->
                loggedInNickname = user.nickname
                route = Route.Main
            }
            .onFailure { error ->
                if (error is AuthException && error.isUnauthorized) {
                    authStateHolder.clearAuth()
                }
                loggedInNickname = null
                route = fallbackRoute
            }
    }

    suspend fun completeLogin(tokenData: AuthTokenData, fallbackNickname: String = "사용자") {
        val completedLogin = authStateHolder.completeLogin(
            tokenData = tokenData,
            fallbackNickname = fallbackNickname,
        )
        mainHomeGuideCompleted = completedLogin.homeGuideCompleted
        loggedInNickname = completedLogin.nickname
        signupDraft = SignupDraft()
        signUpCodeBackRoute = Route.SignUpTerms
        oauthAuthDraft = OAuthAuthDraft()
        kakaoErrorMessage = null
        appleErrorMessage = null
        route = Route.Main
    }

    suspend fun handleServerKakaoLogin(kakaoAccessToken: String) {
        when (val result = authStateHolder.kakaoLogin(kakaoAccessToken = kakaoAccessToken)) {
            is KakaoLoginResult.LoggedIn -> {
                completeLogin(result.tokenData)
            }

            is KakaoLoginResult.NicknameRequired -> {
                oauthAuthDraft = result.data.toOAuthDraft()
                kakaoErrorMessage = null
                route = Route.KakaoNickname
            }

            is KakaoLoginResult.AccountLinkRequired -> {
                oauthAuthDraft = result.data.toOAuthDraft()
                kakaoErrorMessage = null
                route = Route.KakaoAccountLink
            }

            is KakaoLoginResult.Failure -> {
                kakaoErrorMessage = result.message
            }
        }
    }

    fun requestKakaoLogin() {
        if (isKakaoLoading) return

        isKakaoLoading = true
        kakaoErrorMessage = null
        appleErrorMessage = null
        runCatching {
            kakaoLoginLauncher { accessToken, errorMessage ->
                coroutineScope.launch {
                    if (!accessToken.isNullOrBlank()) {
                        runCatching {
                            handleServerKakaoLogin(accessToken)
                        }.onFailure { error ->
                            kakaoErrorMessage = error.toUserFacingMessage("카카오 로그인에 실패했습니다.")
                        }
                    } else {
                        kakaoErrorMessage = errorMessage ?: "카카오 로그인에 실패했습니다."
                    }
                    isKakaoLoading = false
                }
            }
        }.onFailure { error ->
            kakaoErrorMessage = error.toUserFacingMessage("카카오 로그인에 실패했습니다.")
            isKakaoLoading = false
        }
    }

    suspend fun handleServerAppleLogin(
        identityToken: String,
        authorizationCode: String?,
        fullName: String?,
    ) {
        when (
            val result = authStateHolder.appleLogin(
                identityToken = identityToken,
                authorizationCode = authorizationCode,
                fullName = fullName,
            )
        ) {
            is AppleLoginResult.LoggedIn -> {
                completeLogin(result.tokenData)
            }

            is AppleLoginResult.NicknameRequired -> {
                oauthAuthDraft = result.data.toOAuthDraft()
                appleErrorMessage = null
                route = Route.AppleNickname
            }

            is AppleLoginResult.AccountLinkRequired -> {
                oauthAuthDraft = result.data.toOAuthDraft()
                appleErrorMessage = null
                route = Route.AppleAccountLink
            }

            is AppleLoginResult.Failure -> {
                appleErrorMessage = result.message
            }
        }
    }

    fun requestAppleLogin() {
        if (!isAppleLoginAvailable || isAppleLoading) return

        isAppleLoading = true
        appleErrorMessage = null
        kakaoErrorMessage = null
        runCatching {
            appleLoginLauncher { identityToken, authorizationCode, fullName, errorMessage ->
                coroutineScope.launch {
                    if (!identityToken.isNullOrBlank()) {
                        runCatching {
                            handleServerAppleLogin(
                                identityToken = identityToken,
                                authorizationCode = authorizationCode?.takeIf { it.isNotBlank() },
                                fullName = fullName?.takeIf { it.isNotBlank() },
                            )
                        }.onFailure { error ->
                            appleErrorMessage = error.toUserFacingMessage("Apple 로그인에 실패했습니다.")
                        }
                    } else {
                        appleErrorMessage = errorMessage ?: "Apple 로그인에 실패했습니다."
                    }
                    isAppleLoading = false
                }
            }
        }.onFailure { error ->
            appleErrorMessage = error.toUserFacingMessage("Apple 로그인에 실패했습니다.")
            isAppleLoading = false
        }
    }

    QuiketTheme {
        when (route) {
            Route.Loading -> {
                LoadingScreen()
            }

            Route.Onboarding -> {
                OnboardingScreen(
                    onComplete = {
                        coroutineScope.launch {
                            authStateHolder.saveOnboardingCompleted()
                            route = Route.Login
                        }
                    },
                    onSkip = {
                        coroutineScope.launch {
                            authStateHolder.saveOnboardingCompleted()
                            route = Route.Login
                        }
                    },
                )
            }

            Route.Login -> {
                LoginScreen(
                    onBackClick = { route = Route.Onboarding },
                    onQuiketLoginClick = { route = Route.EmailLogin },
                    onKakaoLoginClick = { requestKakaoLogin() },
                    onAppleLoginClick = { requestAppleLogin() },
                    onSignUpClick = {
                        signupSubmitErrorMessage = null
                        signUpCodeBackRoute = Route.SignUpTerms
                        route = Route.SignUpCredentials
                    },
                    isKakaoLoading = isKakaoLoading,
                    kakaoErrorMessage = kakaoErrorMessage,
                    isQuiketLoginVisible = isEmailLoginAvailable,
                    isSignUpVisible = false,
                    isAppleLoginVisible = isAppleLoginAvailable,
                    isAppleLoading = isAppleLoading,
                    appleErrorMessage = appleErrorMessage,
                )
            }

            Route.EmailLogin -> {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var isPasswordVisible by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                var loginErrorMessage by remember { mutableStateOf<String?>(null) }

                LoginEmailScreen(
                    email = email,
                    password = password,
                    isPasswordVisible = isPasswordVisible,
                    isLoginEnabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    onEmailChange = {
                        email = it
                        loginErrorMessage = null
                    },
                    onPasswordChange = {
                        password = it
                        loginErrorMessage = null
                    },
                    onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                    onBackClick = { route = Route.Login },
                    onForgotPasswordClick = {
                        passwordResetDraft = PasswordResetDraft(email = email.trim())
                        route = Route.PasswordResetEmailVerification
                    },
                    onLoginClick = {
                        if (!isLoading) {
                            isLoading = true
                            loginErrorMessage = null
                            coroutineScope.launch {
                                val trimmedEmail = email.trim()
                                runCatching {
                                    val tokenData = authStateHolder.login(
                                        email = trimmedEmail,
                                        password = password,
                                    )
                                    completeLogin(tokenData)
                                }.onFailure { error ->
                                    if (error is AuthException) {
                                        when (error.code) {
                                            "AUTH_INVALID_CREDENTIALS", "AUTH_LOGIN_FAILED" -> {
                                                val failedCount = error.failedLoginCount
                                                loginErrorMessage = if (failedCount != null) {
                                                    "비밀번호를 다시 입력해주세요 ($failedCount/5)"
                                                } else {
                                                    error.toUserFacingMessage("로그인에 실패했습니다.")
                                                }
                                            }

                                            "AUTH_ACCOUNT_LOCKED" -> {
                                                val resetEmail = error.email?.takeIf { it.isNotBlank() } ?: trimmedEmail
                                                passwordResetDraft = PasswordResetDraft(
                                                    email = resetEmail,
                                                    verificationCode = "",
                                                    resetCodeSent = error.resetCodeSent == true,
                                                )
                                                loginErrorMessage = null
                                                route = Route.PasswordResetEmailVerification
                                            }

                                            "AUTH_EMAIL_NOT_VERIFIED" -> {
                                                val verificationEmail = error.email?.takeIf { it.isNotBlank() } ?: trimmedEmail
                                                signupDraft = signupDraft.copy(email = verificationEmail)
                                                signUpCodeBackRoute = Route.EmailLogin
                                                runCatching {
                                                    authStateHolder.resendEmailVerification(verificationEmail)
                                                }
                                                loginErrorMessage = null
                                                route = Route.SignUpCodeVerification
                                            }

                                            else -> {
                                                loginErrorMessage = error.toUserFacingMessage("로그인에 실패했습니다.")
                                            }
                                        }
                                    } else {
                                        loginErrorMessage = error.toUserFacingMessage("로그인에 실패했습니다.")
                                    }
                                }
                                isLoading = false
                            }
                        }
                    },
                    passwordErrorMessage = loginErrorMessage,
                    buttonText = if (isLoading) "로그인 중..." else "로그인",
                )
            }

            Route.PasswordResetEmailVerification -> {
                PasswordResetEmailVerificationRoute(
                    draft = passwordResetDraft,
                    onCloseClick = { route = Route.EmailLogin },
                    onVerificationComplete = { nextDraft ->
                        passwordResetDraft = nextDraft
                        route = Route.PasswordResetNewPassword
                    },
                )
            }

            Route.PasswordResetNewPassword -> {
                PasswordResetNewPasswordRoute(
                    draft = passwordResetDraft,
                    onCloseClick = { route = Route.EmailLogin },
                    onCompleteClick = {
                        passwordResetDraft = PasswordResetDraft()
                        route = Route.EmailLogin
                    },
                )
            }

            Route.KakaoNickname -> {
                OAuthNicknameRoute(
                    draft = oauthAuthDraft,
                    providerName = "카카오",
                    onBackClick = {
                        oauthAuthDraft = OAuthAuthDraft()
                        route = Route.Login
                    },
                    onComplete = { tokenData ->
                        coroutineScope.launch {
                            completeLogin(tokenData, fallbackNickname = oauthAuthDraft.suggestedNickname ?: "사용자")
                        }
                    },
                    onCompleteNickname = authStateHolder::completeKakaoNickname,
                )
            }

            Route.KakaoAccountLink -> {
                OAuthAccountLinkRoute(
                    draft = oauthAuthDraft,
                    providerName = "카카오",
                    onBackClick = {
                        oauthAuthDraft = OAuthAuthDraft()
                        route = Route.Login
                    },
                    onComplete = { tokenData ->
                        coroutineScope.launch {
                            completeLogin(tokenData)
                        }
                    },
                    onLinkAccount = authStateHolder::linkKakaoAccount,
                )
            }

            Route.AppleNickname -> {
                OAuthNicknameRoute(
                    draft = oauthAuthDraft,
                    providerName = "Apple",
                    onBackClick = {
                        oauthAuthDraft = OAuthAuthDraft()
                        route = Route.Login
                    },
                    onComplete = { tokenData ->
                        coroutineScope.launch {
                            completeLogin(tokenData, fallbackNickname = oauthAuthDraft.suggestedNickname ?: "사용자")
                        }
                    },
                    onCompleteNickname = authStateHolder::completeAppleNickname,
                )
            }

            Route.AppleAccountLink -> {
                OAuthAccountLinkRoute(
                    draft = oauthAuthDraft,
                    providerName = "Apple",
                    onBackClick = {
                        oauthAuthDraft = OAuthAuthDraft()
                        route = Route.Login
                    },
                    onComplete = { tokenData ->
                        coroutineScope.launch {
                            completeLogin(tokenData)
                        }
                    },
                    onLinkAccount = authStateHolder::linkAppleAccount,
                )
            }

            Route.SignUpCredentials -> {
                SignUpCredentialsRoute(
                    draft = signupDraft,
                    onBackClick = {
                        signupSubmitErrorMessage = null
                        route = Route.Login
                    },
                    onNextClick = { nextDraft ->
                        signupDraft = nextDraft
                        signupSubmitErrorMessage = null
                        route = Route.SignUpNickname
                    },
                )
            }

            Route.SignUpNickname -> {
                SignUpNicknameRoute(
                    draft = signupDraft,
                    onBackClick = { route = Route.SignUpCredentials },
                    onNextClick = { nextDraft ->
                        signupDraft = nextDraft
                        signupSubmitErrorMessage = null
                        route = Route.SignUpTerms
                    },
                )
            }

            Route.SignUpTerms -> {
                SignUpTermsRoute(
                    onBackClick = { route = Route.SignUpNickname },
                    onSubmitClick = { termsState ->
                        if (!isSignupSubmitting) {
                            val submitDraft = signupDraft.copy(
                                serviceTermsAgreed = termsState.serviceTermsAgreed,
                                privacyTermsAgreed = termsState.privacyTermsAgreed,
                                marketingTermsAgreed = termsState.marketingTermsAgreed,
                            )
                            signupDraft = submitDraft
                            signupSubmitErrorMessage = null
                            isSignupSubmitting = true
                            coroutineScope.launch {
                                runCatching {
                                    authStateHolder.signup(
                                        email = submitDraft.email,
                                        password = submitDraft.password,
                                        passwordConfirm = submitDraft.passwordConfirm,
                                        nickname = submitDraft.nickname,
                                    )
                                }.onSuccess {
                                    signUpCodeBackRoute = Route.SignUpTerms
                                    route = Route.SignUpCodeVerification
                                }.onFailure { error ->
                                    signupSubmitErrorMessage = error.toUserFacingMessage("회원가입에 실패했습니다.")
                                }
                                isSignupSubmitting = false
                            }
                        }
                    },
                    isSubmitting = isSignupSubmitting,
                    errorMessage = signupSubmitErrorMessage,
                    initialTermsState = signupDraft.toTermsState(),
                )
            }

            Route.SignUpCodeVerification -> {
                SignUpCodeVerificationRoute(
                    draft = signupDraft,
                    onBackClick = { route = signUpCodeBackRoute },
                    onVerificationComplete = { tokenData ->
                        coroutineScope.launch {
                            completeLogin(tokenData, fallbackNickname = signupDraft.nickname.ifBlank { "사용자" })
                            signupSubmitErrorMessage = null
                        }
                    },
                )
            }

            Route.Main -> {
                MainScreen(
                    nickname = loggedInNickname,
                    initialHomeGuideCompleted = mainHomeGuideCompleted,
                    onLogout = {
                        coroutineScope.launch {
                            authStateHolder.logout()
                            loggedInNickname = null
                            oauthAuthDraft = OAuthAuthDraft()
                            kakaoErrorMessage = null
                            appleErrorMessage = null
                            route = Route.Login
                        }
                    },
                    onProfileChanged = { profile ->
                        loggedInNickname = profile.nickname
                    },
                    onSessionExpired = {
                        coroutineScope.launch {
                            authStateHolder.clearAuth()
                            loggedInNickname = null
                            route = Route.Login
                        }
                    },
                )
            }
        }
    }
}

private enum class Route {
    Loading,
    Onboarding,
    Login,
    EmailLogin,
    PasswordResetEmailVerification,
    PasswordResetNewPassword,
    KakaoNickname,
    KakaoAccountLink,
    AppleNickname,
    AppleAccountLink,
    SignUpCredentials,
    SignUpNickname,
    SignUpTerms,
    SignUpCodeVerification,
    Main,
}

private fun SignupDraft.toTermsState(): SignUpTermsState =
    SignUpTermsState(
        serviceTermsAgreed = serviceTermsAgreed,
        privacyTermsAgreed = privacyTermsAgreed,
        marketingTermsAgreed = marketingTermsAgreed,
    )

@Composable
private fun LoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = QuiketBrown50,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            SplashAnimation(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * SplashAnimationTopRatio),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * SplashLogoTopRatio),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_splash),
                    contentDescription = "Quiket",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(SplashLogoWidth)
                        .height(SplashLogoHeight),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI 퀴즈로 채워지는\n나만의 도토리 창고",
                    color = QuiketGray700,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun SplashAnimation(
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashAnimationDurationMillis.toInt(),
                easing = LinearEasing,
            ),
        )
    }
    val frame = progress.value * SplashFrameCount

    BoxWithConstraints(modifier = modifier.size(SplashAnimationSize)) {
        val acornDrop = splashProgressBetween(frame, 25f, 30f)
        val acornRise = splashProgressBetween(frame, 30f, 70f)
        val acornReturn = splashProgressBetween(frame, 80f, 105f)
        val acornY = when {
            frame < 25f -> 500f
            frame < 30f -> splashLerp(500f, 583f, splashEaseOut(acornDrop))
            frame < 70f -> splashLerp(583f, 139f, splashEaseOut(acornRise))
            frame < 80f -> 139f
            else -> splashLerp(139f, 583f, splashEaseIn(acornReturn))
        }
        val acornRotation = when {
            frame < 25f -> -9.5f + (sin((frame / 5f) * PI).toFloat() * 9.5f)
            frame < 80f -> splashLerp(0f, 26f, splashProgressBetween(frame, 25f, 80f))
            else -> splashLerp(26f, 2f, acornReturn)
        }

        SplashImageLayer(
            resource = Res.drawable.img_splash_acorn,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 312f,
            assetHeight = 315f,
            centerX = 500f,
            centerY = acornY,
            rotation = acornRotation,
            scale = 0.99f,
            alpha = 1f - splashProgressBetween(frame, 100f, 108f),
        )

        val supportingScale = splashAppearScale(frame, startFrame = 100f)
        val supportingMove = splashEaseOut(splashProgressBetween(frame, 105f, 125f))
        SplashImageLayer(
            resource = Res.drawable.img_splash_word_card,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 145f,
            assetHeight = 120f,
            centerX = splashLerp(470f, 472f, supportingMove),
            centerY = splashLerp(322f, 268f, supportingMove),
            rotation = splashLerp(0f, 12f, supportingMove),
            scale = supportingScale,
            alpha = supportingScale.coerceIn(0f, 1f),
        )
        SplashImageLayer(
            resource = Res.drawable.img_splash_note,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 143f,
            assetHeight = 153f,
            centerX = splashLerp(327.8f, 263.8f, supportingMove),
            centerY = splashLerp(459.5f, 413.5f, supportingMove),
            rotation = splashLerp(0f, -12f, supportingMove),
            scale = supportingScale,
            alpha = supportingScale.coerceIn(0f, 1f),
        )
        SplashImageLayer(
            resource = Res.drawable.img_splash_pad_pencil,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 178f,
            assetHeight = 171f,
            centerX = splashLerp(723f, 754f, supportingMove),
            centerY = splashLerp(383f, 362f, supportingMove),
            rotation = splashLerp(0f, 13f, supportingMove),
            scale = supportingScale,
            alpha = supportingScale.coerceIn(0f, 1f),
        )

        val characterScale = splashAppearScale(frame, startFrame = 100f)
        SplashImageLayer(
            resource = Res.drawable.img_splash_character,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 436f,
            assetHeight = 369f,
            centerX = 544f,
            centerY = 536f,
            scale = characterScale,
            alpha = characterScale.coerceIn(0f, 1f),
        )

        val sparkScale = splashAppearScale(frame, startFrame = 100f)
        val sparkPulse = 0.55f + (sin((frame / 11f) * PI).toFloat() * 0.45f)
        SplashImageLayer(
            resource = Res.drawable.img_splash_spark,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 62f,
            assetHeight = 62f,
            centerX = 245.5f,
            centerY = 569f,
            scale = sparkScale,
            alpha = sparkScale.coerceIn(0f, 1f) * sparkPulse,
        )
        SplashImageLayer(
            resource = Res.drawable.img_splash_spark,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 62f,
            assetHeight = 62f,
            centerX = 617f,
            centerY = 269f,
            scale = sparkScale,
            alpha = sparkScale.coerceIn(0f, 1f) * (1f - sparkPulse * 0.45f),
        )
        SplashImageLayer(
            resource = Res.drawable.img_splash_spark,
            canvasWidth = maxWidth,
            canvasHeight = maxHeight,
            assetWidth = 62f,
            assetHeight = 62f,
            centerX = 828f,
            centerY = 527f,
            scale = sparkScale,
            alpha = sparkScale.coerceIn(0f, 1f) * sparkPulse,
        )
    }
}

@Composable
private fun SplashImageLayer(
    resource: DrawableResource,
    canvasWidth: Dp,
    canvasHeight: Dp,
    assetWidth: Float,
    assetHeight: Float,
    centerX: Float,
    centerY: Float,
    scale: Float,
    alpha: Float,
    rotation: Float = 0f,
) {
    if (alpha <= 0.01f || scale <= 0.01f) return

    val width = canvasWidth * (assetWidth / SplashCanvasSize)
    val height = canvasHeight * (assetHeight / SplashCanvasSize)
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .padding(0.dp)
            .size(width = width, height = height)
            .graphicsLayer {
                translationX = (canvasWidth * (centerX / SplashCanvasSize) - width / 2f).toPx()
                translationY = (canvasHeight * (centerY / SplashCanvasSize) - height / 2f).toPx()
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                this.alpha = alpha.coerceIn(0f, 1f)
            },
    )
}

private fun splashAppearScale(frame: Float, startFrame: Float): Float {
    if (frame < startFrame) return 0f
    val pop = splashEaseOut(splashProgressBetween(frame, startFrame, startFrame + 5f))
    return splashLerp(0f, 1.15f, pop)
}

private fun splashProgressBetween(value: Float, start: Float, end: Float): Float =
    ((value - start) / (end - start)).coerceIn(0f, 1f)

private fun splashLerp(start: Float, end: Float, progress: Float): Float =
    start + ((end - start) * progress.coerceIn(0f, 1f))

private fun splashEaseOut(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return 1f - ((1f - t) * (1f - t))
}

private fun splashEaseIn(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t
}

private const val SplashLogoTopRatio = 148.46667f / 800f
private const val SplashAnimationTopRatio = 171.5f / 800f
private const val SplashFrameCount = 300f
private const val SplashCanvasSize = 1000f
private val SplashLogoWidth = 132.dp
private val SplashLogoHeight = 41.5.dp
private val SplashAnimationSize = 486.dp
