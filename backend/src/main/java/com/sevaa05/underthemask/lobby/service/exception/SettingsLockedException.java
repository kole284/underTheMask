package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class SettingsLockedException extends ApiException {

    public SettingsLockedException() {
        super(HttpStatus.CONFLICT, "SETTINGS_LOCKED", "Settings cannot change after the game starts.");
    }
}
