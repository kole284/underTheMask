package com.sevaa05.underthemask.game.dto;

import java.util.UUID;

public record GamePlayerResponse(
        UUID playerId,
        String playerName,
        boolean connected
) {
}
