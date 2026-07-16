import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localProperty(name: String): String =
    providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name.toEnvironmentVariableName()).orNull
        ?: localProperties.getProperty(name)
        ?: ""

fun String.toEnvironmentVariableName(): String =
    replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace('.', '_')
        .uppercase()

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val kakaoNativeAppKey = localProperty("kakao.native.app.key")
val releaseSigningStoreFile = localProperty("storeFile")
val releaseSigningStorePassword = localProperty("storePassword")
val releaseSigningKeyAlias = localProperty("keyAlias")
val releaseSigningKeyPassword = localProperty("keyPassword")
val hasReleaseSigningConfig = listOf(
    releaseSigningStoreFile,
    releaseSigningStorePassword,
    releaseSigningKeyAlias,
    releaseSigningKeyPassword,
).all(String::isNotBlank)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.f1.quiket.composeapp")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":app-shell"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compottie)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(project(":core:auth"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.kakao.v2.user)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "com.f1.quiket.composeapp"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.f1.quiket"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.5"

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            kakaoNativeAppKey.asBuildConfigString(),
        )
        manifestPlaceholders["kakaoRedirectScheme"] = "kakao$kakaoNativeAppKey"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(releaseSigningStoreFile)
                storePassword = releaseSigningStorePassword
                keyAlias = releaseSigningKeyAlias
                keyPassword = releaseSigningKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".composeapp"
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

gradle.taskGraph.whenReady(
    object : Action<TaskExecutionGraph> {
        override fun execute(graph: TaskExecutionGraph) {
            val includesAndroidReleasePackagingTask = graph.allTasks.any { task ->
                task.project.path == ":composeApp" && task.name in setOf(
                    "assembleRelease",
                    "bundleRelease",
                    "installRelease",
                    "packageRelease",
                    "signRelease",
                )
            }
            if (includesAndroidReleasePackagingTask && !hasReleaseSigningConfig) {
                throw GradleException(
                    "Missing release signing properties in local.properties: " +
                        "storeFile, storePassword, keyAlias, keyPassword",
                )
            }
            if (includesAndroidReleasePackagingTask && kakaoNativeAppKey.isBlank()) {
                throw GradleException(
                    "Missing Kakao Native App Key. Set kakao.native.app.key in local.properties " +
                        "or KAKAO_NATIVE_APP_KEY in the environment.",
                )
            }
        }
    },
)
