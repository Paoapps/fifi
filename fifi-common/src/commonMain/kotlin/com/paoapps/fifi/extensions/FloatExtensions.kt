package com.paoapps.fifi.extensions

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals ) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
