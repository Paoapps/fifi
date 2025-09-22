package com.paoapps.fifi.utils

import kotlinx.datetime.*
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.ExperimentalTime

const val ONE_SECOND_MS = 1 * 1000L

// copied internal fun from kotlinx-datetime
internal fun Int.monthLength(leapYear: Boolean): Int {
    return when (this) {
        2 -> if (leapYear) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

// copied internal fun from kotlinx-datetime
internal fun isLeapYear(year: Int): Boolean {
    val prolepticYear: Long = year.toLong()
    return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
}

fun Month.days(year: Int) = number.monthLength(isLeapYear(year))

val LocalDate.atStartOfDay get() = LocalDateTime(year, monthNumber, dayOfMonth, 0, 0)
val LocalDate.atEndOfDay get() = LocalDateTime(year, monthNumber, dayOfMonth, 23, 59, 59, 999)

val LocalDate.atStartOfMonth get() = LocalDate(year, month, 1)
val LocalDate.atEndOfMonth get() = LocalDate(year, month, month.days(year))

val LocalDate.atStartOfWeek get() = atStartOfDayOfWeek(DayOfWeek.SUNDAY)
val LocalDate.atStartOfIsoWeek get() = atStartOfDayOfWeek(DayOfWeek.MONDAY)

val LocalDate.atEndOfIsoWeek: LocalDate get() = atEndOfDayOfWeek(DayOfWeek.SUNDAY)

val LocalDate.atStartOfYear get() = LocalDate(year, Month.JANUARY, 1)

fun LocalDate.atStartOfDayOfWeek(day: DayOfWeek): LocalDate {
    for (n in 0 until 7) {
        val date = this.minus(n, DateTimeUnit.DAY)
        if (date.dayOfWeek == day) return date
    }
    error("Shouldn't happen")
}

fun LocalDate.atEndOfDayOfWeek(day: DayOfWeek): LocalDate {
    for (n in 0 until 7) {
        val date = this.plus(n, DateTimeUnit.DAY)
        if (date.dayOfWeek == day) return date
    }
    error("Shouldn't happen")
}

fun getLocalTimeZone(): TimeZone {
    return TimeZone.currentSystemDefault()
}

// copied internal fun from klock
val LocalDate.weekOfYear0: Int
    get() {
        val firstThursday = year.first(DayOfWeek.THURSDAY)
        val offset = firstThursday.dayOfMonth - 3
        return (dayOfYear - offset) / 7
    }

// copied internal fun from klock
val LocalDate.weekOfYear1: Int get() = weekOfYear0 + 1
val LocalDateTime.weekOfYear1 get() = date.weekOfYear1
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
val Instant.year get() = toLocalDateTime(TimeZone.currentSystemDefault()).year

// copied internal fun from klock
// ISO 8601 (first week is the one after 1 containing a thursday)
private fun Int.first(dayOfWeek: DayOfWeek): LocalDate {
    val start = LocalDate(this, 1, 1)
    var n = 1
    while (true) {
        val time = start.plus(DateTimeUnit.DateBased.DayBased(n))
        if (time.dayOfWeek == dayOfWeek) return time
        n++
    }
}

val LocalDate.epochMilliseconds get() = LocalDateTime(year, monthNumber, dayOfMonth, 0, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

// Instant.fromEpochMilliseconds is not available on iOS
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun createInstant(epochMilliseconds: Long): Instant = Instant.fromEpochMilliseconds(epochMilliseconds)

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
val Instant.localDate: LocalDate get() = toLocalDateTime(getLocalTimeZone()).date
val DayOfWeek.isWeekend get() = this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY
val DayOfWeek.index1Sunday get() = (isoDayNumber % 7) + 1
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
val LocalDate.toInstant get() = toLocalDateTime.toInstant(getLocalTimeZone())
val LocalDate.toLocalDateTime get() = LocalDateTime(year, monthNumber, dayOfMonth, 0, 0, 0, 0)

val LocalDate.minDaysInIsoWeek: Int
    get() = 4

fun LocalDate.isSameWeek(other: LocalDate): Boolean {
    return other >= this.atStartOfIsoWeek
            && other <= this.atEndOfIsoWeek
}

fun LocalDate.isSameMonth(other: LocalDate): Boolean {
    return other.year == this.year && other.month == this.month
}

/**
 * Checks if this dates week belongs to the given year. According to ISO 8601, a week belongs to a year if at least for
 * days fall in the given year.
 */
fun LocalDate.doesWeekBelongsToYear(year: Int): Boolean {
    val count = countDaysInWeek(year, atStartOfIsoWeek)
    return count >= minDaysInIsoWeek
}

@OptIn(ExperimentalTime::class)
fun LocalDate.getMonthForWeek(week: Int): Int {
    var date = atStartOfIsoWeek
    val grouped = (0 until 7).toList()
        .map { date.plus(it, DateTimeUnit.DAY) }
        .groupBy { it.monthNumber }

    return grouped.entries.maxByOrNull { it.value.size }?.key ?: 1
}

/**
 * Count the number of days of the given year in the week starting at startDate.
 */
fun countDaysInWeek(year: Int, startDate: LocalDate): Int {
    var count = 0
    var date = startDate
    for (i in 0 until 7) {
        if (date.year == year) {
            count++
        }

        date = date.plus(1, DateTimeUnit.DAY)
    }

    return count
}

fun LocalDate.rangeTo(other: LocalDate, unit: DateTimeUnit.DateBased = DateTimeUnit.DAY) = object : Iterable<LocalDate> {
    override fun iterator() = object : Iterator<LocalDate> {
        var current = this@rangeTo

        override fun hasNext() = current <= other

        override fun next(): LocalDate {
            val result = current
            current = current.plus(1, unit)
            return result
        }
    }
}
