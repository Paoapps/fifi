package com.paoapps.fifi.api.domain

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/**
 * Superclass for API calls. Sealed class to use in when expressions.
 */
sealed class ApiResponse<out T, out E> {
    companion object {
        fun <T, E> create(data: T?): ApiResponse<T, E> = data?.let { Success.ok(data) } ?: Failure.create(HttpStatusCode.NotFound)
    }
}

inline fun <T, R, E> ApiResponse<T, E>.map(transform: (T) -> R): ApiResponse<R, E> {
    when (this) {
        is Success -> {
            return Success(statusCode, transform(data))
        }
        is Failure -> {
            return map()
        }
    }
}

inline fun <T, R, E> ApiResponse<T, E>.flatMap(transform: (T) -> ApiResponse<R, E>): ApiResponse<R, E> {
    when (this) {
        is Success -> {
            return transform(data)
        }
        is Failure -> {
            return map()
        }
    }
}

fun <T, E> ApiResponse<T, E>.isEmpty(): Boolean {
    return when (this) {
        is Success -> {
            data is List<*> && data.isEmpty()
        }
        is Failure -> {
            false
        }
    }
}

/**
 * Indicates a success response containing the data from the API call.
 */
data class Success<out T, out E>(val statusCode: Int, val data: T) : ApiResponse<T, E>() {

    override fun toString(): String {
        return "Response data: $data"
    }

    companion object {
        fun <E> noData(statusCode: HttpStatusCode) = Success<Unit, E>(statusCode.value, Unit)
        fun <T, E> ok(data: T) = Success<T, E>(HttpStatusCode.OK.value, data)
    }
}

@Serializable
class ApiSuccessHolder<out T, out E>(val statusCode: Int, val data: T) {
    fun toSuccess() = Success<T, E>(statusCode, data)
}

/**
 * Indicates an error response.
 *
 * @param status The http status code.
 * @param errors Optional errors returned from the API.
 * @param message The exception message
 */
data class Failure<out T, out E>(
    val status: Int,
    val message: String,
    val kind: FailureKind = FailureKind.SERVER,
    val throwable: Throwable? = null,
    val serverError: E? = null,
) : ApiResponse<T, E>() {

    override fun toString(): String {
        return "Response errorcode: $status, message: $message"
    }

    fun <O> map(): Failure<O, E> = Failure(status, message, kind, throwable, serverError)

    fun isNotFound() = status == HttpStatusCode.NotFound.value

    companion object {
        fun <T, E> create(
            status: Int,
            message: String,
            throwable: Throwable? = null
        ) = Failure<T, E>(
            status, message, FailureKind.create(status, throwable), throwable
        )

        fun <T, E> create(statusCode: HttpStatusCode) = create<T, E>(statusCode.value, statusCode.description)
    }
}

/**
 * Indicates the different kind of failures.
 */
enum class FailureKind {
    CLIENT, SERVER, NETWORK;

    companion object {
        fun create(statusCode: Int, throwable: Throwable?): FailureKind {
            return when {
                listOf(
                    "Failed to connect",
                    "Unable to resolve host",
                    "The Internet connection appears to be offline.",
                    "-1009"
                ).map { throwable?.message?.contains(it, true) }.contains(true) -> NETWORK
                statusCode in 400..499 -> CLIENT
                else -> SERVER
            }
        }
    }
}
