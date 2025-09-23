@file:OptIn(ExperimentalTime::class)

package com.paoapps.fifi.auth

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface Claims {
    val exp: Instant
}

interface IdentifiableClaims<UserId>: Claims {
    val id: UserId
    val identifier: String get() = id.toString()
}
