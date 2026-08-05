package com.underthemask.android.core.network

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

enum class ErrorKind { VALIDATION, UNAUTHORIZED, NOT_FOUND, CONFLICT, SERVER, NETWORK, UNKNOWN }

data class AppError(
    val kind: ErrorKind,
    val code: String,
    val userMessage: String,
)

class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.userMessage, cause)

class ErrorMapper(private val json: Json) {
    fun map(throwable: Throwable): AppException {
        if (throwable is AppException) return throwable
        if (throwable is IOException) {
            return AppException(
                AppError(
                    ErrorKind.NETWORK,
                    "NETWORK_ERROR",
                    "Server nije dostupan. Proveri adresu, Wi-Fi mrezu i firewall.",
                ),
                throwable,
            )
        }
        if (throwable is SerializationException) {
            return AppException(
                AppError(ErrorKind.SERVER, "INVALID_RESPONSE", "Server je vratio nepoznat format odgovora."),
                throwable,
            )
        }
        if (throwable is HttpException) {
            val apiError = throwable.response()?.errorBody()?.string()?.let { body ->
                runCatching { json.decodeFromString<ApiErrorDto>(body) }.getOrNull()
            }
            val kind = when (throwable.code()) {
                400 -> ErrorKind.VALIDATION
                401, 403 -> ErrorKind.UNAUTHORIZED
                404 -> ErrorKind.NOT_FOUND
                409 -> ErrorKind.CONFLICT
                in 500..599 -> ErrorKind.SERVER
                else -> ErrorKind.UNKNOWN
            }
            return AppException(
                AppError(
                    kind = kind,
                    code = apiError?.code ?: "HTTP_${throwable.code()}",
                    userMessage = localizedMessage(
                        apiError?.code,
                        apiError?.message?.takeIf(String::isNotBlank) ?: defaultHttpMessage(throwable.code()),
                    ),
                ),
                throwable,
            )
        }
        return AppException(
            AppError(ErrorKind.UNKNOWN, "UNKNOWN_ERROR", "Zahtev nije uspeo. Pokusaj ponovo."),
            throwable,
        )
    }

    private fun defaultHttpMessage(status: Int) = when (status) {
        401, 403 -> "Sesija nije vazeca ili akcija nije dozvoljena."
        404 -> "Lobby vise ne postoji."
        409 -> "Akcija trenutno nije dozvoljena."
        in 500..599 -> "Server trenutno ima problem. Pokusaj ponovo."
        else -> "Zahtev nije uspeo."
    }

    private fun localizedMessage(code: String?, fallback: String): String = when (code) {
        "LOBBY_NOT_FOUND" -> "Lobby ne postoji ili je backend restartovan."
        "LOBBY_FULL" -> "Lobby je popunjen."
        "DUPLICATE_PLAYER_NAME" -> "To ime je vec zauzeto u lobbyju."
        "INVALID_LOBBY_CODE" -> "Lobby kod nije vazeci."
        "UNAUTHORIZED_PLAYER_TOKEN" -> "Sacuvana sesija vise nije vazeca."
        "ONLY_HOST_CAN_UPDATE_SETTINGS" -> "Samo host moze da menja podesavanja."
        "ONLY_HOST_CAN_START_GAME" -> "Samo host moze da pokrene igru."
        "ONLY_HOST_CAN_RESET_GAME" -> "Samo host moze da vrati igrace u lobby."
        "SETTINGS_LOCKED" -> "Podesavanja su zakljucana nakon pocetka igre."
        "LOBBY_NOT_WAITING" -> "Ova akcija je dozvoljena samo dok lobby ceka igrace."
        "INVALID_PLAYER_NAME" -> "Ime igraca nije vazeca vrednost."
        "INVALID_GAME_SETTINGS" -> "Podesavanja igre nisu vazeca."
        "NOT_ENOUGH_PLAYERS" -> "Potrebna su najmanje tri igraca."
        "TOO_MANY_IMPOSTORS" -> "Broj impostora mora biti manji od broja igraca."
        "GAME_ALREADY_STARTED" -> "Partija je vec u toku."
        "GAME_CONTENT_UNAVAILABLE" -> "Katalog reci nije dostupan. Proveri bazu i Flyway migracije."
        "INVALID_CLUE" -> "Trag nije vazeci."
        "SECRET_WORD_AS_CLUE" -> "Tajna rec ne moze biti trag."
        "NOT_YOUR_TURN" -> "Nije tvoj red za slanje traga."
        "GAME_NOT_FINISHED" -> "Partija mora biti zavrsena pre povratka u lobby."
        "VOTING_NOT_ACTIVE" -> "Glasanje jos nije aktivno."
        "ALREADY_VOTED" -> "Tvoj glas je vec zabelezen."
        "INVALID_VOTE_COUNT" -> "Izabran je pogresan broj osumnjicenih."
        "INVALID_VOTE_TARGET" -> "Mozes glasati samo za druge igrace iz ove partije."
        "GAME_NOT_STARTED" -> "Partija jos nije pokrenuta."
        "GAME_NOT_IN_PROGRESS" -> "Partija trenutno nije aktivna."
        "VALIDATION_ERROR" -> "Proveri unete podatke i pokusaj ponovo."
        else -> fallback
    }
}

suspend fun <T> ErrorMapper.apiCall(block: suspend () -> T): T = try {
    block()
} catch (throwable: Throwable) {
    throw map(throwable)
}
