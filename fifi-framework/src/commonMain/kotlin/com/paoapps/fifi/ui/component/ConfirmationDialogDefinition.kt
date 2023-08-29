package com.paoapps.fifi.ui.component

object ConfirmationDialogDefinition {
    data class Properties<Event>(
        val title: String? = null,
        val message: String? = null,
        val buttons: List<Button<Event>>,
        val cancelLabel: String
    ) {
        data class Button<Event>(
            val label: String,
            val onClick: Event? = null,
            val role: Role = Role.DEFAULT
        )

        enum class Role {
            DEFAULT,
            DESTRUCTIVE
        }
    }
}
