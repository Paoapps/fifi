package com.paoapps.fifi.model.datacontainer

interface DataProcessor<T> {
    fun process(data: T): T
}
