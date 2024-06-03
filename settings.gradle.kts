pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "FiFi"

includeBuild("convention-plugins")

include(":fifi-common")
include(":fifi-auth")
include(":fifi-framework")
include(":sample:android")
include(":sample:shared")
