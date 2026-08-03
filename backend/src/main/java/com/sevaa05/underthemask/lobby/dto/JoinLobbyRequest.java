package com.sevaa05.underthemask.lobby.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinLobbyRequest(
        @NotBlank
        @Size(max = 32)
        String playerName
) {
}
