package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatePlayerNameException extends ApiException {

    public DuplicatePlayerNameException() {
        super(HttpStatus.CONFLICT, "DUPLICATE_PLAYER_NAME", "Player name already exists in this lobby.");
    }
}
