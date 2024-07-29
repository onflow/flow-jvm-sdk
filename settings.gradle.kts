pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }

    repositories {
        gradlePluginPortal()
        maven("https://kotlin.bintray.com/kotlinx")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name="flow-jvm-sdk"
include("sdk", "kotlin-example", "java-example", "common")
