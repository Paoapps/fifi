package com.paoapps.fifi.api

import kotlinx.serialization.json.Json

val jsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
}
