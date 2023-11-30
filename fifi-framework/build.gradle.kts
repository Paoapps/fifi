plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("convention.publication")
}

group = "com.paoapps.fifi"
version = "0.0.5-SNAPSHOT"

val ktorVersion = "2.1.3"
val logbackVersion = "1.2.3"
val serializationVersion = "1.4.0"
val coroutinesVersion = "1.6.4"
val koinVersion = "3.4.3"
val dateTimeVersion = "0.4.0"
val kermitVersion = "1.0.0"
val lifecycleVersion = "2.2.0"

kotlin {
    android {
        publishLibraryVariants("debug", "release")
    }

//    jvm {
//        compilations.all {
//            kotlinOptions.jvmTarget = "1.8"
//        }
//        testRuns["test"].executionTask.configure {
//            useJUnitPlatform()
//        }
//    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":fifi-common"))

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

                implementation(libs.multiplatform.settings)

                implementation(libs.blockedcache)
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/commonMain/kotlin"))
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("com.google.android.material:material:1.2.1")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
                implementation(libs.koin.android)
                implementation(libs.androidx.security.crypto)

                implementation(libs.bundles.android)
                implementation(libs.bundles.android.ui)
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/androidMain/kotlin"))
        }
//        val androidTest by getting {
//            dependencies {
//                implementation(kotlin("test-junit"))
//                implementation("junit:junit:4.13.2")
//            }
//        }

//        val jvmMain by getting {
//            kotlin.srcDirs(project.projectDir.resolve("build/src/jvmMain/kotlin"))
//        }

//        val jsMain by getting {
//            kotlin.srcDirs(project.projectDir.resolve("build/src/jsMain/kotlin"))
//        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            kotlin.srcDirs(project.projectDir.resolve("build/src/iosMain/kotlin"))
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    namespace = "com.paoapps.fifi.framework"

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
}