package com.underthemask.android.feature.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.underthemask.android.core.ui.AppBackground
import com.underthemask.android.core.ui.AppConfirmDialog
import com.underthemask.android.core.ui.AppPanel
import com.underthemask.android.core.ui.AppTextField
import com.underthemask.android.core.ui.AppTopBar
import com.underthemask.android.core.ui.BottomActionSurface
import com.underthemask.android.core.ui.ConnectionBanner
import com.underthemask.android.core.ui.InlineError
import com.underthemask.android.core.ui.LoadingScreen
import com.underthemask.android.core.ui.PlayerAvatar
import com.underthemask.android.core.ui.PrimaryAction
import com.underthemask.android.core.ui.RealtimeLifecycle
import com.underthemask.android.core.ui.SectionHeader
import com.underthemask.android.core.ui.StatusPill
import com.underthemask.android.core.ui.theme.AppColors

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

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                AppTopBar(
                    title = gameState?.game?.phase?.displayName() ?: "Igra",
                    eyebrow = "Lobby ${state.lobby?.lobbyCode.orEmpty()}  •  ${gameState?.game?.totalPlayers ?: 0} igrača",
                    actionLabel = "Izađi",
                    onAction = { showLeaveDialog = true },
                )
            },
            bottomBar = {
                gameState?.let {
                    GameBottomAction(
                        state = state,
                        gameState = it,
                        onClueChange = onClueChange,
                        onSubmitClue = onSubmitClue,
                        onSubmitVote = onSubmitVote,
                        onReset = onReset,
                    )
                }
            },
        ) { padding ->
            gameState?.let {
                GameContent(
                    state = state,
                    gameState = it,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onToggleRole = onToggleRole,
                    onToggleSuspect = onToggleSuspect,
                    onDismissError = onDismissError,
                )
            }
        }
    }

    if (showLeaveDialog) {
        AppConfirmDialog(
            title = "Napustiti partiju?",
            message = "Aktivna runda biće prekinuta, a preostali igrači vraćeni u lobby.",
            confirmLabel = "Napusti partiju",
            confirmEnabled = !state.isActionPending,
            onConfirm = {
                showLeaveDialog = false
                onLeave()
            },
            onDismiss = { showLeaveDialog = false },
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
    BoxWithConstraints(modifier) {
        val wide = maxWidth >= 720.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = 980.dp).align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "connection") { ConnectionBanner(state.connectionState) }
            state.errorMessage?.let { message ->
                item(key = "error") { InlineError(message, onDismissError) }
            }

            when (game.phase) {
                GamePhase.CLUES -> {
                    item(key = "game-state") {
                        if (wide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                RolePanel(gameState, state.roleRevealed, onToggleRole, Modifier.weight(1f))
                                CurrentTurnPanel(game, state.playerId, Modifier.weight(1f))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                CurrentTurnPanel(game, state.playerId)
                                RolePanel(gameState, state.roleRevealed, onToggleRole)
                            }
                        }
                    }
                    item(key = "turn-order") { TurnOrder(game.players, game.currentPlayerId, state.playerId) }
                    item(key = "clue-feed") {
                        ClueTimeline(
                            clues = game.clues,
                            playerId = state.playerId,
                            totalPlayers = game.totalPlayers,
                            title = "Tok tragova",
                        )
                    }
                }

                GamePhase.VOTING -> {
                    item(key = "vote-intro") {
                        VotingIntro(game.requiredSuspectCount, state.selectedPlayerIds.size, gameState.hasSubmittedVote)
                    }
                    item(key = "role") { RolePanel(gameState, state.roleRevealed, onToggleRole) }
                    item(key = "clue-feed") {
                        ClueTimeline(game.clues, state.playerId, game.totalPlayers, "Tragovi za odluku")
                    }
                    if (gameState.hasSubmittedVote) {
                        item(key = "vote-complete") {
                            VoteSubmitted(game.votesSubmitted, game.totalPlayers)
                        }
                    } else {
                        item(key = "vote-grid") {
                            VoteGrid(
                                players = game.players.filter { it.playerId != state.playerId },
                                selectedPlayerIds = state.selectedPlayerIds,
                                requiredCount = game.requiredSuspectCount,
                                actionPending = state.isActionPending,
                                twoColumns = wide,
                                onToggleSuspect = onToggleSuspect,
                            )
                        }
                    }
                }

                GamePhase.FINISHED -> {
                    game.result?.let { result ->
                        item(key = "result") { ResultHero(result, game.players) }
                        item(key = "tally") { VoteTallyPanel(result) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RolePanel(
    gameState: GameState,
    revealed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImpostor = gameState.role == PlayerRole.IMPOSTOR
    val accent = if (isImpostor) AppColors.Impostor else AppColors.Crew
    val panelColor by animateColorAsState(
        if (revealed) accent.copy(alpha = 0.13f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        label = "rolePanelColor",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics {
                role = Role.Button
                contentDescription = if (revealed) "Sakrij privatnu ulogu" else "Otkrij privatnu ulogu"
            },
        color = panelColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (revealed) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = if (revealed) 12.dp else 4.dp,
        onClick = onToggle,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        listOf(accent.copy(alpha = if (revealed) 0.12f else 0.03f), Color.Transparent),
                    ),
                )
                .padding(20.dp),
        ) {
            AnimatedContent(
                targetState = revealed,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.98f)) },
                label = "roleReveal",
            ) { isRevealed ->
                if (!isRevealed) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("?", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text("PRIVATNA ULOGA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Dodirni da otkriješ ulogu", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Text(
                            "Zakloni ekran od drugih igrača.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Text("TVOJA ULOGA", style = MaterialTheme.typography.labelSmall, color = accent)
                        Text(
                            if (isImpostor) "IMPOSTOR" else "CREWMATE",
                            style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
                            color = accent,
                            textAlign = TextAlign.Center,
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    if (isImpostor) "TVOJA POMOĆ" else "TAJNA REČ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    if (isImpostor) gameState.hint else gameState.secretWord.orEmpty(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                )
                                if (!isImpostor) {
                                    Text(
                                        "Kategorija: ${gameState.hint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        Text("Dodirni ponovo da sakriješ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentTurnPanel(game: GamePublicState, playerId: String?, modifier: Modifier = Modifier) {
    val currentPlayer = game.players.firstOrNull { it.playerId == game.currentPlayerId }
    val isMine = game.currentPlayerId == playerId
    AppPanel(modifier = modifier, highlighted = isMine) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("NA POTEZU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                StatusPill(if (isMine) "Tvoj potez" else "Čekanje")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PlayerAvatar(currentPlayer?.playerName.orEmpty(), emphasized = isMine, size = 52.dp)
                Column {
                    Text(
                        if (isMine) "Ti daješ trag" else currentPlayer?.playerName.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        if (isMine) "Daj jednu reč koja vodi ekipu." else "Prati tok i spremi svoj trag.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnOrder(players: List<GamePlayer>, currentPlayerId: String?, playerId: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionHeader("Runda", "Redosled poteza", "${players.size} igrača")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 16.dp)) {
            items(players, key = { "turn-${it.playerId}" }) { player ->
                val active = player.playerId == currentPlayerId
                val dotColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                Surface(
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Canvas(Modifier.size(6.dp)) { drawCircle(dotColor) }
                        Text(
                            buildString {
                                append(player.playerName)
                                if (player.playerId == playerId) append(" (ti)")
                            },
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClueTimeline(clues: List<GameClue>, playerId: String?, totalPlayers: Int, title: String) {
    AppPanel {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader("Istorija", title, "${clues.size}/$totalPlayers")
            if (clues.isEmpty()) {
                EmptyClues()
            } else {
                clues.forEachIndexed { index, clue ->
                    ClueEntry(clue, index + 1, isMine = clue.playerId == playerId)
                }
            }
        }
    }
}

@Composable
private fun EmptyClues() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Box(contentAlignment = Alignment.Center) { Text("…", style = MaterialTheme.typography.titleLarge) }
        }
        Text("Prvi trag još nije poslat.", style = MaterialTheme.typography.titleSmall)
        Text("Ovde će se pojaviti tok runde.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ClueEntry(clue: GameClue, turn: Int, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        PlayerAvatar(clue.playerName, emphasized = isMine, size = 38.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (isMine) "${clue.playerName} (ti)" else clue.playerName,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text("TRAG $turn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMine) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Text(
                    clue.clue,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun VotingIntro(required: Int, selected: Int, submitted: Boolean) {
    AppPanel(highlighted = !submitted) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VREME JE ZA ODLUKU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                StatusPill(if (submitted) "Glas poslato" else "$selected/$required izabrano")
            }
            Text(
                if (submitted) "Tvoj glas je zabeležen" else "Ko je impostor?",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                if (submitted) "Sačekaj da ostatak ekipe završi glasanje."
                else "Izaberi ${if (required == 1) "jednog igrača" else "$required igrača"} na osnovu njihovih tragova.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VoteSubmitted(votesSubmitted: Int, totalPlayers: Int) {
    AppPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = AppColors.Success.copy(alpha = 0.14f),
                contentColor = AppColors.Success,
            ) {
                Box(contentAlignment = Alignment.Center) { Text("✓", color = AppColors.Success, style = MaterialTheme.typography.titleLarge) }
            }
            Column {
                Text("Glas je zabeležen", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Glasalo je $votesSubmitted/$totalPlayers igrača.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VoteGrid(
    players: List<GamePlayer>,
    selectedPlayerIds: Set<String>,
    requiredCount: Int,
    actionPending: Boolean,
    twoColumns: Boolean,
    onToggleSuspect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Osumnjičeni", "Izaberi ${selectedPlayerIds.size} od $requiredCount")
        if (twoColumns) {
            players.chunked(2).forEach { rowPlayers ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowPlayers.forEach { player ->
                        VotePlayerCard(
                            player = player,
                            selected = player.playerId in selectedPlayerIds,
                            enabled = !actionPending && (
                                player.playerId in selectedPlayerIds || selectedPlayerIds.size < requiredCount
                            ),
                            modifier = Modifier.weight(1f),
                            onToggle = { onToggleSuspect(player.playerId) },
                        )
                    }
                    if (rowPlayers.size == 1) Box(Modifier.weight(1f))
                }
            }
        } else {
            players.forEach { player ->
                VotePlayerCard(
                    player = player,
                    selected = player.playerId in selectedPlayerIds,
                    enabled = !actionPending && (
                        player.playerId in selectedPlayerIds || selectedPlayerIds.size < requiredCount
                    ),
                    onToggle = { onToggleSuspect(player.playerId) },
                )
            }
        }
    }
}

@Composable
private fun VotePlayerCard(
    player: GamePlayer,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
) {
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "voteBorder",
    )
    val surfaceColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "voteSurface",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.selected = selected
                role = Role.Checkbox
                contentDescription = "Glas za ${player.playerName}"
            },
        shape = MaterialTheme.shapes.large,
        color = surfaceColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        enabled = enabled || selected,
        onClick = onToggle,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerAvatar(player.playerName, emphasized = selected, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(player.playerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        selected -> "Izabran kao osumnjičeni"
                        !enabled -> "Broj izbora je popunjen"
                        else -> "Dodirni za izbor"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(selected, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHero(result: GameResult, players: List<GamePlayer>) {
    val crewmatesWon = result.winner == GameWinner.CREWMATES
    val accent = if (crewmatesWon) AppColors.Crew else AppColors.Impostor
    val impostorNames = result.impostorPlayerIds.mapNotNull { id ->
        players.firstOrNull { it.playerId == id }?.playerName
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.1f), Color.Transparent)))
                .padding(horizontal = 22.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusPill("Kraj runde", accent)
            Text(
                if (crewmatesWon) "IGRAČI POBEĐUJU" else "IMPOSTORI POBEĐUJU",
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
                color = accent,
                textAlign = TextAlign.Center,
            )
            Text(
                if (crewmatesWon) "Maska je pala." else "Prevara je ostala neotkrivena.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ResultFact("Tajna reč", result.secretWord, Modifier.weight(1f))
                ResultFact(
                    if (impostorNames.size > 1) "Impostori" else "Impostor",
                    impostorNames.joinToString(),
                    Modifier.weight(1f),
                )
            }
            if (result.tie) {
                Text(
                    "Nerešen rezultat ide u korist impostora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ResultFact(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun VoteTallyPanel(result: GameResult) {
    val sorted = result.tallies.sortedByDescending { it.votes }
    val maxVotes = sorted.maxOfOrNull { it.votes }?.coerceAtLeast(1) ?: 1
    AppPanel {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader("Rezultat glasanja", "Glasovi", "${result.tallies.sumOf { it.votes }} ukupno")
            sorted.forEach { tally ->
                val target = tally.votes.toFloat() / maxVotes
                val progress by animateFloatAsState(target, label = "tallyProgress")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tally.playerName, style = MaterialTheme.typography.labelLarge)
                        Text(tally.votes.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                        )
                    }
                }
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
    val focusManager = LocalFocusManager.current
    BottomActionSurface {
        AnimatedContent(targetState = game.phase, label = "gameAction") { phase ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (phase) {
                    GamePhase.CLUES -> if (state.isMyTurn) {
                        AppTextField(
                            value = state.clueInput,
                            onValueChange = onClueChange,
                            label = "Tvoj trag",
                            supportingText = "${state.clueInput.length}/80",
                            imeAction = ImeAction.Send,
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onSend = {
                                    if (state.canSubmitClue) {
                                        focusManager.clearFocus()
                                        onSubmitClue()
                                    }
                                },
                            ),
                        )
                        PrimaryAction(
                            text = if (state.isActionPending) "Šaljem..." else "Pošalji trag",
                            enabled = state.canSubmitClue,
                            onClick = {
                                focusManager.clearFocus()
                                onSubmitClue()
                            },
                        )
                    } else {
                        val current = game.players.firstOrNull { it.playerId == game.currentPlayerId }?.playerName
                        WaitingAction("Čeka se trag igrača ${current.orEmpty()}.")
                    }

                    GamePhase.VOTING -> if (gameState.hasSubmittedVote) {
                        WaitingAction("Čeka se ostatak glasova (${game.votesSubmitted}/${game.totalPlayers}).")
                    } else {
                        PrimaryAction(
                            text = if (state.isActionPending) "Šaljem..." else "Potvrdi glas",
                            enabled = state.canSubmitVote,
                            onClick = onSubmitVote,
                        )
                    }

                    GamePhase.FINISHED -> if (state.isHost) {
                        PrimaryAction(
                            text = if (state.isActionPending) "Obrada..." else "Nazad u lobby",
                            enabled = !state.isActionPending,
                            onClick = onReset,
                        )
                    } else {
                        WaitingAction("Čeka se host za novu rundu.")
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitingAction(message: String) {
    val accentColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(8.dp)) { drawCircle(accentColor) }
        Text(
            message,
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun GamePhase.displayName() = when (this) {
    GamePhase.CLUES -> "Tragovi"
    GamePhase.VOTING -> "Glasanje"
    GamePhase.FINISHED -> "Rezultat"
}
