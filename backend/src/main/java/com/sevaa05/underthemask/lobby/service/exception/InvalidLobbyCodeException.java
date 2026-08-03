package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidLobbyCodeException extends ApiException {

    public InvalidLobbyCodeException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_LOBBY_CODE", "Lobby code must contain six non-ambiguous uppercase characters.");
    }
}
