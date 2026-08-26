package com.paoapps.fifi.utils

import com.paoapps.fifi.ui.component.ConfirmationDialogDefinition
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.VoidEvent
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private sealed class TestAction {
    data object Navigate : TestAction()
}

private sealed class TestEvent : AbstractEvent() {
    data object Tap : TestEvent()
}

class ActionHandlerTest {

    @Test
    fun toOutputUsesEmitter() = runBlocking {
        val handler = ActionHandler<TestAction, TestEvent>(this) { _, _ -> null }

        val output = handler.toOutput { emitter ->
            emitter.action(TestAction.Navigate)
            "done"
        }

        assertEquals("done", output)
    }

    @Test
    fun confirmationDialogPropertiesAreSupported() {
        val dialog = ConfirmationDialogDefinition.Properties(
            title = "Delete?",
            message = "This cannot be undone",
            buttons = listOf(
                ConfirmationDialogDefinition.Properties.Button(
                    label = "Delete",
                    onClick = VoidEvent,
                    role = ConfirmationDialogDefinition.Properties.Role.DESTRUCTIVE,
                ),
            ),
            cancelLabel = "Cancel",
        )
        assertEquals("Delete?", dialog.title)
        assertTrue(dialog.buttons.single().role == ConfirmationDialogDefinition.Properties.Role.DESTRUCTIVE)
    }
}
