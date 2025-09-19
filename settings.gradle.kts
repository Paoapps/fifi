pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "FiFi"

include(":fifi-common")
include(":fifi-auth")
include(":fifi-framework")
// include(":sample:android")
// include(":sample:shared")
