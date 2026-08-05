package com.underthemask.android.feature.createLobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.underthemask.android.core.model.GameSettings
import com.underthemask.android.core.model.HintType
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

data class CreateLobbyUiState(
    val hostName: String = "",
    val impostorCount: Int = 1,
    val hintType: HintType = HintType.CATEGORY,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean get() = !isSubmitting && InputValidation.playerNameError(hostName) == null
}

@HiltViewModel
class CreateLobbyViewModel @Inject constructor(
    private val repository: LobbyRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreateLobbyUiState())
    private val _effects = MutableSharedFlow<AppEffect>(extraBufferCapacity = 1)
    val state: StateFlow<CreateLobbyUiState> = _state.asStateFlow()
    val effects: SharedFlow<AppEffect> = _effects.asSharedFlow()

    fun setHostName(value: String) = _state.update { it.copy(hostName = value.take(32), errorMessage = null) }
    fun setImpostorCount(value: Int) = _state.update { it.copy(impostorCount = value, errorMessage = null) }
    fun setHintType(value: HintType) = _state.update { it.copy(hintType = value, errorMessage = null) }

    fun submit() {
        val snapshot = _state.value
        val validationError = InputValidation.playerNameError(snapshot.hostName)
        if (snapshot.isSubmitting || validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                repository.createLobby(
                    snapshot.hostName,
                    GameSettings(snapshot.impostorCount, snapshot.hintType),
                )
            }.onSuccess { session ->
                _effects.emit(AppEffect.OpenLobby(session.lobbyCode))
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.userMessage()) }
            }
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
