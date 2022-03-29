pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url= "https://kotlin.bintray.com/kotlinx")
    }
}

rootProject.name = "TCPChat"

include(":common")
include(":server")
include(":client")
