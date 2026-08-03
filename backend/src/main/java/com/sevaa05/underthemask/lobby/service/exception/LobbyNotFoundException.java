package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class LobbyNotFoundException extends ApiException {

    public LobbyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "LOBBY_NOT_FOUND", "Lobby was not found.");
    }
}
