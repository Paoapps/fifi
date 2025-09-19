plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false
    id("com.android.library") version libs.versions.plugin.android apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
