import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("quiket.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localProperty(
    name: String,
    defaultValue: String,
): String = localProperties.getProperty(name)
    ?.takeIf(String::isNotBlank)
    ?: defaultValue

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val quiketApiBaseUrl = localProperty(
    name = "quiket.api.baseUrl",
    defaultValue = "http://43.201.222.243:8080/api/v1/",
)
val releaseQuiketApiBaseUrl = localProperty(
    name = "quiket.api.baseUrl",
    defaultValue = "https://quiket.invalid/api/v1/",
)

android {
    namespace = "com.f1.quiket.core.network"

    defaultConfig {
        buildConfigField("String", "QUIKET_API_BASE_URL", quiketApiBaseUrl.asBuildConfigString())
    }

    buildTypes {
        release {
            buildConfigField("String", "QUIKET_API_BASE_URL", releaseQuiketApiBaseUrl.asBuildConfigString())
        }
    }
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
