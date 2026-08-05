package com.underthemask.android.core.datastore

import com.underthemask.android.core.model.PlayerSession
import kotlinx.coroutines.flow.StateFlow

data class SessionState(
    val isLoaded: Boolean = false,
    val session: PlayerSession? = null,
)

interface SessionManager {
    val state: StateFlow<SessionState>
    fun reconnectToken(): String?
    suspend fun awaitSession(): PlayerSession?
    suspend fun save(session: PlayerSession)
    suspend fun clear()
}
