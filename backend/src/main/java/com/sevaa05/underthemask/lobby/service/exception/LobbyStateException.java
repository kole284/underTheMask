package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class LobbyStateException extends ApiException {

    public LobbyStateException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
