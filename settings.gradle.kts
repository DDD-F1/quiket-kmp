pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "Quiket"

include(":app")
include(":composeApp")
include(":core:common")
include(":core:database")
include(":core:designsystem")
include(":core:navigation")
include(":core:network")
include(":core:session")
include(":core:testing")
include(":feature:floating")
include(":feature:history")
include(":feature:home")
include(":feature:login")
include(":feature:main")
include(":feature:mypage")
include(":feature:onboarding")
include(":feature:review")
include(":feature:sample")
include(":feature:splash")
