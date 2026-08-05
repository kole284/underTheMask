package com.underthemask.android.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class InputValidationTest {
    @Test
    fun `lobby code is normalized and validated against backend alphabet`() {
        assertEquals("ABC234", InputValidation.normalizeLobbyCode(" abc234 "))
        assertNull(InputValidation.lobbyCodeError("abc234"))
        assertNotNull(InputValidation.lobbyCodeError("ABC10O"))
    }

    @Test
    fun `clue rejects blank oversized and secret word values`() {
        assertNotNull(InputValidation.clueError("  ", "Pizza"))
        assertNotNull(InputValidation.clueError("a".repeat(81), "Pizza"))
        assertNotNull(InputValidation.clueError("pIzZa", "Pizza"))
        assertNull(InputValidation.clueError("Italija", "Pizza"))
    }

    @Test
    fun `suspect selection never exceeds required count`() {
        var selected = emptySet<String>()
        selected = InputValidation.toggleSuspect(selected, "p2", 2)
        selected = InputValidation.toggleSuspect(selected, "p3", 2)
        selected = InputValidation.toggleSuspect(selected, "p4", 2)
        assertEquals(setOf("p2", "p3"), selected)
        assertNull(InputValidation.voteError(selected.size, 2))
        assertNotNull(InputValidation.voteError(1, 2))
    }
}
