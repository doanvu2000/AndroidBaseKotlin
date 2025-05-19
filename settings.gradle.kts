pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
    }
}
rootProject.name = "BaseProject"
include(":app")
include(":skeleton")
include(":sliderview")
include(":stickerview")
include(":simplecropview")
include(":cameraview")
include(":weekviewevent")
