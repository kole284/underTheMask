package com.underthemask.android.core.ui

sealed interface AppEffect {
    data object OpenHome : AppEffect
    data class OpenLobby(val lobbyCode: String) : AppEffect
    data class OpenGame(val lobbyCode: String) : AppEffect
}

fun Throwable.userMessage(): String =
    (this as? com.underthemask.android.core.network.AppException)?.error?.userMessage
        ?: "Doslo je do neocekivane greske."
