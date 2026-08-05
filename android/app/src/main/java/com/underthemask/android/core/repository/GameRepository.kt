package com.underthemask.android.core.repository

import com.underthemask.android.core.model.GameState
import com.underthemask.android.core.model.Lobby
import com.underthemask.android.core.network.ErrorMapper
import com.underthemask.android.core.network.LobbyApiService
import com.underthemask.android.core.network.SubmitClueRequestDto
import com.underthemask.android.core.network.SubmitVoteRequestDto
import com.underthemask.android.core.network.apiCall
import com.underthemask.android.core.network.toDomain
import javax.inject.Inject
import javax.inject.Singleton

interface GameRepository {
    suspend fun start(code: String): GameState
    suspend fun get(code: String): GameState
    suspend fun submitClue(code: String, clue: String): GameState
    suspend fun submitVote(code: String, playerIds: List<String>): GameState
    suspend fun reset(code: String): Lobby
}

@Singleton
class DefaultGameRepository @Inject constructor(
    private val api: LobbyApiService,
    private val errorMapper: ErrorMapper,
) : GameRepository {
    override suspend fun start(code: String) = errorMapper.apiCall {
        api.startGame(code.normalized()).toDomain()
    }

    override suspend fun get(code: String) = errorMapper.apiCall {
        api.getGame(code.normalized()).toDomain()
    }

    override suspend fun submitClue(code: String, clue: String) = errorMapper.apiCall {
        api.submitClue(code.normalized(), SubmitClueRequestDto(clue.trim())).toDomain()
    }

    override suspend fun submitVote(code: String, playerIds: List<String>) = errorMapper.apiCall {
        api.submitVote(code.normalized(), SubmitVoteRequestDto(playerIds)).toDomain()
    }

    override suspend fun reset(code: String) = errorMapper.apiCall {
        api.resetGame(code.normalized()).toDomain()
    }

    private fun String.normalized() = trim().uppercase()
}
