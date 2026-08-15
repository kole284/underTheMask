package com.underthemask.android.feature

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.underthemask.android.core.model.GamePhase
import com.underthemask.android.core.model.GamePlayer
import com.underthemask.android.core.model.GamePublicState
import com.underthemask.android.core.model.GameSettings
import com.underthemask.android.core.model.GameState
import com.underthemask.android.core.model.HintType
import com.underthemask.android.core.model.Lobby
import com.underthemask.android.core.model.LobbyStatus
import com.underthemask.android.core.model.PlayerRole
import com.underthemask.android.core.ui.theme.UnderTheMaskTheme
import com.underthemask.android.core.websocket.ConnectionState
import com.underthemask.android.feature.game.GameScreen
import com.underthemask.android.feature.game.GameUiState
import org.junit.Rule
import org.junit.Test

class GameScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun currentPlayerSeesClueInput() {
        composeRule.setContent {
            UnderTheMaskTheme(darkTheme = true) {
                GameScreen(
                    state = uiState(GamePhase.CLUES, currentPlayerId = "p1"),
                    onScreenStarted = {},
                    onScreenStopped = {},
                    onToggleRole = {},
                    onClueChange = {},
                    onSubmitClue = {},
                    onToggleSuspect = {},
                    onSubmitVote = {},
                    onReset = {},
                    onLeave = {},
                    onDismissError = {},
                )
            }
        }

        composeRule.onNodeWithText("Tvoj trag").assertExists()
        composeRule.onNodeWithText("Pošalji trag").assertExists()
    }

    @Test
    fun exactVotingSelectionEnablesSubmit() {
        composeRule.setContent {
            UnderTheMaskTheme(darkTheme = true) {
                GameScreen(
                    state = uiState(GamePhase.VOTING).copy(selectedPlayerIds = setOf("p2")),
                    onScreenStarted = {},
                    onScreenStopped = {},
                    onToggleRole = {},
                    onClueChange = {},
                    onSubmitClue = {},
                    onToggleSuspect = {},
                    onSubmitVote = {},
                    onReset = {},
                    onLeave = {},
                    onDismissError = {},
                )
            }
        }

        composeRule.onNodeWithText("Potvrdi glas").assertIsEnabled()
    }

    private fun uiState(phase: GamePhase, currentPlayerId: String? = null): GameUiState {
        val players = listOf(
            GamePlayer("p1", "Mina", true),
            GamePlayer("p2", "Luka", true),
            GamePlayer("p3", "Sara", true),
        )
        return GameUiState(
            gameState = GameState(
                game = GamePublicState(
                    roundId = "round-1",
                    phase = phase,
                    currentPlayerId = currentPlayerId,
                    players = players,
                    clues = emptyList(),
                    votesSubmitted = 0,
                    totalPlayers = 3,
                    requiredSuspectCount = 1,
                    result = null,
                ),
                role = PlayerRole.CREWMATE,
                secretWord = "Pizza",
                hint = "Hrana",
                hasSubmittedVote = false,
            ),
            lobby = Lobby(
                lobbyCode = "ABC234",
                status = LobbyStatus.IN_GAME,
                hostPlayerId = "p1",
                settings = GameSettings(1, HintType.CATEGORY),
                players = emptyList(),
                playerCount = 3,
                maxPlayers = 12,
            ),
            playerId = "p1",
            isLoading = false,
            connectionState = ConnectionState.CONNECTED,
        )
    }
}
