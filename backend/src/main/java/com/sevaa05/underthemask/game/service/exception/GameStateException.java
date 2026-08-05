package com.sevaa05.underthemask.game.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class GameStateException extends ApiException {

    public GameStateException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static GameStateException conflict(String code, String message) {
        return new GameStateException(HttpStatus.CONFLICT, code, message);
    }

    public static GameStateException forbidden(String code, String message) {
        return new GameStateException(HttpStatus.FORBIDDEN, code, message);
    }

    public static GameStateException unavailable(String code, String message) {
        return new GameStateException(HttpStatus.SERVICE_UNAVAILABLE, code, message);
    }
}
