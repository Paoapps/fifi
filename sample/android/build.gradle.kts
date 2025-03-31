plugins {
    id("com.android.application")
    kotlin("android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.paoapps.fifi.sample.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "com.paoapps.fifi.sample.android"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

val composeVersion = "1.3.1"
val koinVersion = "3.4.3"
val serializationVersion = "1.5.1"

dependencies {
    implementation(project(":sample:shared"))
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("junit:junit:4.12")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") {
        version {
            strictly("1.6.4")
        }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation ("io.coil-kt:coil:2.4.0")
    implementation ("io.coil-kt:coil-svg:2.4.0")

    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")

    // TODO: everything below is new in rewrite, remove anything above that we don't need anymore

    //DI
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:3.4.4")

    // Compose
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.navigation:navigation-compose:2.4.1")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Material UI Extensions
    implementation("io.github.vanpra.compose-material-dialogs:core:0.9.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.28.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
}