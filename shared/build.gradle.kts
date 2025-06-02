plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
}

kotlin {
//    @Suppress("OPT_IN_USAGE")
//    targetHierarchy.default()
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.filekit.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.kotlinx.json)

                api(libs.koin.core)
                implementation(libs.koin.compose)

                // 引入excel处理
                // implementation("org.apache.poi:poi:5.2.3")
                implementation(libs.poi)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test")) // Kotlin 测试基础库
            }
        }
    }
}
