package com.paoapps.fifi.viewmodel

import com.paoapps.fifi.coroutines.AppMainScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

actual abstract class ViewModel {

    actual val viewModelScope: CoroutineScope = AppMainScope()

    /**
     * Override this to do any cleanup immediately before the internal [CoroutineScope][kotlinx.coroutines.CoroutineScope]
     * is cancelled in [clear]
     */
    protected actual open fun onCleared() {
    }

    /**
     * Cancels the internal [CoroutineScope][kotlinx.coroutines.CoroutineScope]. After this is called, the ViewModel should
     * no longer be used.
     */
    fun clear() {
        onCleared()
        viewModelScope.cancel()
    }
}
