package com.sevaa05.underthemask.game.dto;

import java.util.UUID;

public record VoteTallyResponse(
        UUID playerId,
        String playerName,
        int votes
) {
}
