package com.paoapps.fifi.domain.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

enum class NetworkStatus {
    AVAILABLE,
    UNAVAILABLE,
    UNKNOWN,
}

object NetworkStatusMonitor {
    private val networkStatusFlow = MutableStateFlow(NetworkStatus.UNKNOWN)
    val networkStatus: Flow<NetworkStatus> = networkStatusFlow

    fun update(status: NetworkStatus) {
        networkStatusFlow.value = status
    }
}
