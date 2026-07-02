package com.f1.quiket.feature.mypage.navigation

import com.f1.quiket.core.navigation.QuiketDestination

data object MyPageDestination : QuiketDestination {
    override val route: String = "main/mypage"
}

data object MyPageSettingDestination : QuiketDestination {
    override val route: String = "main/mypage/setting"
}

data object AccountSettingDestination : QuiketDestination {
    override val route: String = "main/mypage/setting/account"
}

data object AlarmSettingDestination : QuiketDestination {
    override val route: String = "main/mypage/setting/alarm"
}

data object InquiryDestination : QuiketDestination {
    override val route: String = "main/mypage/setting/inquiry"
}

data object TermsDestination : QuiketDestination {
    override val route: String = "main/mypage/setting/terms"
}

data object PrivacyPolicyDestination : QuiketDestination {
    override val route: String = "main/mypage/setting/privacy-policy"
}