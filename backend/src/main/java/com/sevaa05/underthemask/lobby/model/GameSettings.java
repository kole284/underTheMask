package com.sevaa05.underthemask.lobby.model;

import com.sevaa05.underthemask.lobby.service.exception.InvalidGameSettingsException;

public class GameSettings {

    private final int impostorCount;
    private final HintType hintType;

    public GameSettings(int impostorCount, HintType hintType) {
        if (impostorCount != 1 && impostorCount != 2) {
            throw new InvalidGameSettingsException("impostorCount must be 1 or 2.");
        }
        if (hintType == null) {
            throw new InvalidGameSettingsException("hintType is required.");
        }
        this.impostorCount = impostorCount;
        this.hintType = hintType;
    }

    public int getImpostorCount() {
        return impostorCount;
    }

    public HintType getHintType() {
        return hintType;
    }
}
