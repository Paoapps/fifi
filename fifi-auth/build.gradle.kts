plugins {
    kotlin("multiplatform") version libs.versions.kotlin
    id("com.android.library") version libs.versions.plugin.android
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "com.paoapps.fifi"
version = "0.0.36-SNAPSHOT-41"

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
        publishLibraryVariants("release")
        publishLibraryVariantsGroupedByFlavor = true
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":fifi-common"))
                implementation(project(":fifi-framework"))

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
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.androidx.security.crypto)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
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
        val jvmMain by getting
        val jvmTest by getting
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "com.paoapps.fifi.auth"
}

mavenPublishing {
    publishToMavenCentral()

    coordinates(group.toString(), "fifi-auth", version.toString())

    pom {
        name = "FiFi Auth"
        description = "Authentication module for FiFi Kotlin Multiplatform library."
        inceptionYear = "2024"
        url = "https://github.com/Paoapps/fifi"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "lammertw"
                name = "Lammert Westerhoff"
                email = "lammert@paoapps.com"
            }
        }
        scm {
            url = "https://github.com/Paoapps/fifi"
            connection = "scm:git:git://github.com/Paoapps/fifi.git"
            developerConnection = "scm:git:ssh://git@github.com:Paoapps/fifi.git"
        }
    }
}

// Configure signing only when publishing to Maven Central
if (project.hasProperty("signingInMemoryKeyId") ||
    project.hasProperty("signing.keyId") ||
    System.getenv("GPG_KEY_ID") != null) {
    mavenPublishing {
        signAllPublications()
    }
}
