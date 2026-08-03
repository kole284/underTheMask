package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class UnauthorizedPlayerTokenException extends ApiException {

    public UnauthorizedPlayerTokenException() {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_PLAYER_TOKEN", "Player reconnect token is missing or invalid.");
    }
}
