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
    }
}

rootProject.name = "SimpleChat"
include(":app")
include(":feature:home")
include(":feature:authentication")
include(":core:ui")
include(":core:common")
include(":core:crypto")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":domain:home")
include(":domain:profile")
include(":domain:authentication")
include(":data:home")
include(":data:profile")
include(":data:authentication")
