package com.paoapps.fifi.log

interface IOSLoggingDelegate {
    fun error(message: String)
    fun warn(message: String)
    fun debug(message: String)
    fun info(message: String)
}

var iosLoggingDelegate: IOSLoggingDelegate? = null

actual fun error(message: String, error: Throwable?, vararg args: String) {
    iosLoggingDelegate?.error(message)
}

actual fun warn(message: String, error: Throwable?, vararg args: String) {
    iosLoggingDelegate?.warn(message)
}

actual fun debug(message: String, vararg args: String) {
    iosLoggingDelegate?.debug(message)
}

actual fun info(message: String, vararg args: String) {
    iosLoggingDelegate?.info(message)
}