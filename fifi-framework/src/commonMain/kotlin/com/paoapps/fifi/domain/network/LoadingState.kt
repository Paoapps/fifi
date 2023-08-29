package com.paoapps.fifi.domain.network

sealed class LoadingState {

    object Idle: LoadingState()
    object Loading: LoadingState()
    data class Error(val error: String): LoadingState()
    object Offline: LoadingState()

    val isLoading: Boolean get() = this == Loading
    val isError: Boolean get() = this is Error
    val errorMessage: String? get() = if (this is Error) error else null

    operator fun plus(other: LoadingState): LoadingState = when (this) {
        Idle -> other
        Loading, Offline -> if (other is Error) other else this
        is Error -> this
    }
}
