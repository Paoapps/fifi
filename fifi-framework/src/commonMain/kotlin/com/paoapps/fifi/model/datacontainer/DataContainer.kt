package com.paoapps.fifi.model.datacontainer

import kotlinx.coroutines.flow.Flow

interface DataContainer<T: Any> {
    var data: T?
    val dataFlow: Flow<T?>

    var json: String?

    fun updateJson(json: String?, deleteWhenInvalid: Boolean)
}

inline fun <T: Any> DataContainer<T>.updateData(update: (T) -> T) {
    data = data?.let(update)
}
