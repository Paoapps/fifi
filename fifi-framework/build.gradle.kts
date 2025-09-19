kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":fifi-common"))

                //Network
                implementation(libs.bundles.ktor.common)

                // DI
                api(libs.koin.core)

                implementation(libs.multiplatform.settings)

                implementation(libs.blockedcache)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okHttp)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.koin.android)

                implementation(libs.bundles.android)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.okHttp)
            }
        }
    }
}
