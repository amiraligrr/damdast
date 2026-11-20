pluginManagement {
    repositories {
        // Google's Maven endpoint is not reachable from every network.
        // This mirror serves the same artifacts.
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        maven("https://maven.tapsell.ir")
    }
}

rootProject.name = "Damdast"
include(":app")
