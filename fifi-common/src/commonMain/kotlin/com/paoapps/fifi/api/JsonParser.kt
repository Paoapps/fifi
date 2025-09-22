package com.paoapps.fifi.api

import com.paoapps.fifi.serialization.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
val jsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
    serializersModule = SerializersModule {
        contextual(Instant::class, InstantSerializer)
    }
}
