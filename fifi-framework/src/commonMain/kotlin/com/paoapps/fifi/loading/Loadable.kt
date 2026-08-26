package com.paoapps.fifi.loading

import com.paoapps.blockedcache.CacheResult

/**
 * A UI-agnostic representation of cached or fetched data suitable for ViewModel output.
 *
 * Apps typically map [Loadable] to their own error/offline UI components. See [asLoadable]
 * for converting [CacheResult] from blocked-cache.
 */
data class Loadable<T>(
    val result: Result<T>,
    val isLoading: Boolean,
) {
    val data: T? get() = when (result) {
        is Result.Data -> result.value
        is Result.Error, is Result.Empty, is Result.Offline -> null
    }

    fun <R> map(transform: (T) -> R): Loadable<R> = Loadable(
        result = when (result) {
            is Result.Data -> Result.Data(transform(result.value))
            is Result.Empty -> Result.Empty()
            is Result.Error -> Result.Error(result.throwable, result.message)
            is Result.Offline -> Result.Offline()
        },
        isLoading = isLoading,
    )

    sealed class Result<T> {
        data class Data<T>(val value: T) : Result<T>()
        data class Error<T>(val throwable: Throwable? = null, val message: String? = null) : Result<T>()
        data class Offline<T>(val message: String? = null) : Result<T>()
        class Empty<T> : Result<T>()

        val isError: Boolean get() = this is Error || this is Offline
    }

    companion object {
        fun <T> loading(value: T): Loadable<T> = Loadable(Result.Data(value), isLoading = true)
        fun <T> data(value: T): Loadable<T> = Loadable(Result.Data(value), isLoading = false)
        fun <T> error(throwable: Throwable? = null, message: String? = null): Loadable<T> =
            Loadable(Result.Error(throwable, message), isLoading = false)
        fun <T> offline(message: String? = null): Loadable<T> =
            Loadable(Result.Offline(message), isLoading = false)
        fun <T> empty(): Loadable<T> = Loadable(Result.Empty(), isLoading = false)
    }
}

fun <T : Any> CacheResult<T>.asLoadable(
    loadingPlaceholder: T? = null,
    treatEmptyAsError: Boolean = false,
    ignoreError: Boolean = false,
    offlineAsError: Boolean = false,
    errorMessage: (CacheResult.Error<*>) -> String? = { null },
): Loadable<T> =
    actualOrStaleData?.let { Loadable.data(it) } ?: when (this) {
        is CacheResult.Error -> {
            if (ignoreError) Loadable.empty()
            else Loadable.error(message = errorMessage(this))
        }
        is CacheResult.Offline -> {
            if (ignoreError) Loadable.empty()
            else if (offlineAsError) Loadable.error(message = "Offline")
            else Loadable.offline()
        }
        is CacheResult.Loading -> loadingPlaceholder?.let { Loadable.loading(it) } ?: Loadable.empty()
        else -> if (treatEmptyAsError) Loadable.error(message = "Empty") else Loadable.empty()
    }

fun <T : Any, R : Any> CacheResult<T>.asLoadable(
    loadingPlaceholder: R?,
    treatEmptyAsError: Boolean = false,
    ignoreError: Boolean = false,
    offlineAsError: Boolean = false,
    errorMessage: (CacheResult.Error<*>) -> String? = { null },
    transform: (T) -> R?,
): Loadable<R> =
    actualOrStaleData?.let { transform(it)?.let { mapped -> Loadable.data(mapped) } } ?: when (this) {
        is CacheResult.Error -> {
            if (ignoreError) Loadable.empty()
            else Loadable.error(message = errorMessage(this))
        }
        is CacheResult.Offline -> {
            if (ignoreError) Loadable.empty()
            else if (offlineAsError) Loadable.error(message = "Offline")
            else Loadable.offline()
        }
        is CacheResult.Loading -> loadingPlaceholder?.let { Loadable.loading(it) } ?: Loadable.empty()
        else -> if (treatEmptyAsError) Loadable.error(message = "Empty") else Loadable.empty()
    }

fun <T : Any> List<T>.asLoadableList(): List<Loadable<T>> = map { Loadable.data(it) }
