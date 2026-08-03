package com.sevaa05.underthemask.lobby.service.exception;

import com.sevaa05.underthemask.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class OnlyHostCanUpdateSettingsException extends ApiException {

    public OnlyHostCanUpdateSettingsException() {
        super(HttpStatus.FORBIDDEN, "ONLY_HOST_CAN_UPDATE_SETTINGS", "Only the host can update lobby settings.");
    }
}
