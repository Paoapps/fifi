package com.paoapps.fifi.domain

import kotlinx.serialization.Serializable

@Serializable
data class LaunchData(
    val isFirstLaunch: Boolean = true,
    val previousAppVersion: String? = null,
    val currentAppVersion: String
)
