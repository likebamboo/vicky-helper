rootProject.name = "VickyHelper"

include(":shared")
include(":desktopApp")
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        google()
    }
}

dependencyResolutionManagement {
    repositories {

        mavenCentral()
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")

        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
