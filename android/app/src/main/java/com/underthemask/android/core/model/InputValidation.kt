package com.underthemask.android.core.model

object InputValidation {
    private val lobbyCodePattern = Regex("^[A-HJ-NP-Z2-9]{6}$")

    fun playerNameError(name: String): String? = when {
        name.trim().isEmpty() -> "Unesi ime igrača."
        name.trim().length > 32 -> "Ime može imati najviše 32 karaktera."
        else -> null
    }

    fun normalizeLobbyCode(code: String): String = code.trim().uppercase()

    fun lobbyCodeError(code: String): String? = when {
        normalizeLobbyCode(code).isEmpty() -> "Unesi lobby kod."
        !lobbyCodePattern.matches(normalizeLobbyCode(code)) -> "Lobby kod mora imati 6 važećih znakova."
        else -> null
    }

    fun clueError(clue: String, secretWord: String?): String? = when {
        clue.trim().isEmpty() -> "Trag ne može biti prazan."
        clue.trim().length > 80 -> "Trag može imati najviše 80 karaktera."
        secretWord != null && clue.trim().equals(secretWord, ignoreCase = true) ->
            "Tajna reč ne može biti trag."
        else -> null
    }

    fun voteError(selectedCount: Int, requiredCount: Int): String? =
        if (selectedCount == requiredCount) null
        else "Izaberi tačno $requiredCount ${if (requiredCount == 1) "igrača" else "igrača"}."

    fun toggleSuspect(
        selectedIds: Set<String>,
        playerId: String,
        requiredCount: Int,
    ): Set<String> = when {
        playerId in selectedIds -> selectedIds - playerId
        selectedIds.size < requiredCount -> selectedIds + playerId
        else -> selectedIds
    }
}
