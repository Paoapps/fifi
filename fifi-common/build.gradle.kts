kotlin {
    sourceSets {
        commonMain {
            dependencies {
                //Network
                implementation(libs.bundles.ktor.common)

                // DI
                api(libs.koin.core)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
