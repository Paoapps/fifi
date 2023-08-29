package com.paoapps.fifi.domain.cache

import com.paoapps.fifi.domain.cache.NowProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class DatetimeNowProvider : NowProvider {

    override fun now() = currentInstant().toEpochMilliseconds()

    override fun currentInstant(): Instant = Clock.System.now()
}
