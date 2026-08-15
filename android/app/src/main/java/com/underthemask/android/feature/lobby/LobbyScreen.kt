package com.underthemask.android.feature.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.underthemask.android.core.model.HintType
import com.underthemask.android.core.model.Lobby
import com.underthemask.android.core.model.Player
import com.underthemask.android.core.ui.ConnectionBanner
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.LoadingScreen
import com.underthemask.android.core.ui.RealtimeLifecycle
import com.underthemask.android.core.ui.SegmentedChoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    state: LobbyUiState,
    onScreenStarted: () -> Unit,
    onScreenStopped: () -> Unit,
    onImpostorCountChange: (Int) -> Unit,
    onHintTypeChange: (HintType) -> Unit,
    onStartGame: () -> Unit,
    onLeave: () -> Unit,
    onDismissError: () -> Unit,
) {
    RealtimeLifecycle(onScreenStarted, onScreenStopped)
    var showLeaveDialog by remember { mutableStateOf(false) }
    val lobby = state.lobby

    if (state.isLoading && lobby == null) {
        LoadingScreen("Učitavam lobby...")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lobby", style = MaterialTheme.typography.labelMedium)
                        Text(lobby?.lobbyCode ?: "------", fontWeight = FontWeight.Black)
                    }
                },
                actions = { TextButton(onClick = { showLeaveDialog = true }) { Text("Izađi") } },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (state.isHost) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.canStart,
                            onClick = onStartGame,
                        ) {
                            Text(if (state.isActionPending) "Obrada..." else "Pokreni igru")
                        }
                    } else {
                        Text(
                            "Čekaš hosta da pokrene igru.",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (lobby != null) {
            LobbyContent(
                lobby = lobby,
                state = state,
                modifier = Modifier.fillMaxSize().padding(padding),
                onImpostorCountChange = onImpostorCountChange,
                onHintTypeChange = onHintTypeChange,
                onDismissError = onDismissError,
            )
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Napustiti lobby?") },
            text = { Text("Tvoja lokalna sesija biće obrisana. Ako je partija aktivna, backend vraća ostale igrače u lobby.") },
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
private fun LobbyContent(
    lobby: Lobby,
    state: LobbyUiState,
    modifier: Modifier,
    onImpostorCountChange: (Int) -> Unit,
    onHintTypeChange: (HintType) -> Unit,
    onDismissError: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "connection") { ConnectionBanner(state.connectionState) }
        state.errorMessage?.let { message ->
            item(key = "error") { InlineError(message, onDismissError) }
        }
        item(key = "summary") {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Status", style = MaterialTheme.typography.labelMedium)
                        Text(lobby.status.name, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Igrači", style = MaterialTheme.typography.labelMedium)
                        Text("${lobby.playerCount}/${lobby.maxPlayers}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item(key = "settings") {
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Podešavanja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (state.isHost) "Host kontrola" else "Samo host",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    SegmentedChoice(
                        label = "Broj impostora",
                        selected = lobby.settings.impostorCount,
                        options = listOf(1 to "1", 2 to "2"),
                        enabled = state.isHost && !state.isActionPending,
                        onSelected = onImpostorCountChange,
                    )
                    SegmentedChoice(
                        label = "Pomoć za impostora",
                        selected = lobby.settings.hintType,
                        options = listOf(HintType.CATEGORY to "Kategorija", HintType.ASSOCIATION to "Asocijacija"),
                        enabled = state.isHost && !state.isActionPending,
                        onSelected = onHintTypeChange,
                    )
                }
            }
        }
        item(key = "players-heading") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Igrači", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Minimum 3", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(lobby.players, key = { it.playerId }) { player ->
            PlayerRow(player, isCurrentPlayer = player.playerId == state.playerId)
        }
    }
}

@Composable
private fun PlayerRow(player: Player, isCurrentPlayer: Boolean) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = if (isCurrentPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        player.playerName.firstOrNull()?.uppercase() ?: "?",
                        color = if (isCurrentPlayer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(player.playerName, fontWeight = FontWeight.Bold)
                Text(
                    buildString {
                        append(if (player.isHost) "Host" else "Igrač")
                        if (isCurrentPlayer) append(" - ti")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                if (player.connected) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = if (player.connected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
}
