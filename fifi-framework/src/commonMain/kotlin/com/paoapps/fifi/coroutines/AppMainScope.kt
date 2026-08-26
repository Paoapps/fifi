package com.paoapps.fifi.coroutines

import com.paoapps.fifi.log.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class AppMainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + exceptionHandler

    internal val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error(throwable.message ?: "Unhandled coroutine error", throwable)
    }
}
