plugins {
    application
    id("kotlin-platform-jvm")
}

version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0-native-mt")
    implementation("io.ktor:ktor-network:1.6.8")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("client.MainKt")
}
