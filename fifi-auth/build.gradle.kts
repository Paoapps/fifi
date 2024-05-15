plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("maven-publish")
}

group = "com.paoapps.fifi"
version = "0.0.27"

kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "fifi-auth"
        }
    }
    
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

            kotlin.srcDirs(project.projectDir.resolve("build/src/commonMain/kotlin"))
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            kotlin.srcDirs(project.projectDir.resolve("build/src/androidMain/kotlin"))

            dependencies {
                implementation(libs.koin.android)
                implementation(libs.androidx.security.crypto)
            }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "com.paoapps.fifi.auth"
}

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//            credentials {
//                username = getExtraString("ossrhUsername")
//                password = getExtraString("ossrhPassword")
//            }
        }
    }
//
//    // Configure all publications
//    publications.withType<MavenPublication> {
//        // Stub javadoc.jar artifact
//        artifact(javadocJar.get())
//
//        // Provide artifacts information requited by Maven Central
//        pom {
//            name.set("FiFi")
//            description.set("Kotlin Multiplatform Mobile framework for optimal code sharing between iOS and Android.")
//            url.set("https://github.com/lammertw/fifi")
//
//            licenses {
//                license {
//                    name.set("MIT")
//                    url.set("https://opensource.org/licenses/MIT")
//                }
//            }
//            developers {
//                developer {
//                    id.set("https://github.com/lammertw")
//                    name.set("Lammert Westerhoff")
//                    email.set("lammert@paoapps.com")
//                }
//            }
//            scm {
//                url.set("https://github.com/lammertw/fifi")
//            }
//        }
//    }
}