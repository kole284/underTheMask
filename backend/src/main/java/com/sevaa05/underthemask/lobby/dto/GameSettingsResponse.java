package com.sevaa05.underthemask.lobby.dto;

import com.sevaa05.underthemask.lobby.model.GameSettings;
import com.sevaa05.underthemask.lobby.model.HintType;

public record GameSettingsResponse(
        int impostorCount,
        HintType hintType
) {

    public static GameSettingsResponse from(GameSettings settings) {
        return new GameSettingsResponse(settings.getImpostorCount(), settings.getHintType());
    }
}
