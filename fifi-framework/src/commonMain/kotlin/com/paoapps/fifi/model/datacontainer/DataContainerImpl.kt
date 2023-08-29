package com.paoapps.fifi.model.datacontainer

import com.paoapps.fifi.api.jsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.KSerializer

class DataContainerImpl<T>(val serializer: KSerializer<T>, val dataPreProcessors: List<DataProcessor<T>>): DataContainer<T> {

    private val _dataFlow = MutableStateFlow<T?>(null)
    override val dataFlow = _dataFlow

    override var data: T?
        set(value) {
            val processedData = dataPreProcessors.fold(value) { data, processor -> data?.let(processor::process) }
            _dataFlow.value = processedData
        }
        get() = _dataFlow.value

    override var json: String?
        get() {
            return data?.let { jsonParser.encodeToString<T>(serializer, it) }
        }
        set(value) {
            try {
                data = value?.let { jsonParser.decodeFromString(serializer, it) }
            } catch (e: Throwable) {
                // If parsing of provided json fails ignore the json.
                println("Cannot parse json, ignore it., message: ${e.message}")
            }
        }

    override fun updateJson(json: String?, deleteWhenInvalid: Boolean) {
        try {
            this.json = json
        } catch (e: RuntimeException) {
            if (deleteWhenInvalid) {
                this.json = null
            } else {
                throw e
            }
        }
    }

}