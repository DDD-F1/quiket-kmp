pluginManagement {
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

include(":composeApp")
include(":app-shell")
include(":core:auth")
include(":core:designsystem")
include(":core:legal")
include(":core:network")
include(":core:platform")
include(":feature:auth")
include(":feature:history")
include(":feature:home")
include(":feature:mypage")
include(":feature:quiz")
include(":feature:subject")
