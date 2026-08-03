package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidGameSettingsException extends ApiException {

    public InvalidGameSettingsException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_GAME_SETTINGS", message);
    }
}
