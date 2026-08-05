package com.underthemask.android.core.model

object InputValidation {
    private val lobbyCodePattern = Regex("^[A-HJ-NP-Z2-9]{6}$")

    fun playerNameError(name: String): String? = when {
        name.trim().isEmpty() -> "Unesi ime igraca."
        name.trim().length > 32 -> "Ime moze imati najvise 32 karaktera."
        else -> null
    }

    fun normalizeLobbyCode(code: String): String = code.trim().uppercase()

    fun lobbyCodeError(code: String): String? = when {
        normalizeLobbyCode(code).isEmpty() -> "Unesi lobby kod."
        !lobbyCodePattern.matches(normalizeLobbyCode(code)) -> "Lobby kod mora imati 6 vazecih znakova."
        else -> null
    }

    fun clueError(clue: String, secretWord: String?): String? = when {
        clue.trim().isEmpty() -> "Trag ne moze biti prazan."
        clue.trim().length > 80 -> "Trag moze imati najvise 80 karaktera."
        secretWord != null && clue.trim().equals(secretWord, ignoreCase = true) ->
            "Tajna rec ne moze biti trag."
        else -> null
    }

    fun voteError(selectedCount: Int, requiredCount: Int): String? =
        if (selectedCount == requiredCount) null
        else "Izaberi tacno $requiredCount ${if (requiredCount == 1) "igraca" else "igraca"}."

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
