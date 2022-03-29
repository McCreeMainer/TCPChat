plugins {
    id("kotlin-platform-jvm")
    kotlin("plugin.serialization")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.2")
    implementation("io.ktor:ktor-network:1.6.8")

    testImplementation(kotlin("test"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

tasks.test {
    useJUnitPlatform()
}
