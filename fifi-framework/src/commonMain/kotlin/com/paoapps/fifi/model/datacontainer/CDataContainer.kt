package com.paoapps.fifi.model.datacontainer

fun <T: Any> DataContainer<T>.wrap() = CDataContainer(this)

class CDataContainer<T: Any>(val wrapped: DataContainer<T>) : DataContainer<T> by wrapped
