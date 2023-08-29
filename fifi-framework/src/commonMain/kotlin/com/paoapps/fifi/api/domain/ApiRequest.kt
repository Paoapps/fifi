package com.paoapps.fifi.api.domain

import kotlinx.serialization.Serializable

@Serializable
class DataHolder<T>(val data: T)