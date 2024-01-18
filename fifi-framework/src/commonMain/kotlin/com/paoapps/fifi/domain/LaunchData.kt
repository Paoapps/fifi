package com.paoapps.fifi.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LaunchData(
    val isFirstLaunch: Boolean = true,
    val previousAppVersion: String? = null,
    val currentAppVersion: String
)
