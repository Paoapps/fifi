package com.paoapps.fifi.domain.network

import com.paoapps.fifi.api.domain.ApiResponse
import com.paoapps.fifi.api.domain.Failure
import com.paoapps.fifi.api.domain.Success

/**
 * Object which is propagated via the viewmodels to the UI.
 *
 */
sealed class NetworkDataContainer<out T, E> {

    class Empty<T, E>: NetworkDataContainer<T, E>()

    data class Offline<T, E>(val staleData: T? = null, val creationTimeStaleData: Long? = null): NetworkDataContainer<T, E>()

    /**
     * Indicaties the data is loading.
     */
    data class Loading<T, E>(val staleData: T? = null, val creationTimeStaleData: Long? = null) : NetworkDataContainer<T, E>()

    /**
     * Indicates a success.
     *
     * @property data The actual data.
     */
    data class Success<T, E>(val data: T) : NetworkDataContainer<T, E>()

    val actualOrStaleData: T?
        get() = when (this) {
            is Success -> this.data
            is Loading -> this.staleData
            is Error -> this.staleData
            is Offline -> this.staleData
            is Empty -> null
        }

    val loadingState: LoadingState
        get() = when (this) {
            is Loading -> LoadingState.Loading
            is Error -> {
                LoadingState.Error(failure.message)
            }
            is Offline -> {
                LoadingState.Offline
            }
            is Success, is Empty -> LoadingState.Idle
        }

    val isLoading: Boolean get() = this is Loading

    fun <D> map(transform: (T) -> (D)): NetworkDataContainer<D, E> = when(this) {
        is Loading -> Loading(staleData?.let(transform), creationTimeStaleData)
        is Success -> Success(transform(data))
        is Error -> Error(failure.map(), staleData?.let(transform), creationTimeStaleData)
        is Empty -> Empty()
        is Offline -> Offline(staleData?.let(transform), creationTimeStaleData)
    }

    fun <D> flatMap(transform: (T) -> (NetworkDataContainer<D, E>)): NetworkDataContainer<D, E> = when(this) {
        is Loading -> Loading(staleData?.let(transform)?.actualOrStaleData, creationTimeStaleData)
        is Success -> transform(data)
        is Error -> Error(failure.map(), staleData?.let(transform)?.actualOrStaleData, creationTimeStaleData)
        is Empty -> Empty()
        is Offline -> Offline(staleData?.let(transform)?.actualOrStaleData, creationTimeStaleData)
    }

    fun <D> mapNotNull(transform: (T) -> (D?)): NetworkDataContainer<D, E> = when(this) {
        is Loading -> Loading(staleData?.let(transform), creationTimeStaleData)
        is Success -> transform(data)?.let { Success(it) } ?: Empty()
        is Error -> Error(failure.map(), staleData?.let(transform), creationTimeStaleData)
        is Empty -> Empty()
        is Offline -> Offline(staleData?.let(transform), creationTimeStaleData)
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Error

    operator fun <X> plus(other: NetworkDataContainer<X, E>): NetworkDataContainer<Pair<T?, X?>, E> {
        return when {
            this is Error -> Error(failure = failure.map(), staleData = Pair(staleData, other.actualOrStaleData), creationTimeStaleData = creationTimeStaleData)
            other is Error -> Error(other.failure.map(), Pair(this.actualOrStaleData, other.staleData), other.creationTimeStaleData)
            this is Loading || other is Loading -> Loading(
                null,
                0
            )
            else -> Success(
                Pair(
                    this.actualOrStaleData,
                    other.actualOrStaleData
                )
            )
        }
    }

    data class Error<T, E>(
        val failure: Failure<T, E>,
        val staleData: T? = null,
        val creationTimeStaleData: Long? = null
    ) : NetworkDataContainer<T, E>() {

        override fun toString(): String {
            return "Response failure: $failure"
        }

//        val modelError: ModelError = if (kind == FailureKind.NETWORK) {
//            ModelError.NetworkError(message)
//        } else {
//            errors.firstOrNull()?.let { error ->
//                if (error.code != null && error.details != null) {
//                    ModelError.ServerError(status, error.code, error.details)
//                } else null
//            } ?: ModelError.ServerError(status, "unknown", "")
//        }
    }
}

fun <T, E> ApiResponse<T, E>.asCommonDataContainer(): NetworkDataContainer<T, E> = when (this) {
    is Success -> {
        NetworkDataContainer.Success<T, E>(data)
    }
    is Failure -> {
        NetworkDataContainer.Error(this)
    }
}
