package com.sevaa05.underthemask.game.dto;

import com.sevaa05.underthemask.game.model.GamePhase;
import java.util.List;
import java.util.UUID;

public record GamePublicResponse(
        UUID roundId,
        GamePhase phase,
        UUID currentPlayerId,
        List<GamePlayerResponse> players,
        List<GameClueResponse> clues,
        int votesSubmitted,
        int totalPlayers,
        int requiredSuspectCount,
        GameResultResponse result
) {
}
