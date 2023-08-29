package com.paoapps.fifi.auth

import kotlinx.datetime.Instant

interface Claims {
    val exp: Instant
}

interface IdentifiableClaims: Claims {
    val identifier: String
}
