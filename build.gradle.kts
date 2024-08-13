import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Configuration variables
val defaultGroupId = "org.onflow"
val defaultVersion = "2.0.0"

fun getProp(name: String, defaultValue: String? = null): String? {
    return project.findProperty("flow.$name")?.toString()?.trim()?.ifBlank { null }
        ?: project.findProperty(name)?.toString()?.trim()?.ifBlank { null }
        ?: defaultValue
}

plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("org.jmailen.kotlinter") version "4.2.0" apply false
    id("kotlinx-serialization") version "1.8.0" apply false
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://dl.bintray.com/ethereum/maven/") }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jmailen.kotlinter")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "com.vanniktech.maven.publish")

    group = getProp("groupId", defaultGroupId)!!
    version = when {
        getProp("version") !in setOf("unspecified", null) -> getProp("version")!!
        getProp("snapshotDate") != null -> "${defaultVersion.replace("-SNAPSHOT", "")}.${getProp("snapshotDate")!!}-SNAPSHOT"
        else -> defaultVersion
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_21.toString()
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        }
    }

    dependencies {
        "api"("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
        "dokkaHtmlPlugin"("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")
    }

    tasks.named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_21.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview")
            allWarningsAsErrors = false
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }

        systemProperty("logback.configurationFile", rootProject.file("./logback.xml").absolutePath)
        systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "info")
        systemProperty("io.netty.logger.type", "slf4j")
        systemProperty("io.netty.leakDetection.level", "DISABLED")
    }
}
