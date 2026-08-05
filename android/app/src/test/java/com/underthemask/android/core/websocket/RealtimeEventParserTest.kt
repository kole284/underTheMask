package com.underthemask.android.core.websocket

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RealtimeEventParserTest {
    private val parser = RealtimeEventParser(Json { ignoreUnknownKeys = true })

    @Test
    fun `routes lobby and game event names without reading private data`() {
        assertEquals(
            LobbyRealtimeEvent.LOBBY_UPDATED,
            parser.parse("""{"type":"LOBBY_UPDATED","payload":{},"occurredAt":"2026-01-01T00:00:00Z"}"""),
        )
        assertEquals(
            LobbyRealtimeEvent.GAME_UPDATED,
            parser.parse("""{"type":"GAME_UPDATED","payload":{"phase":"CLUES"},"occurredAt":"now"}"""),
        )
        assertNull(parser.parse("not-json"))
    }
}
