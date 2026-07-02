import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class QuiketAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        plugins.withId("com.android.library") {
            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    buildConfig = true
                }

                packaging {
                    resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                }

                testOptions {
                    unitTests.isIncludeAndroidResources = true
                }
            }
        }

        plugins.withId("org.jetbrains.kotlin.android") {
            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        tasks.withType(Test::class.java).configureEach {
            javaClass.methods
                .firstOrNull { method -> method.name == "setFailOnNoDiscoveredTests" }
                ?.invoke(this, false)
        }
    }
}
