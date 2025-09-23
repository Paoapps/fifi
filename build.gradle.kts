plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false
    id("com.android.library") version libs.versions.plugin.android apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

// Shared configuration for KMP library modules
subprojects {
    if (name.startsWith("fifi-")) {
        apply(plugin = "kotlin-multiplatform")
        apply(plugin = "com.android.library")
        apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
        apply(plugin = "com.vanniktech.maven.publish")

        group = property("group") as String
        version = property("version") as String

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
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

            sourceSets.apply {
                commonTest {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }
                androidMain {
                    dependencies {
                        // Android specific dependencies can be added here
                    }
                }
                androidUnitTest {
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }
                iosX64Main.get()
                iosArm64Main.get()
                iosSimulatorArm64Main.get()
                iosMain {
                    dependsOn(commonMain.get())
                    iosX64Main.get().dependsOn(this)
                    iosArm64Main.get().dependsOn(this)
                    iosSimulatorArm64Main.get().dependsOn(this)
                }
                iosX64Test.get()
                iosArm64Test.get()
                iosSimulatorArm64Test.get()
                iosTest {
                    dependsOn(commonTest.get())
                    iosX64Test.get().dependsOn(this)
                    iosArm64Test.get().dependsOn(this)
                    iosSimulatorArm64Test.get().dependsOn(this)
                }
                jvmMain.get()
                jvmTest.get()
            }
        }

        extensions.configure<com.android.build.gradle.LibraryExtension> {
            compileSdk = 34
            sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
            defaultConfig {
                minSdk = 26
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            lint {
                disable += "NullSafeMutableLiveData"
            }
            namespace = "com.paoapps.fifi.${project.name.replace("-", "")}"
        }

        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral()

            coordinates(
                group.toString(),
                project.name,
                version.toString()
            )

            pom {
                name = "FiFi ${project.name.replaceFirstChar { it.uppercase() }}"
                description = "${project.name.replace("-", " ").replaceFirstChar { it.uppercase() }} module for FiFi Kotlin Multiplatform library."
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
                    developerConnection = "scm:git:ssh://git@github.com/Paoapps/fifi.git"
                }
            }
        }

        // Configure signing only when publishing to Maven Central
        if (project.hasProperty("signingInMemoryKeyId") ||
            project.hasProperty("signing.keyId") ||
            System.getenv("GPG_KEY_ID") != null) {
            extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
                signAllPublications()
            }
        }
    }
}
