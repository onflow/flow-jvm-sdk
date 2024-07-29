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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/ethereum/maven/") }
}

dependencies {
    // Use JUnit Jupiter Engine for testing.
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
//    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    implementation(project(":sdk"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testFixturesImplementation("org.mockito:mockito-core:3.12.4")
    testFixturesImplementation("org.mockito:mockito-inline:3.11.2")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
