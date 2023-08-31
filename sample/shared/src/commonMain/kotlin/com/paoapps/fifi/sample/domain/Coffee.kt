package com.paoapps.fifi.sample.domain

import kotlinx.serialization.Serializable

@Serializable
data class Coffee(
    val id: Int,
    val title: String,
    val description: String,
    val ingredients: List<String>,
)
