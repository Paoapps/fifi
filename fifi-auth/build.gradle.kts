kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":fifi-common"))
                implementation(project(":fifi-framework"))

                //Network
                implementation(libs.bundles.ktor.common)

                // DI
                api(libs.koin.core)

                implementation(libs.multiplatform.settings)

                implementation(libs.blockedcache)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.androidx.security.crypto)
            }
        }
    }
}
