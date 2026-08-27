package com.paoapps.fifi.utils

import com.paoapps.fifi.ui.component.ConfirmationDialogDefinition
import com.paoapps.fifi.viewmodel.AbstractEvent
import com.paoapps.fifi.viewmodel.VoidEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
        // ActionHandler launches an infinite events.collect job on the given scope;
        // use a child scope we can cancel so runBlocking can complete.
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val handler = ActionHandler<TestAction, TestEvent>(scope) { _, _ -> null }

            val output = handler.toOutput { emitter ->
                emitter.action(TestAction.Navigate)
                "done"
            }

            assertEquals("done", output)
        } finally {
            scope.cancel()
        }
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
