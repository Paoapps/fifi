package com.paoapps.fifi.log

expect fun error(message: String, error: Throwable? = null, vararg args: String)
expect fun warn(message: String, error: Throwable? = null, vararg args: String = emptyArray())
expect fun debug(message: String, vararg args: String = emptyArray())
expect fun info(message: String, vararg args: String = emptyArray())