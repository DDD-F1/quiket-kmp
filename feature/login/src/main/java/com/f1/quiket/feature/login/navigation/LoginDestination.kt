package com.f1.quiket.feature.login.navigation

import com.f1.quiket.core.navigation.QuiketDestination

data object LoginDestination : QuiketDestination {
    override val route: String = "login"
}

internal data object LoginEmailDestination : QuiketDestination {
    override val route: String = "login/email"
}

internal data object SignUpEmailVerificationDestination : QuiketDestination {
    override val route: String = "login/signup/email-verification"
}

internal data object SignUpNicknameDestination : QuiketDestination {
    override val route: String = "login/signup/nickname"
}

internal data object SignUpTermsDestination : QuiketDestination {
    override val route: String = "login/signup/terms"
}

internal data object SignUpCodeVerificationDestination : QuiketDestination {
    override val route: String = "login/signup/code-verification"
}

internal data object PasswordResetEmailVerificationDestination : QuiketDestination {
    override val route: String = "login/password-reset/email-verification"
}

internal data object PasswordResetNewPasswordDestination : QuiketDestination {
    override val route: String = "login/password-reset/new-password"
}

internal data object KakaoNicknameDestination : QuiketDestination {
    override val route: String = "login/kakao/nickname"
}

internal data object KakaoAccountLinkDestination : QuiketDestination {
    override val route: String = "login/kakao/account-link"
}
