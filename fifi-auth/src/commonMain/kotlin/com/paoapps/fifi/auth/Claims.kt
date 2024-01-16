package com.paoapps.fifi.auth

import kotlinx.datetime.Instant

interface Claims {
    val exp: Instant
}

interface IdentifiableClaims<UserId>: Claims {
    val id: UserId
    val identifier: String get() = id.toString()
}
