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

rootProject.name = "Markdown View"
include(":app")
include(":markwon-core")
include(":markwon-editor")
include(":markwon-ext-latex")
include(":markwon-inline-parser")
include(":markwon-test-span")
include(":markwon-syntax-highlight")
include(":markwon-ext-strikethrough")
include(":markwon-html")
include(":markwon-ext-tables")
include(":markwon-ext-tasklist")
include(":markwon-image")
include(":markwon-image-coil")
include(":markwon-image-glide")
