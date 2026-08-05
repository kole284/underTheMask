package com.underthemask.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.underthemask.android.core.ui.AppEffect
import com.underthemask.android.core.ui.LoadingScreen
import com.underthemask.android.feature.createLobby.CreateLobbyScreen
import com.underthemask.android.feature.createLobby.CreateLobbyViewModel
import com.underthemask.android.feature.game.GameScreen
import com.underthemask.android.feature.game.GameViewModel
import com.underthemask.android.feature.home.HomeScreen
import com.underthemask.android.feature.home.HomeViewModel
import com.underthemask.android.feature.joinLobby.JoinLobbyScreen
import com.underthemask.android.feature.joinLobby.JoinLobbyViewModel
import com.underthemask.android.feature.lobby.LobbyScreen
import com.underthemask.android.feature.lobby.LobbyViewModel
import com.underthemask.android.feature.startup.StartupViewModel
import kotlinx.coroutines.flow.Flow

private object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val JOIN = "join"
    const val LOBBY = "lobby/{code}"
    const val GAME = "game/{code}"

    fun lobby(code: String) = "lobby/${code.uppercase()}"
    fun game(code: String) = "game/${code.uppercase()}"
}

@Composable
fun AppNavHost(startupViewModel: StartupViewModel = hiltViewModel()) {
    val startupState = startupViewModel.state.collectAsStateWithLifecycle().value
    val initialEffect = startupState.destination
    if (startupState.isLoading || initialEffect == null) {
        LoadingScreen("Obnavljam sesiju...")
        return
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = initialEffect.toRoute()) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                debugBackendAddress = viewModel.debugBackendAddress,
                startupMessage = startupState.errorMessage,
                onCreateLobby = { navController.navigate(Routes.CREATE) },
                onJoinLobby = { navController.navigate(Routes.JOIN) },
            )
        }
        composable(Routes.CREATE) {
            val viewModel: CreateLobbyViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            CollectEffects(viewModel.effects) { navController.handleEffect(it, Routes.CREATE) }
            CreateLobbyScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onNameChange = viewModel::setHostName,
                onImpostorCountChange = viewModel::setImpostorCount,
                onHintTypeChange = viewModel::setHintType,
                onSubmit = viewModel::submit,
            )
        }
        composable(Routes.JOIN) {
            val viewModel: JoinLobbyViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            CollectEffects(viewModel.effects) { navController.handleEffect(it, Routes.JOIN) }
            JoinLobbyScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onNameChange = viewModel::setPlayerName,
                onCodeChange = viewModel::setLobbyCode,
                onSubmit = viewModel::submit,
            )
        }
        composable(Routes.LOBBY) {
            val viewModel: LobbyViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            CollectEffects(viewModel.effects) { navController.handleEffect(it, Routes.LOBBY) }
            LobbyScreen(
                state = state,
                onScreenStarted = viewModel::onScreenStarted,
                onScreenStopped = viewModel::onScreenStopped,
                onImpostorCountChange = viewModel::updateImpostorCount,
                onHintTypeChange = viewModel::updateHintType,
                onStartGame = viewModel::startGame,
                onLeave = viewModel::leaveLobby,
                onDismissError = viewModel::dismissError,
            )
        }
        composable(Routes.GAME) {
            val viewModel: GameViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            CollectEffects(viewModel.effects) { navController.handleEffect(it, Routes.GAME) }
            GameScreen(
                state = state,
                onScreenStarted = viewModel::onScreenStarted,
                onScreenStopped = viewModel::onScreenStopped,
                onToggleRole = viewModel::toggleRoleVisibility,
                onClueChange = viewModel::setClueInput,
                onSubmitClue = viewModel::submitClue,
                onToggleSuspect = viewModel::toggleSuspect,
                onSubmitVote = viewModel::submitVote,
                onReset = viewModel::resetGame,
                onLeave = viewModel::leaveLobby,
                onDismissError = viewModel::dismissError,
            )
        }
    }
}

@Composable
private fun CollectEffects(effects: Flow<AppEffect>, onEffect: (AppEffect) -> Unit) {
    LaunchedEffect(effects) { effects.collect(onEffect) }
}

private fun AppEffect.toRoute(): String = when (this) {
    AppEffect.OpenHome -> Routes.HOME
    is AppEffect.OpenLobby -> Routes.lobby(lobbyCode)
    is AppEffect.OpenGame -> Routes.game(lobbyCode)
}

private fun NavHostController.handleEffect(effect: AppEffect, currentPattern: String) {
    val destination = effect.toRoute()
    navigate(destination) {
        launchSingleTop = true
        when (effect) {
            AppEffect.OpenHome -> popUpTo(graph.id) { inclusive = true }
            else -> popUpTo(currentPattern) { inclusive = true }
        }
    }
}
