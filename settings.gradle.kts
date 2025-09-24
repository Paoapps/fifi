pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FiFi"

include(":fifi-common")
include(":fifi-auth")
include(":fifi-framework")
include(":sample:android")
include(":sample:shared")
