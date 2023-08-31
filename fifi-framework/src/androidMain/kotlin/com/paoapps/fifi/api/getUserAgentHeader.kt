package com.paoapps.fifi.api

import android.os.Build

actual fun getUserAgentHeader(appVersion: String): String {
    return "Android/${Build.VERSION.SDK_INT} ($appVersion)"
}
