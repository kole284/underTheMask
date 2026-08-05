package com.sevaa05.underthemask.game.dto;

import com.sevaa05.underthemask.game.model.GameWinner;
import java.util.List;
import java.util.UUID;

public record GameResultResponse(
        GameWinner winner,
        String secretWord,
        List<UUID> impostorPlayerIds,
        List<UUID> mostVotedPlayerIds,
        boolean tie,
        List<VoteTallyResponse> tallies
) {
}
