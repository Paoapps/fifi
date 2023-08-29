package com.paoapps.fifi.model.datacontainer

fun <T> DataContainer<T>.wrap() = CDataContainer(this)

class CDataContainer<T>(val wrapped: DataContainer<T>) : DataContainer<T> by wrapped
