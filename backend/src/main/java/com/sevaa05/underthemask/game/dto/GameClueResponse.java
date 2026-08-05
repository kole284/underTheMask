package com.sevaa05.underthemask.game.dto;

import java.util.UUID;

public record GameClueResponse(
        UUID playerId,
        String playerName,
        String clue
) {
}
