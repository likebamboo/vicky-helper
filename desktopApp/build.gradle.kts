import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":shared"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.likebamboo.vicky.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "vicky"
            packageVersion = project.property("VERSION_NAME")?.toString() ?: "1.0.0"

            val iconsRoot = project.file("desktop-icons")
            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
                bundleID = "com.likebamboo.vicky"
                setDockNameSameAsPackageName = true
                appStore = true
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                menuGroup = packageName
//                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "5217305f-f0cc-488b-9e29-0e47c0922761"
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }
        }

        buildTypes.release {
            proguard {
                isEnabled.set(false)
                obfuscate.set(false)
                optimize.set(false)
                joinOutputJars.set(false)
                configurationFiles.from("proguard-rules.pro")
            }
        }
    }
}
