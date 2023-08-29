package com.paoapps.fifi.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class AppMainScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        //        get() = Dispatchers.Main + job + exceptionHandler
        get() = Dispatchers.Main + job

    internal val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
//        com.eneco.enecoapp.shared.common.logging.error(throwable.message ?: "Error occurred", throwable)
        println("native: error occurred: ${throwable.message}")
    }
}