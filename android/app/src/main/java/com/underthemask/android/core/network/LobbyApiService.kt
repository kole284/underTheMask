package com.underthemask.android.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LobbyApiService {
    @POST("lobbies")
    suspend fun createLobby(@Body request: CreateLobbyRequestDto): LobbySessionDto

    @POST("lobbies/{code}/players")
    suspend fun joinLobby(@Path("code") code: String, @Body request: JoinLobbyRequestDto): LobbySessionDto

    @GET("lobbies/{code}")
    suspend fun getLobby(@Path("code") code: String): LobbyDto

    @POST("lobbies/{code}/reconnect")
    suspend fun reconnect(@Path("code") code: String): LobbySessionDto

    @DELETE("lobbies/{code}/players/me")
    suspend fun leaveLobby(@Path("code") code: String)

    @PATCH("lobbies/{code}/settings")
    suspend fun updateSettings(
        @Path("code") code: String,
        @Body request: UpdateSettingsRequestDto,
    ): LobbyDto

    @POST("lobbies/{code}/game/start")
    suspend fun startGame(@Path("code") code: String): GameStateDto

    @GET("lobbies/{code}/game")
    suspend fun getGame(@Path("code") code: String): GameStateDto

    @POST("lobbies/{code}/game/clues")
    suspend fun submitClue(
        @Path("code") code: String,
        @Body request: SubmitClueRequestDto,
    ): GameStateDto

    @POST("lobbies/{code}/game/votes")
    suspend fun submitVote(
        @Path("code") code: String,
        @Body request: SubmitVoteRequestDto,
    ): GameStateDto

    @POST("lobbies/{code}/game/reset")
    suspend fun resetGame(@Path("code") code: String): LobbyDto
}
