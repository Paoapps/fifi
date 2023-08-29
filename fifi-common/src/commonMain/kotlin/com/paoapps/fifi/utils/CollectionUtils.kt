package com.paoapps.fifi.utils

 fun <T> Iterable<T>.insertInBetween(item: T): List<T> {
    val result = mutableListOf<T>()
    forEach { element ->
        if (result.isNotEmpty()) {
            result.add(item)
        }
        result.add(element)
    }
    return result
}

fun <T> Set<T>.toggle(item: T): Set<T> = if (contains(item)) minus(item) else plus(item)
