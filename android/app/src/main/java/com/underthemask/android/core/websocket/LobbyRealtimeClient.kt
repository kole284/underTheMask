package com.underthemask.android.core.websocket

import com.underthemask.android.core.config.BackendConfig
import com.underthemask.android.core.di.ApplicationScope
import com.underthemask.android.core.network.RealtimeEventTypeDto
import com.underthemask.android.core.network.RealtimeSignalDto
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }
enum class LobbyRealtimeEvent { LOBBY_UPDATED, GAME_UPDATED }

interface LobbyRealtimeClient {
    val connectionState: StateFlow<ConnectionState>
    val events: SharedFlow<LobbyRealtimeEvent>
    suspend fun connect(lobbyCode: String)
    suspend fun disconnect()
}

@Singleton
class StompLobbyClient @Inject constructor(
    @Named("websocket") okHttpClient: OkHttpClient,
    private val eventParser: RealtimeEventParser,
    @param:ApplicationScope private val scope: CoroutineScope,
) : LobbyRealtimeClient {
    private val client = StompClient(OkHttpWebSocketClient(okHttpClient))
    private val controlMutex = Mutex()
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _events = MutableSharedFlow<LobbyRealtimeEvent>(extraBufferCapacity = 16)
    private var activeCode: String? = null
    private var connectionJob: Job? = null

    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    override val events: SharedFlow<LobbyRealtimeEvent> = _events.asSharedFlow()

    override suspend fun connect(lobbyCode: String) {
        val normalizedCode = lobbyCode.trim().uppercase()
        controlMutex.withLock {
            if (activeCode == normalizedCode && connectionJob?.isActive == true) return
            stopLocked()
            activeCode = normalizedCode
            connectionJob = scope.launch { runConnectionLoop(normalizedCode) }
        }
    }

    override suspend fun disconnect() {
        controlMutex.withLock {
            activeCode = null
            stopLocked()
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private suspend fun stopLocked() {
        connectionJob?.cancelAndJoin()
        connectionJob = null
    }

    private suspend fun runConnectionLoop(code: String) {
        var retryDelayMs = 1_000L
        while (scope.isActive && activeCode == code) {
            var session: StompSession? = null
            try {
                _connectionState.value = ConnectionState.CONNECTING
                session = client.connect(BackendConfig.wsUrl)
                _connectionState.value = ConnectionState.CONNECTED
                retryDelayMs = 1_000L
                session.subscribeText("/topic/lobbies/$code").collect(::handleMessage)
                _connectionState.value = ConnectionState.ERROR
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                _connectionState.value = ConnectionState.ERROR
            } finally {
                if (session != null) {
                    withContext(NonCancellable) {
                        withTimeoutOrNull(1_500) { runCatching { session.disconnect() } }
                    }
                }
            }

            if (activeCode == code) {
                _connectionState.value = ConnectionState.DISCONNECTED
                delay(retryDelayMs)
                retryDelayMs = (retryDelayMs * 2).coerceAtMost(10_000L)
            }
        }
    }

    private fun handleMessage(body: String) {
        eventParser.parse(body)?.let(_events::tryEmit)
    }
}

class RealtimeEventParser @Inject constructor(private val json: Json) {
    fun parse(body: String): LobbyRealtimeEvent? {
        val signal = runCatching { json.decodeFromString<RealtimeSignalDto>(body) }.getOrNull() ?: return null
        return when (signal.type) {
            RealtimeEventTypeDto.LOBBY_UPDATED -> LobbyRealtimeEvent.LOBBY_UPDATED
            RealtimeEventTypeDto.GAME_UPDATED -> LobbyRealtimeEvent.GAME_UPDATED
        }
    }
}
