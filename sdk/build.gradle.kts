import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    jacoco
    signing
    `java-library`
    `java-test-fixtures`
    `maven-publish`
}

fun getProp(name: String, defaultValue: String? = null): String? {
    return project.findProperty("flow.$name")?.toString()?.trim()?.ifBlank { null }
        ?: project.findProperty(name)?.toString()?.trim()?.ifBlank { null }
        ?: defaultValue
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")

    api("org.onflow:flow:1.0.0")
    api("com.github.TrustedDataFramework:java-rlp:1.1.20")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    api("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    api(platform("com.fasterxml.jackson:jackson-bom:2.16.1"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.5.2")

    testApi("org.junit.jupiter:junit-jupiter:5.10.1")
    testApi("org.assertj:assertj-core:3.25.1")

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testFixturesImplementation("org.mockito:mockito-core:3.12.4")
    testFixturesImplementation("org.mockito:mockito-inline:3.11.2")

    testImplementation(testFixtures(project(":common")))
    testImplementation(project(":common"))
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        kotlin.srcDirs("src/intTest")
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    intTestImplementation(testFixtures(project(":common")))
    intTestImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    intTestImplementation("org.assertj:assertj-core:3.25.1")
    intTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
        finalizedBy("jacocoTestReport")
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            html.required = true
            xml.required = true
            csv.required = false
        }
    }

    jacoco {
        toolVersion = "0.8.11"
    }

    kotlinter {
        reporters = arrayOf("checkstyle", "plain", "html")
    }

    val documentationJar by creating(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.get().outputs)
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(classes)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource + sourceSets["testFixtures"].allSource)
    }

    artifacts {
        add("archives", documentationJar)
        add("archives", sourcesJar)
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.DEFAULT, true)

        coordinates(group.toString(), "flow-jvm-sdk", version.toString())

        signAllPublications()

        pom {
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            name.set(project.name)
            url.set("https://onflow.org")
            description.set("The Flow Blockchain JVM SDK")
            scm {
                url.set("https://github.com/onflow/flow")
                connection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
                developerConnection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
            }
            developers {
                developer {
                    name.set("Flow Developers")
                    url.set("https://onflow.org")
                }
            }
        }
    }
}

signing {
    if (getProp("signing.key") != null) {
        useInMemoryPgpKeys(getProp("signing.key"), getProp("signing.password"))
    } else {
        useGpgCmd()
    }
    sign(publishing.publications)
}
