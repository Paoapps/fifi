package com.paoapps.fifi.api

import android.os.Build
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

actual fun getUserAgentHeader(appVersion: String): String {
    return "Android/${Build.VERSION.SDK_INT} ($appVersion)"
}

actual fun recordException(throwable: Throwable) {
    Firebase.crashlytics.recordException(throwable)
}