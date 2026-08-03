package com.sevaa05.underthemask.lobby.dto;

import com.sevaa05.underthemask.lobby.model.HintType;
import jakarta.validation.constraints.NotNull;

public record UpdateSettingsRequest(
        @NotNull
        Integer impostorCount,

        @NotNull
        HintType hintType
) {
}
