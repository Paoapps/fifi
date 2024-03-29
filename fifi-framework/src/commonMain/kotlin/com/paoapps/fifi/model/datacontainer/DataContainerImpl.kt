package com.paoapps.fifi.model.datacontainer

import com.paoapps.fifi.api.jsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.KSerializer

class DataContainerImpl<T: Any>(
    val serializer: KSerializer<T>,
    val initialData: T,
    val dataPreProcessors: List<DataProcessor<T>> = emptyList(),
): DataContainer<T> {

    private val _dataFlow = MutableStateFlow<T?>(null)
    override val dataFlow = _dataFlow

    override var data: T?
        set(value) {
            val processedData = dataPreProcessors.fold(value) { data, processor -> data?.let { processor.process(it) } }
            _dataFlow.value = processedData
        }
        get() = _dataFlow.value

    override var json: String?
        get() {
            return data?.let { jsonParser.encodeToString<T>(serializer, it) }
        }
        set(value) {
            try {
                val parsedData = value?.let {
                    jsonParser.decodeFromString(serializer, it)
                }
                if (parsedData != null) {
                    data = parsedData
                } else {
                    _dataFlow.value = initialData
                }
            } catch (e: Throwable) {
                println("Cannot parse json. message: ${e.message}")
                if (data == null) {
                    _dataFlow.value = initialData
                }
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