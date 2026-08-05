package com.underthemask.android.feature.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.underthemask.android.core.model.LobbyStatus
import com.underthemask.android.core.network.AppException
import com.underthemask.android.core.network.ErrorKind
import com.underthemask.android.core.repository.LobbyRepository
import com.underthemask.android.core.ui.AppEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StartupUiState(
    val isLoading: Boolean = true,
    val destination: AppEffect? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val lobbyRepository: LobbyRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StartupUiState())
    val state: StateFlow<StartupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val session = lobbyRepository.currentSession()
            if (session == null) {
                _state.value = StartupUiState(false, AppEffect.OpenHome, null)
                return@launch
            }
            var startupError: String? = null
            val destination = runCatching { lobbyRepository.reconnect() }
                .fold(
                    onSuccess = { lobby ->
                        if (lobby.status == LobbyStatus.WAITING) AppEffect.OpenLobby(lobby.lobbyCode)
                        else AppEffect.OpenGame(lobby.lobbyCode)
                    },
                    onFailure = { throwable ->
                        val appError = (throwable as? AppException)?.error
                        startupError = if (appError?.kind == ErrorKind.NOT_FOUND
                            || appError?.kind == ErrorKind.UNAUTHORIZED
                        ) {
                            "Prethodni lobby vise ne postoji. Lokalna sesija je obrisana."
                        } else {
                            "Reconnect nije uspeo. Sesija je sacuvana; proveri backend adresu i mrezu."
                        }
                        AppEffect.OpenHome
                    },
                )
            _state.value = StartupUiState(false, destination, startupError)
        }
    }
}
