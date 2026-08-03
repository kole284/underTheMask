package com.sevaa05.underthemask.lobby.dto;

import com.sevaa05.underthemask.lobby.model.HintType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLobbyRequest(
        @NotBlank
        @Size(max = 32)
        String hostName,

        @NotNull
        Integer impostorCount,

        @NotNull
        HintType hintType
) {
}
