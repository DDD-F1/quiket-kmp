plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("quiket.android.library")
    id("quiket.android.compose")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.f1.quiket.feature.main"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:floating"))
    implementation(project(":feature:home"))
    implementation(project(":feature:history"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:review"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
