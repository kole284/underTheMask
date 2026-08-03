package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class LobbyFullException extends ApiException {

    public LobbyFullException() {
        super(HttpStatus.CONFLICT, "LOBBY_FULL", "Lobby already has the maximum number of players.");
    }
}
