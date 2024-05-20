package com.paoapps.fifi.log

actual fun error(message: String, error: Throwable?, vararg args: String) {
    println(message)
}

actual fun warn(message: String, error: Throwable?, vararg args: String) {
    println(message)
}

actual fun debug(message: String, vararg args: String) {
    println(message)
}

actual fun info(message: String, vararg args: String) {
    println(message)
}