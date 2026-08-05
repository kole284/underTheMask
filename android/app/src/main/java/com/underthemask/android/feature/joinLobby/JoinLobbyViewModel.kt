package com.underthemask.android.feature.joinLobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.underthemask.android.core.model.InputValidation
import com.underthemask.android.core.repository.LobbyRepository
import com.underthemask.android.core.ui.AppEffect
import com.underthemask.android.core.ui.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinLobbyUiState(
    val playerName: String = "",
    val lobbyCode: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting
        && InputValidation.playerNameError(playerName) == null
        && InputValidation.lobbyCodeError(lobbyCode) == null
}

@HiltViewModel
class JoinLobbyViewModel @Inject constructor(
    private val repository: LobbyRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(JoinLobbyUiState())
    private val _effects = MutableSharedFlow<AppEffect>(extraBufferCapacity = 1)
    val state: StateFlow<JoinLobbyUiState> = _state.asStateFlow()
    val effects: SharedFlow<AppEffect> = _effects.asSharedFlow()

    fun setPlayerName(value: String) = _state.update { it.copy(playerName = value.take(32), errorMessage = null) }
    fun setLobbyCode(value: String) = _state.update {
        it.copy(lobbyCode = InputValidation.normalizeLobbyCode(value).take(6), errorMessage = null)
    }

    fun submit() {
        val snapshot = _state.value
        val validationError = InputValidation.playerNameError(snapshot.playerName)
            ?: InputValidation.lobbyCodeError(snapshot.lobbyCode)
        if (snapshot.isSubmitting || validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching { repository.joinLobby(snapshot.lobbyCode, snapshot.playerName) }
                .onSuccess { session -> _effects.emit(AppEffect.OpenLobby(session.lobbyCode)) }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.userMessage()) } }
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
