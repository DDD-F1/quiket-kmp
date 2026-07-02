plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("quiket.android.library")
}

android {
    namespace = "com.f1.quiket.core.testing"
}

dependencies {
    api(libs.androidx.test.core)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.robolectric)
    api(libs.truth)
}
