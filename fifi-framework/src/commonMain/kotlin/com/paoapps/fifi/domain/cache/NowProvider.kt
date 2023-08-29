package com.paoapps.fifi.domain.cache

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface NowProvider {
    fun now(): Long
    fun currentInstant(): Instant
}
