package com.underthemask.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.underthemask.android.core.di.ApplicationScope
import com.underthemask.android.core.model.PlayerSession
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@Singleton
class DataStoreSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope scope: CoroutineScope,
) : SessionManager {
    override val state = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw throwable
        }
        .map { preferences ->
            val code = preferences[LOBBY_CODE]
            val playerId = preferences[PLAYER_ID]
            val token = preferences[RECONNECT_TOKEN]
            val session = if (code != null && playerId != null && token != null) {
                PlayerSession(code, playerId, token)
            } else {
                null
            }
            SessionState(isLoaded = true, session = session)
        }
        .stateIn(scope, SharingStarted.Eagerly, SessionState())

    override fun reconnectToken(): String? = state.value.session?.reconnectToken

    override suspend fun awaitSession(): PlayerSession? = state.first { it.isLoaded }.session

    override suspend fun save(session: PlayerSession) {
        dataStore.edit { preferences ->
            preferences[LOBBY_CODE] = session.lobbyCode
            preferences[PLAYER_ID] = session.playerId
            preferences[RECONNECT_TOKEN] = session.reconnectToken
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val LOBBY_CODE = stringPreferencesKey("lobby_code")
        val PLAYER_ID = stringPreferencesKey("player_id")
        val RECONNECT_TOKEN = stringPreferencesKey("reconnect_token")
    }
}
