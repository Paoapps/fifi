plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("convention.publication")
}

group = "com.paoapps.fifi"
version = "0.0.32"

kotlin {
    jvmToolchain(17)

    androidTarget {

        publishLibraryVariants("debug", "release")
    }

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "fifi-common"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {

                //Network
                implementation(libs.bundles.ktor.common)

                //JSON
                implementation(libs.kotlinx.serialization.json)

                // DI
                api(libs.koin.core)

                // Date Time
                implementation(libs.kotlinx.datetime)

                //Coroutines
                implementation(libs.kotlinx.coroutines.core)
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/commonMain/kotlin"))
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            kotlin.srcDirs(project.projectDir.resolve("build/src/androidMain/kotlin"))
        }

        val jvmMain by getting {
            kotlin.srcDirs(project.projectDir.resolve("build/src/jvmMain/kotlin"))
        }

        val iosMain by creating {
            kotlin.srcDirs(project.projectDir.resolve("build/src/iosMain/kotlin"))
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "com.paoapps.fifi.common"
}
