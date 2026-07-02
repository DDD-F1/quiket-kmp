plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("quiket.android.library")
}

android {
    namespace = "com.f1.quiket.core.session"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
