plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

val koinVersion = "3.4.3"

kotlin {
    android()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            export(project(":fifi-framework"))

            // workaround: https://youtrack.jetbrains.com/issue/KT-60230#focus=Comments-27-7921542.0-0
            // will be fixed in Kotlin 1.9.10
            if (System.getenv("XCODE_VERSION_MAJOR") == "1500") {
                linkerOpts += "-ld64"
            }

            transitiveExport = true
        }
    }

    sourceSets {
        val ktorVersion = "2.3.11"
        val coroutinesVersion = "1.7.3"
        val serializationVersion = "1.5.1"

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion") {
                    version {
                        strictly(coroutinesVersion)
                    }
                }
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                implementation(project(":fifi-common"))
                api(project(":fifi-framework"))

                implementation(libs.blockedcache)

                implementation("io.insert-koin:koin-core:$koinVersion")
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/commonMain/kotlin"))
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("io.ktor:ktor-client-mock:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        val androidMain by getting {

            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

                // FIXME: test dependencies should not be here
                implementation("androidx.test.espresso:espresso-core:3.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("androidx.test.ext:junit-ktx:1.1.4")
                implementation("androidx.test.espresso:espresso-contrib:3.5.1")
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/androidMain/kotlin"))
        }
        // val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }

            kotlin.srcDirs(project.projectDir.resolve("build/src/iosMain/kotlin"))
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        animationsDisabled = true
    }
    namespace = "com.paoapps.fifi.sample.shared"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        named("main") {
            res.srcDirs("build/src/androidMain/res")
        }
    }
}
