plugins {
    `kotlin-dsl`
}

group = "com.f1.quiket.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "quiket.android.application"
            implementationClass = "QuiketAndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "quiket.android.library"
            implementationClass = "QuiketAndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "quiket.android.compose"
            implementationClass = "QuiketAndroidComposeConventionPlugin"
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:9.1.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
}
