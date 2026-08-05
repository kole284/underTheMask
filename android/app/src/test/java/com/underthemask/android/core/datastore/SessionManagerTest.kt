package com.underthemask.android.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.underthemask.android.core.model.PlayerSession
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SessionManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `session is persisted exposed without UI token and cleared`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            File(temporaryFolder.root, "session.preferences_pb")
        }
        val manager = DataStoreSessionManager(dataStore, scope)
        val session = PlayerSession("ABC234", "player-1", "private-token")

        manager.save(session)
        assertEquals(session, manager.state.first { it.session != null }.session)
        assertEquals("private-token", manager.reconnectToken())

        manager.clear()
        assertNull(manager.state.first { it.isLoaded && it.session == null }.session)
    }
}
