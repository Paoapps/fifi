package com.paoapps.fifi.ui.component

import kotlinx.serialization.Serializable

object ToastDefinition {

    /**
     * Model that contains the information to show a toast. The ID is added to make this model unique.
     * This is required to detect changes / display the same toast multiple times (retry errors for example)
     */
    @Serializable
    data class Properties(
        val title: String? = null,
        val message: String? = null,
        val token: String? = null
    )
}
