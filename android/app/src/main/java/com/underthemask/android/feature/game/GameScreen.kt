package com.underthemask.android.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.model.GameClue
import com.underthemask.android.core.model.GamePhase
import com.underthemask.android.core.model.GamePlayer
import com.underthemask.android.core.model.GamePublicState
import com.underthemask.android.core.model.GameResult
import com.underthemask.android.core.model.GameState
import com.underthemask.android.core.model.GameWinner
import com.underthemask.android.core.model.PlayerRole
import com.underthemask.android.core.ui.ConnectionBanner
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.LoadingScreen
import com.underthemask.android.core.ui.RealtimeLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameUiState,
    onScreenStarted: () -> Unit,
    onScreenStopped: () -> Unit,
    onToggleRole: () -> Unit,
    onClueChange: (String) -> Unit,
    onSubmitClue: () -> Unit,
    onToggleSuspect: (String) -> Unit,
    onSubmitVote: () -> Unit,
    onReset: () -> Unit,
    onLeave: () -> Unit,
    onDismissError: () -> Unit,
) {
    RealtimeLifecycle(onScreenStarted, onScreenStopped)
    var showLeaveDialog by remember { mutableStateOf(false) }
    val gameState = state.gameState

    if (state.isLoading && gameState == null) {
        LoadingScreen("Učitavam partiju...")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lobby ${state.lobby?.lobbyCode.orEmpty()}", style = MaterialTheme.typography.labelMedium)
                        Text(gameState?.game?.phase?.displayName() ?: "Igra", fontWeight = FontWeight.Black)
                    }
                },
                actions = { TextButton(onClick = { showLeaveDialog = true }) { Text("Izađi") } },
            )
        },
        bottomBar = {
            if (gameState != null) {
                GameBottomAction(
                    state = state,
                    gameState = gameState,
                    onClueChange = onClueChange,
                    onSubmitClue = onSubmitClue,
                    onSubmitVote = onSubmitVote,
                    onReset = onReset,
                )
            }
        },
    ) { padding ->
        if (gameState != null) {
            GameContent(
                state = state,
                gameState = gameState,
                modifier = Modifier.fillMaxSize().padding(padding),
                onToggleRole = onToggleRole,
                onToggleSuspect = onToggleSuspect,
                onDismissError = onDismissError,
            )
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Napustiti partiju?") },
            text = { Text("Backend će prekinuti aktivnu rundu i vratiti preostale igrače u lobby.") },
            confirmButton = {
                TextButton(
                    enabled = !state.isActionPending,
                    onClick = {
                        showLeaveDialog = false
                        onLeave()
                    },
                ) { Text("Napusti") }
            },
            dismissButton = { TextButton(onClick = { showLeaveDialog = false }) { Text("Odustani") } },
        )
    }
}

@Composable
private fun GameContent(
    state: GameUiState,
    gameState: GameState,
    modifier: Modifier,
    onToggleRole: () -> Unit,
    onToggleSuspect: (String) -> Unit,
    onDismissError: () -> Unit,
) {
    val game = gameState.game
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "connection") { ConnectionBanner(state.connectionState) }
        state.errorMessage?.let { message ->
            item(key = "error") { InlineError(message, onDismissError) }
        }
        if (game.phase != GamePhase.FINISHED) {
            item(key = "role") {
                RolePanel(gameState, state.roleRevealed, onToggleRole)
            }
        }

        when (game.phase) {
            GamePhase.CLUES -> {
                item(key = "turn-status") {
                    CurrentTurnPanel(game, state.playerId)
                }
                item(key = "turn-order") {
                    TurnOrder(game.players, game.currentPlayerId, state.playerId)
                }
                item(key = "chat-title") { SectionTitle("Chat tragova", "${game.clues.size}/${game.totalPlayers}") }
                if (game.clues.isEmpty()) {
                    item(key = "chat-empty") { EmptyClues() }
                } else {
                    items(game.clues, key = { "clue-${it.playerId}" }) { clue ->
                        ClueBubble(clue, isMine = clue.playerId == state.playerId)
                    }
                }
            }
            GamePhase.VOTING -> {
                item(key = "chat-title") { SectionTitle("Svi tragovi", game.clues.size.toString()) }
                items(game.clues, key = { "clue-${it.playerId}" }) { clue ->
                    ClueBubble(clue, isMine = clue.playerId == state.playerId)
                }
                item(key = "vote-title") {
                    SectionTitle(
                        "Izaberi osumnjičene",
                        "${state.selectedPlayerIds.size}/${game.requiredSuspectCount}",
                    )
                }
                if (gameState.hasSubmittedVote) {
                    item(key = "vote-complete") {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                            Text(
                                "Glas je zabeležen. Glasalo je ${game.votesSubmitted}/${game.totalPlayers} igrača.",
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                } else {
                    items(
                        game.players.filter { it.playerId != state.playerId },
                        key = { "vote-${it.playerId}" },
                    ) { player ->
                        VotePlayerRow(
                            player = player,
                            selected = player.playerId in state.selectedPlayerIds,
                            enabled = !state.isActionPending && (
                                player.playerId in state.selectedPlayerIds
                                    || state.selectedPlayerIds.size < game.requiredSuspectCount
                                ),
                            onToggle = { onToggleSuspect(player.playerId) },
                        )
                    }
                }
            }
            GamePhase.FINISHED -> {
                game.result?.let { result ->
                    item(key = "result") { ResultPanel(result, game.players) }
                    item(key = "tally-title") { SectionTitle("Glasovi", game.totalPlayers.toString()) }
                    items(result.tallies.sortedByDescending { it.votes }, key = { "tally-${it.playerId}" }) { tally ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(tally.playerName, fontWeight = FontWeight.SemiBold)
                                Text(tally.votes.toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RolePanel(gameState: GameState, revealed: Boolean, onToggle: () -> Unit) {
    val isImpostor = gameState.role == PlayerRole.IMPOSTOR
    Surface(
        color = if (isImpostor) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Tvoja uloga", style = MaterialTheme.typography.labelMedium)
                    Text(
                        if (revealed) gameState.role.name else "SAKRIVENO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                OutlinedButton(onClick = onToggle) { Text(if (revealed) "Sakrij" else "Otkrij") }
            }
            if (revealed) {
                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = MaterialTheme.shapes.small) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(if (isImpostor) "Tvoj hint" else "Tajna reč", style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (isImpostor) gameState.hint else gameState.secretWord.orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        if (!isImpostor) {
                            Text("Kategorija: ${gameState.hint}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                Text("Privatni podatak je sakriven.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CurrentTurnPanel(game: GamePublicState, playerId: String?) {
    val currentPlayer = game.players.firstOrNull { it.playerId == game.currentPlayerId }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Na potezu", style = MaterialTheme.typography.labelMedium)
            Text(
                if (game.currentPlayerId == playerId) "Ti daješ trag" else currentPlayer?.playerName.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun TurnOrder(players: List<GamePlayer>, currentPlayerId: String?, playerId: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Redosled poteza", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 16.dp)) {
            items(players, key = { "turn-${it.playerId}" }) { player ->
                val active = player.playerId == currentPlayerId
                Surface(
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        buildString {
                            append(player.playerName)
                            if (player.playerId == playerId) append(" (ti)")
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(count, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyClues() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
        Text(
            "Prvi trag još nije poslat.",
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ClueBubble(clue: GameClue, isMine: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            modifier = Modifier.fillMaxWidth(0.86f).widthIn(max = 420.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            Text(
                if (isMine) "${clue.playerName} (ti)" else clue.playerName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    clue.clue,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun VotePlayerRow(player: GamePlayer, selected: Boolean, enabled: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        onClick = onToggle,
        enabled = enabled || selected,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() }, enabled = enabled || selected)
            Text(player.playerName, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ResultPanel(result: GameResult, players: List<GamePlayer>) {
    val crewmatesWon = result.winner == GameWinner.CREWMATES
    val impostorNames = result.impostorPlayerIds.mapNotNull { id ->
        players.firstOrNull { it.playerId == id }?.playerName
    }
    Surface(
        color = if (crewmatesWon) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Pobednici", style = MaterialTheme.typography.labelMedium)
            Text(
                if (crewmatesWon) "IGRAČI" else "IMPOSTORI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text("Tajna reč: ${result.secretWord}", fontWeight = FontWeight.Bold)
            Text("Impostor${if (impostorNames.size > 1) "i" else ""}: ${impostorNames.joinToString()}")
            if (result.tie) {
                Text(
                    "Nerešen rezultat ide u korist impostora.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GameBottomAction(
    state: GameUiState,
    gameState: GameState,
    onClueChange: (String) -> Unit,
    onSubmitClue: () -> Unit,
    onSubmitVote: () -> Unit,
    onReset: () -> Unit,
) {
    val game = gameState.game
    Surface(tonalElevation = 4.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (game.phase) {
                GamePhase.CLUES -> if (state.isMyTurn) {
                    OutlinedTextField(
                        value = state.clueInput,
                        onValueChange = onClueChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tvoj trag") },
                        supportingText = { Text("${state.clueInput.length}/80") },
                        singleLine = true,
                    )
                    Button(modifier = Modifier.fillMaxWidth(), enabled = state.canSubmitClue, onClick = onSubmitClue) {
                        Text(if (state.isActionPending) "Šaljem..." else "Pošalji trag")
                    }
                } else {
                    val current = game.players.firstOrNull { it.playerId == game.currentPlayerId }?.playerName
                    Text(
                        "Čeka se trag igrača ${current.orEmpty()}.",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
                GamePhase.VOTING -> if (gameState.hasSubmittedVote) {
                    Text(
                        "Čeka se da svi igrači glasaju (${game.votesSubmitted}/${game.totalPlayers}).",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                } else {
                    Button(modifier = Modifier.fillMaxWidth(), enabled = state.canSubmitVote, onClick = onSubmitVote) {
                        Text(if (state.isActionPending) "Šaljem..." else "Potvrdi glas")
                    }
                }
                GamePhase.FINISHED -> if (state.isHost) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isActionPending,
                        onClick = onReset,
                    ) { Text(if (state.isActionPending) "Obrada..." else "Nazad u lobby") }
                } else {
                    Text("Čeka se host za novu rundu.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

private fun GamePhase.displayName() = when (this) {
    GamePhase.CLUES -> "Tragovi"
    GamePhase.VOTING -> "Glasanje"
    GamePhase.FINISHED -> "Rezultat"
}
