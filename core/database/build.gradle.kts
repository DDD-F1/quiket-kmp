plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("quiket.android.library")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.f1.quiket.core.database"
}

dependencies {
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.hilt.android)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}
