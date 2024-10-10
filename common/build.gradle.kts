import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    `java-test-fixtures`
}

// Helper function to get properties
fun getProp(name: String, defaultValue: String? = null): String? {
    return project.findProperty("flow.$name")?.toString()?.trim()?.ifBlank { null }
        ?: project.findProperty(name)?.toString()?.trim()?.ifBlank { null }
        ?: defaultValue
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)  // Set JVM target to 21
        freeCompilerArgs.addAll("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/ethereum/maven/") }
}

dependencies {
    implementation(project(":sdk"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testFixturesImplementation(project(":sdk"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testFixturesImplementation("org.mockito:mockito-core:5.5.0")
    testFixturesImplementation("org.mockito:mockito-inline:5.2.0")

    intTestImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    intTestImplementation("org.assertj:assertj-core:3.25.1")
    intTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
//    create("testFixtures") {
//        compileClasspath += sourceSets["main"].output
//        runtimeClasspath += sourceSets["main"].output
//    }
    create("intTest") {
        compileClasspath += sourceSets["main"].output + sourceSets["testFixtures"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["testFixtures"].output
        kotlin.srcDirs("src/intTest/kotlin")
    }
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

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
